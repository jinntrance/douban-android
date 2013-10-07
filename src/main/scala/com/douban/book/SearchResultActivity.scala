package com.douban.book

import com.douban.base._
import android.os.Bundle
import android.widget._
import android.view._
import com.douban.models.{Collection, Book, BookSearchResult}
import scala.concurrent._
import org.scaloid.common._
import ExecutionContext.Implicits.global
import java.lang.String
import Constant._
import scala.util.Failure
import scala.util.Success
import java.util
import scala.collection


/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 5/3/13 5:25 PM
 * @version 1.0
 * @see http://developers.douban.com/wiki/?title=api_v2
 */
class SearchResultActivity extends DoubanActivity {
  var searchText = ""

  protected override def onCreate(b: Bundle) = {
    super.onCreate(b)
    setContentView(R.layout.book_list)
    searchText = getIntent.getStringExtra(SEARCH_TEXT_KEY)
    if (null == b) {
      var noResult = true
      waitToLoad({if (noResult) finish()})
      future {
        Book.search(searchText, "", count = this.countPerPage)
      } onComplete {
        case Success(books) =>  {
          noResult = false
          SearchResult.init(books)
          books.total match {
            case 0 => toast(R.string.search_no_result)
            case _ => {
              debug("search result total:" + books.total)
              findViewById(R.id.list_container) match {
                case v: View => runOnUiThread(fragmentManager.beginTransaction().replace(R.id.list_container, new SearchResultList()).commit())
                case _ =>
              }
            }
          }
          finishedLoading()
        }
        case Failure(err) => {
          error(err.getMessage)
          finish()
        }
      }
    }
  }

  def updateTitle() {
    setTitle(getString(R.string.search_result, searchText))
  }

}

class SearchResultList extends DoubanListFragment[SearchResultActivity] {

  val stateMapping=Map("wish"-> "想读","reading"-> "在读","read" -> "读过")
  var loading = false
  var footer: Option[View] = None
  private var currentPage = 1

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, bundle: Bundle) = {
    footer = Some(inflater.inflate(R.layout.book_list_loader, null))
    super.onCreateView(inflater, container, bundle)
  }

  override def onActivityCreated(bundle: Bundle) {
    super.onActivityCreated(bundle)
    getListView.setDivider(null)
    getListView.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS)
    getListView.addFooterView(footer.get)
    setListAdapter(new BookItemAdapter(SearchResult.books.map(b=>beanToMap(b))))
    getThisActivity.updateTitle()
    updateFooter()
  }

  def updateFooter() = {
    getThisActivity.find[TextView](R.id.to_load) match {
      case footer: TextView => footer.setText(getString(R.string.swipe_up_to_load, SearchResult.searchedNumber.toString, SearchResult.total.toString))
      case _ =>
    }
  }

  override def onStart() {
    super.onStart()
    if (getFragmentManager.findFragmentById(R.id.book_fragment) != null) {
      getListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE)
    }
  }


  override def onListItemClick(l: ListView, v: View, position: Int, id: Long) {
    getListView.setItemChecked(position, true)
    getThisActivity.fragmentManager.findFragmentById(R.id.book_fragment) match {
      case bf: SearchResultDetail =>
        bf.updateBookView()
      case _ => {
        SearchResult.selectedBook=Some(SearchResult.books.get(position))
        startActivity(SIntent[BookActivity].putExtra(Constant.BOOK_KEY,SearchResult.selectedBook))
      }
    }
  }

  class BookItemAdapter(list:collection.mutable.Buffer[Map[String,String]]) extends BaseAdapter {

    case class ViewHolder(image:ImageView,title:TextView,authors:TextView,publisher:TextView,stars:ImageView)

    override def getView(position: Int, view: View, parent: ViewGroup): View = {
      import SearchResult._
      import CollectionActivity._
      val convertView = if(null!=view) view else getThisActivity.getLayoutInflater.inflate(R.layout.book_list_item,null)
      if (null != convertView) {
        val b = SearchResult.books.get(position)
        batchSetValues(mapping,list.get(position),convertView)
        val c: Collection = b.current_user_collection
        displayWhen(R.id.favorite, null == c, convertView)
        if (null != c) {
          convertView.findViewById(R.id.currentState) match{
            case state:TextView=>{
              state.setVisibility(View.VISIBLE)
              state.setText(stateMapping(c.status))
              state.setTextColor(colorMap(idsMap(c.status)))
            }
            case _=>
          }
        } else convertView.findViewById(R.id.fav_layout) onClick (v => {
          SearchResult.selectedBook=Some(SearchResult.books.get(position))
          startActivity(SIntent[CollectionActivity].putExtra(Constant.BOOK_KEY,SearchResult.books.get(position)))
        })
        getThisActivity.loadImageWithTitle(b.image, R.id.book_img, b.title, convertView)
        if (position + 2 >= SearchResult.searchedNumber) load()
      }
      convertView
    }

    def load() = {
      if ((SearchResult.searchedNumber < SearchResult.total) && !loading) future {
        currentPage += 1
        loading = true
        Book.search(getThisActivity.searchText, "", currentPage, countPerPage)
      } onSuccess {
        case b => {
         addResult(b)
         runOnUiThread{
           notifyDataSetChanged()
           updateFooter()
         }
         if(SearchResult.searchedNumber == SearchResult.total)toast(getString(R.string.more_books_loaded).format(SearchResult.searchedNumber))
         else toast(R.string.more_loaded_finished)
         loading = false
        }
      }
    }

    def getCount: Int = SearchResult.searchedNumber

    def getItem(index: Int): Map[String,String] = list.get(index)

    def getItemId(position: Int): Long = position

    def addResult(r:BookSearchResult)={
      list++=r.books.map(beanToMap(_))
      SearchResult.add(r)
    }
  }

}

object SearchResult {
  val STATE_STRING="current_user_collection.status"
  val mapping: Map[Int, Any] = Map(
    R.id.bookTitle -> "title", R.id.bookAuthor -> List("author","translator"), R.id.bookPublisher -> List("publisher","pubdate"),
    R.id.ratingNum -> ("rating.numRaters","(%s)"), R.id.ratedStars -> "rating.average",
    R.id.currentState -> STATE_STRING
  )
  var books: java.util.List[Book] = new util.ArrayList[Book]()
  var selectedBook: Option[Book] = None
  var total: Long = 0
  var searchedNumber = 0

  def init(r: BookSearchResult) = {
    books.clear()
    books.addAll(r.books)
    total = r.total
    searchedNumber = r.books.size()
  }

  def add(r: BookSearchResult) = {
    books.addAll(r.books)
    searchedNumber += r.books.size()
  }
}