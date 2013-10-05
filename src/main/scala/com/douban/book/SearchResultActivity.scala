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
import android.app.{ProgressDialog, Activity}
import android.content.{DialogInterface, Context}
import Constant._
import scala.util.Failure
import scala.util.Success
import java.util

/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 5/3/13 5:25 PM
 * @version 1.0
 * @see http://developers.douban.com/wiki/?title=api_v2
 */
class SearchResultActivity extends DoubanActivity with OnBookSelectedListener {
  var searchText = ""

  protected override def onCreate(b: Bundle) = {
    super.onCreate(b)
    setContentView(R.layout.book_list)
    searchText = getIntent.getStringExtra(SEARCH_TEXT_KEY)
    if (null == b) {
      var pd: ProgressDialog = null
      var noResult = true
      pd = ProgressDialog.show(this, getString(R.string.search), getString(R.string.searching), false, true, new DialogInterface.OnCancelListener() {
        def onCancel(p1: DialogInterface) {
          if (noResult) finish()
        }
      })
      future {
        Book.search(searchText, "", count = this.countPerPage)
      } onComplete {
        case Success(books) => runOnUiThread {
          noResult = false
          SearchResult.init(books)
          books.total match {
            case 0 => toast(R.string.search_no_result)
            case _ => {
              debug("search result total:" + books.total)
              findViewById(R.id.list_container) match {
                case v: View => getFragmentManager.beginTransaction().replace(R.id.list_container, new SearchResultList()).commit()
                case _ =>
              }
            }
          }
          pd.cancel()
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

  def onBookSelected(position: Int) {
    getFragmentManager.findFragmentById(R.id.book_fragment) match {
      case bf: SearchResultDetail =>
        bf.updateBookView()
      case _ => {
        SearchResult.selectedBook=Some(SearchResult.books.get(position))
        startActivity(SIntent[BookActivity])
      }
    }
  }
}

trait OnBookSelectedListener {
  def onBookSelected(position: Int)
}

class SearchResultList extends DoubanListFragment[SearchResultActivity] {

  val stateMapping=Map("wish"-> "想读","reading"-> "在读","read" -> "读过")
  var loading = false
  var adapter: Option[BookItemAdapter] = None
  var footer: Option[View] = None
  private var currentPage = 1
  private var mCallback: OnBookSelectedListener = null

  override def onCreate(b: Bundle) {
    super.onCreate(b)
    adapter = Some(new BookItemAdapter())
  }

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, bundle: Bundle) = {
    footer = Some(inflater.inflate(R.layout.book_list_loader, null))
    super.onCreateView(inflater, container, bundle)
  }

  override def onActivityCreated(bundle: Bundle) {
    super.onActivityCreated(bundle)
    getListView.setDivider(null)
    getListView.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS)
    getListView.addFooterView(footer.get)
    setListAdapter(adapter.get)
    getThisActivity.updateTitle()
    updateFooter()
  }

  def updateFooter() = runOnUiThread {
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

  override def onAttach(activity: Activity) {
    super.onAttach(activity)
    mCallback = activity.asInstanceOf[OnBookSelectedListener]
  }

  override def onListItemClick(l: ListView, v: View, position: Int, id: Long) {
    getListView.setItemChecked(position, true)
    mCallback.onBookSelected(position)
  }

  class BookItemAdapter extends BaseAdapter {

    override def getView(position: Int, view: View, parent: ViewGroup): View = {
      import SearchResult._
      val convertView = if(null!=view) view else getThisActivity.getLayoutInflater.inflate(R.layout.book_list_item,parent)
      if (null != convertView) {
        val b = getItem(position)
        batchSetTextView(mapping,beanToMap(b))
        val c: Collection = b.current_user_collection
        displayWhen(R.id.favorite, null == c, convertView)
        if (null != c) {
          convertView.find[TextView](R.id.currentState).setText(stateMapping(c.status))
        } else convertView.findViewById(R.id.fav_layout) onClick (v => {
          runOnUiThread(startActivity(SIntent[CollectionActivity]))
        })
        getThisActivity.loadImage(b.image, R.id.book_img, b.title, convertView)
        if (position + currentPage/4 >= SearchResult.searchedNumber) load()
      }
      convertView
    }

    def load() = {
      if ((SearchResult.searchedNumber <= SearchResult.total) && !loading) future {
        currentPage += 1
        loading = true
        Book.search(getThisActivity.searchText, "", currentPage, countPerPage)
      } onSuccess {
        case b => {
          addResult(b)
          runOnUiThread{
            notifyDataSetChanged()
            toast(getString(R.string.more_books_loaded,SearchResult.searchedNumber.toString))
          }
          updateFooter()
          loading = false
        }
      }
    }

    def getCount: Int = SearchResult.searchedNumber

    def getItem(index: Int): Book = SearchResult.books.get(index)

    def getItemId(position: Int): Long = position

    def addResult(r:BookSearchResult)=SearchResult.add(r)
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