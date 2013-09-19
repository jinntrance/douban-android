package com.douban.book.ui

import com.douban.base.{DoubanList, DoubanActivity}
import android.os.Bundle
import com.douban.book.R
import android.widget.{TextView, AbsListView, ListView}
import android.view._
import com.douban.models.Book
import scala.concurrent._
import org.scaloid.common._
import scala.util.Success
import scala.util.Failure
import ExecutionContext.Implicits.global
import java.lang.String
import android.app.{FragmentTransaction, ListFragment, Fragment, Activity}

/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 5/3/13 5:25 PM
 * @version 1.0
 * @see http://developers.douban.com/wiki/?title=api_v2
 */

class SearchResultActivity extends DoubanActivity {
  private var currentPage = 1
  private var searchText = ""

  protected override def onCreate(b: Bundle) = {
    super.onCreate(b)
    setContentView(R.layout.book_list)
    if (findViewById(R.id.list_container) != null && null == b) {
      import SearchActivity._
      searchText = getSearchText(b)
      setTitle(getString(R.string.search_result, searchText))
      val f: Fragment = new SearchResultList()
      f.setArguments(getIntent.getExtras)
      getFragmentManager.beginTransaction().add(R.id.list_container, f).commit()
    }
  }

  override def onStart() {
    super.onStart()
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

  def onArticleSelected(position: Int) {
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

class SearchResultList extends ListFragment with DoubanList {


  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle) = inflater.inflate(R.id.list_container, container)

  private[ui] var mCallback: OnBookSelectedListener = null

  trait OnBookSelectedListener {
    def onArticleSelected(position: Int)
  }

  override def onCreate(b: Bundle) {
    super.onCreate(b)
    val result = SearchActivity.getBooks(b)
    setListAdapter(simpleAdapter(getActivity, result.books, R.layout.book_list_item, Map(
      "title" -> R.id.bookTitle, "author" -> R.id.bookAuthor, "publisher" -> R.id.bookPublisher,
      "numRaters" -> R.id.ratingNum, "average" -> R.id.ratedStars

    )))
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
    mCallback.onArticleSelected(position)
    getListView.setItemChecked(position, true)
  }

}


object SearchResult {
  val ARG_POSITION: String = "position"
}

class SearchResultDetail extends Fragment {

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
    val article: TextView = getActivity.findViewById(R.id.bookTitle).asInstanceOf[TextView]
    article.setText("the TITLE")  //TODO
    mCurrentPosition = position
  }

  override def onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putInt(ARG_POSITION, mCurrentPosition)
  }
}


