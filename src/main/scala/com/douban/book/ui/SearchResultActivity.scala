package com.douban.book.ui

import com.douban.base.{DoubanList, DoubanActivity}
import android.os.Bundle
import com.douban.book.{TR, R}
import android.widget.{TextView, AbsListView, ListView}
import android.view._
import com.douban.models.{BookSearchResult, Book}
import scala.concurrent._
import org.scaloid.common._
import scala.util.Success
import scala.util.Failure
import ExecutionContext.Implicits.global
import java.lang.String
import android.app.{FragmentTransaction, ListFragment, Fragment, Activity}
import android.graphics.drawable.Drawable
import java.net.URL
import java.io.InputStream

/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 5/3/13 5:25 PM
 * @version 1.0
 * @see http://developers.douban.com/wiki/?title=api_v2
 */

class SearchResultActivity extends DoubanActivity with OnBookSelectedListener{
  private var currentPage = 1
  private var searchText = ""

  protected override def onCreate(b: Bundle) = {
    super.onCreate(b)
    setContentView(R.layout.book_list)
    if (null== b) {
      import SearchActivity._
      searchText = getSearchText(getIntent.getExtras)
      setTitle(getString(R.string.search_result, searchText))
      if(findViewById(R.id.list_container)!=null){
      val f: Fragment = new SearchResultList()
      f.setArguments(getIntent.getExtras)
      getFragmentManager.beginTransaction().add(R.id.list_container, f).commit()
      }
    }
  }

  def load(v: View) {
    future {
      toast(R.string.loading)
      Book.search(searchText, "", currentPage, this.count)
    } onComplete {
      case Success(books) => {

      }
      case Failure(err) => sys.error(err.getMessage)
    }
  }

  def onBookSelected(position: Int) {
    val articleFrag: SearchResultDetail = getFragmentManager.findFragmentById(R.id.book_fragment).asInstanceOf[SearchResultDetail]
    if (articleFrag != null) {
      articleFrag.updateArticleView(position)
    }
    else {
      val newFragment: SearchResultDetail = new SearchResultDetail()
      val args: Bundle = new Bundle
      args.putInt(SearchResult.ARG_POSITION, position)
      newFragment.setArguments(args)
      val transaction: FragmentTransaction = getFragmentManager.beginTransaction
      transaction.replace(R.id.list_container, newFragment)
      transaction.addToBackStack(null)
      transaction.commit
    }
  }
}

trait OnBookSelectedListener {
  def onBookSelected(position: Int)
}

class SearchResultList extends ListFragment with DoubanList {

//  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle) = inflater.inflate(R.id.list_container, container)
  var result:BookSearchResult=null

  private[ui] var mCallback: OnBookSelectedListener = null

  override def onCreate(b: Bundle) {
    super.onCreate(b)
    result = SearchActivity.getBooks(getActivity.getIntent.getExtras)
    setListAdapter(simpleAdapter(getActivity, result.books, R.layout.book_list_item, SearchResult.mapping))
  }

  override def onStart() {
    super.onStart()
    if (getFragmentManager.findFragmentById(R.id.book_fragment) != null) {
      getListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE)
    }
  }

  override def onAttach(activity: Activity) {
    super.onAttach(activity)
    try {
      mCallback = activity.asInstanceOf[OnBookSelectedListener]
    }
    catch {
      case e: ClassCastException => {
        throw new ClassCastException(activity.toString + " must implement OnBookSelectedListener")
      }
    }
  }

  override def onListItemClick(l: ListView, v: View, position: Int, id: Long) {
    mCallback.onBookSelected(position)
    getListView.setItemChecked(position, true)
    getActivity.getIntent.putExtra(SearchResult.BOOK_KEY,result.books.get(position % count))
  }

}


object SearchResult {
  val ARG_POSITION: String = "position"
  val BOOK_KEY="book"
  val mapping: Map[Int, String] = Map(
    R.id.bookTitle -> "title", R.id.bookAuthor -> "author", R.id.bookPublisher -> "publisher",
    R.id.ratingNum -> "numRaters", R.id.ratedStars -> "average", R.id.currentState -> "current_user_collection.status"
  )
  def drawableFromUrl(url:String,name:String) = {
    Drawable.createFromStream(new URL(url).getContent.asInstanceOf[InputStream], name)
  }
}

class SearchResultDetail extends Fragment with DoubanList{

  import SearchResult.ARG_POSITION

  private[ui] var mCurrentPosition: Int = -1

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    if (savedInstanceState != null) {
      mCurrentPosition = savedInstanceState.getInt(ARG_POSITION)
    }
    inflater.inflate(R.layout.book_view, container, false)
  }

  override def onStart {
    super.onStart
    val args: Bundle = getArguments
    if (args != null) {
      updateArticleView(args.getInt(ARG_POSITION))
    }
    else if (mCurrentPosition != -1) {
      updateArticleView(mCurrentPosition)
    }
  }

  def updateArticleView(position: Int) {
    val bookView: TextView = getActivity.findViewById(R.id.bookTitle).asInstanceOf[TextView]
    val book=getActivity.getIntent.getExtras.getSerializable(SearchResult.BOOK_KEY).asInstanceOf[Book]
    batchSetTextView(SearchResult.mapping++Map(R.id.book_author_abstract->"author_intro",R.id.book_content_abstract->"summary"),book)
    getActivity.findViewById(R.id.book_img).setBackgroundDrawable(SearchResult.drawableFromUrl(book.image,book.title))
    mCurrentPosition = position
  }

  override def onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putInt(ARG_POSITION, mCurrentPosition)
  }
}


