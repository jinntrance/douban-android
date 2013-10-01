package com.douban.book

import com.douban.base._
import android.os.Bundle
import android.widget._
import android.view._
import com.douban.models.Book
import scala.concurrent._
import org.scaloid.common._
import ExecutionContext.Implicits.global
import java.lang.String
import android.app.{ListFragment, Activity}
import com.douban.models.BookSearchResult
import scala.util.Failure
import scala.util.Success
import android.content.Context
import Constant._

/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 5/3/13 5:25 PM
 * @version 1.0
 * @see http://developers.douban.com/wiki/?title=api_v2
 */
class SearchResultActivity extends DoubanActivity with OnBookSelectedListener {
  private var searchText = ""

  protected override def onCreate(b: Bundle) = {
    super.onCreate(b)
    setContentView(R.layout.book_list)
    searchText = SearchActivity.getSearchText(getIntent.getExtras)
    if (null == b) {
      if (findViewById(R.id.list_container) != null) {
        val listFragment = new SearchResultList()
        listFragment.setArguments(getIntent.getExtras)
        getFragmentManager.beginTransaction().replace(R.id.list_container, listFragment).commit()
      }
    }
  }

  def updateTitle() {
    setTitle(getString(R.string.search_result, searchText))
  }

  def onBookSelected(position: Int) {
    val articleFrag: SearchResultDetail = getFragmentManager.findFragmentById(R.id.book_fragment).asInstanceOf[SearchResultDetail]
    if (articleFrag != null) {
      articleFrag.updateBookView()
    } else {
      startActivity(SIntent[BookActivity].putExtras(getIntent.getExtras))
    }
  }
}

trait OnBookSelectedListener {
  def onBookSelected(position: Int)
}

class SearchResultList extends DoubanListFragment {
  var books: java.util.List[Book] = null
  var adapter: SimpleAdapter = null
  private var currentPage = 1
  private var result: BookSearchResult = null
  private var mCallback: OnBookSelectedListener = null

  override def onCreate(b: Bundle) {
    super.onCreate(b)
    result = SearchActivity.getBooks(getActivity.getIntent.getExtras)
    books = result.books
    adapter = new BookItemAdapter(getActivity, listToMap(books), R.layout.book_list_item, SearchResult.mapping.values.toArray, SearchResult.mapping.keys.toArray)
    setListAdapter(adapter)
  }

/*  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, bundle: Bundle) = {
    val r = super.onCreateView(inflater, container, bundle)
    footer = inflater.inflate(R.layout.book_list_loader, container, true)
    r
  }*/

  override def onActivityCreated(bundle: Bundle) {
    super.onActivityCreated(bundle)
    getListView.setDivider(null)
    getListView.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS)
    getActivity.asInstanceOf[SearchResultActivity].updateTitle()
    updateFooter()
  }

  def updateFooter() {
    val footer=getThisActivity.find[TextView](R.id.to_load)
    if(null!=footer) footer.setText(getString(R.string.swipe_up_to_load, new Integer(books.size()), new Integer(result.total)))
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

  def load(v: View) {
    if (currentPage * this.count < result.total) future {
      toast(R.string.loading)
      currentPage += 1
      Book.search(SearchActivity.getSearchText(getActivity.getIntent.getExtras), "", currentPage, this.count)
    } onComplete {
      case Success(b) => {
        books.addAll(b.books)
        adapter.notifyDataSetChanged()
        updateFooter()
      }
      case Failure(err) => sys.error(err.getMessage)
    }
  }

  override def onListItemClick(l: ListView, v: View, position: Int, id: Long) {
    getListView.setItemChecked(position, true)
    getActivity.getIntent.putExtra(BOOK_KEY, result.books.get(position))
    mCallback.onBookSelected(position)
  }

  class BookItemAdapter(context: Context, data: java.util.List[_ <: java.util.Map[String, _]], resource: Int, from: Array[String], to: Array[Int]) extends SimpleAdapter(context, data, resource, from, to) {
    override def getView(position: Int, view: View, parent: ViewGroup): View = {
      val convertView = super.getView(position, view, parent)
      if (null != convertView) {
        val b = books.get(position)
        convertView.find[TextView](R.id.ratingNum).setText("(" + b.rating.numRaters + ")")
        if (null != b.current_user_collection) {
          convertView.find[ImageView](R.id.favorite).setVisibility(View.GONE)
          convertView.find[TextView](R.id.currentState).setText(b.current_user_collection.status match {
            case "wish" => "想读"
            case "reading" => "在读"
            case "read" => "读过"
            case _ => ""
          })
        } else {
          getActivity.getIntent.putExtra(BOOK_KEY, books.get(position))
          convertView.find[ImageView](R.id.favorite).onClick(v=>{
            getActivity.getIntent.putExtra(BOOK_KEY, books.get(position))
            getFragmentManager.beginTransaction().add(R.id.list_container_root,new CollectionFragment()).commit()
          })
          convertView.find[TextView](R.id.currentState).onClick(v => {
            getActivity.getIntent.putExtra(BOOK_KEY, books.get(position)).putExtra(STATE_ID, v.getId)
            getFragmentManager.beginTransaction().add(R.id.list_container_root,new CollectionFragment()).commit()
          })
        }
        getThisActivity.loadImage(b.image, R.id.book_img, b.title, convertView)
      }
      convertView
    }
  }

}

object SearchResult {
  val mapping: Map[Int, String] = Map(
    R.id.bookTitle -> "title", R.id.bookAuthor -> "author", R.id.bookPublisher -> "publisher",
    R.id.ratingNum -> "rating.numRaters", R.id.ratedStars -> "rating.average",
    R.id.currentState -> "current_user_collection.status"
  )
}