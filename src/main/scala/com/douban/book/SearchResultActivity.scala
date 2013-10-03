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
import android.app.{ProgressDialog, Activity}
import android.content.{DialogInterface, Context}
import Constant._
import com.douban.models.BookSearchResult
import scala.util.Failure
import scala.util.Success

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
    searchText=getIntent.getStringExtra(SEARCH_TEXT_KEY)
    if(null == b){
    var pd: ProgressDialog = null
    var noResult=true
    pd= ProgressDialog.show(this, getString(R.string.search), getString(R.string.searching), false, true, new DialogInterface.OnCancelListener() {
      def onCancel(p1: DialogInterface) {
          if(noResult)  finish()
      }
    })
    future {
      Book.search(searchText, "", count = this.countPerPage)
    } onComplete {
      case Success(books) => runOnUiThread{
        noResult=false
        pd.cancel()
        SearchResult.init(books)
        if (books.total == 0) {
          toast(R.string.search_no_result)
        }
        else {
          debug("search result total:" + books.total)
          if (findViewById(R.id.list_container) != null) {
            getFragmentManager.beginTransaction().replace(R.id.list_container, new SearchResultList()).commit()
          }
        }
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

class SearchResultList extends DoubanListFragment[SearchResultActivity] {
  var loading=false
  var adapter: SimpleAdapter = null
  var footer:View=null
  private var currentPage = 1
  private var mCallback: OnBookSelectedListener = null

  override def onCreate(b: Bundle) {
    super.onCreate(b)
    adapter = new BookItemAdapter(getActivity, listToMap(SearchResult.books), R.layout.book_list_item, SearchResult.mapping.values.toArray, SearchResult.mapping.keys.toArray)
  }

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, bundle: Bundle) = {
    footer = inflater.inflate(R.layout.book_list_loader, null)
    super.onCreateView(inflater, container, bundle)
  }

  override def onActivityCreated(bundle: Bundle) {
    super.onActivityCreated(bundle)
    getListView.setDivider(null)
    getListView.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS)
    getListView.addFooterView(footer)
    setListAdapter(adapter)
    getThisActivity.updateTitle()
    updateFooter()
  }

  override def onDestroyView() {
    super.onDestroyView()
    setListAdapter(null)
  }

  def updateFooter() =runOnUiThread{
    val footer=getThisActivity.find[TextView](R.id.to_load)
    if(null!=footer) footer.setText(getString(R.string.swipe_up_to_load, new Integer(SearchResult.books.size()), SearchResult.total.toString))
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

  class BookItemAdapter(context: Context, data: java.util.List[java.util.Map[String, String]], resource: Int, from: Array[String], to: Array[Int]) extends SimpleAdapter(context, data, resource, from, to) {
    override def getView(position: Int, view: View, parent: ViewGroup): View ={
      val convertView = super.getView(position, view, parent)
      if (null != convertView) {
        val b = SearchResult.books.get(position)
        convertView.find[TextView](R.id.ratingNum).setText("(" + b.rating.numRaters + ")")
        displayWhen(R.id.favorite,null==b.current_user_collection,convertView)
        if (null != b.current_user_collection) {
          convertView.find[TextView](R.id.currentState).setText(b.current_user_collection.status match {
            case "wish" => "想读"
            case "reading" => "在读"
            case "read" => "读过"
            case _ =>""
          })
        } else convertView.findViewById(R.id.fav_layout) onClick(v=>{
            startActivity(SIntent[CollectionActivity])
          })
        getThisActivity.loadImage(b.image, R.id.book_img, b.title, convertView)
        if(position+2==SearchResult.total) load()
      }
      convertView
    }
    def load()= {
      if ((SearchResult.searchedNumber <= SearchResult.total)&& !loading) future {
        currentPage += 1
        loading=true
        Book.search(getThisActivity.searchText, "", currentPage, countPerPage)
      } onComplete {
        case Success(b) => {
          SearchResult.add(b)
          data.addAll(b.books.map(beanToMap(_)))
          runOnUiThread(notifyDataSetChanged())
          updateFooter()
          loading=false
        }
        case Failure(err) => if(null!=err) err.printStackTrace()
      }
    }
  }
}

object SearchResult {
  val mapping: Map[Int, String] = Map(
    R.id.bookTitle -> "title", R.id.bookAuthor -> "author", R.id.bookPublisher -> "publisher",
    R.id.ratingNum -> "rating.numRaters", R.id.ratedStars -> "rating.average",
    R.id.currentState -> "current_user_collection.status"
  )
  var books:java.util.List[Book]=null
  var selectedBook:Book=null
  var total:Long=0
  var searchedNumber=0

  def init(r:BookSearchResult)={
    books=r.books
    total=r.total
    searchedNumber+=r.books.size()
  }
  def add(r:BookSearchResult)={
    books.addAll(r.books)
    searchedNumber+=books.size()
  }
}