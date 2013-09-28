package com.douban.book.ui

import com.douban.base.{DoubanList, DoubanActivity}
import android.os.Bundle
import com.douban.book.R
import android.widget._
import android.view._
import com.douban.models.Book
import scala.concurrent._
import org.scaloid.common._
import ExecutionContext.Implicits.global
import java.lang.String
import android.app.{ListFragment, Fragment, Activity}
import SearchActivity._
import com.douban.models.BookSearchResult
import scala.util.Failure
import scala.util.Success
import android.content.Context

/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 5/3/13 5:25 PM
 * @version 1.0
 * @see http://developers.douban.com/wiki/?title=api_v2
 */

class SearchResultActivity extends DoubanActivity with OnBookSelectedListener{
  private var searchText = ""

  protected override def onCreate(b: Bundle) = {
    super.onCreate(b)
    setContentView(R.layout.book_list)
    searchText = getSearchText(getIntent.getExtras)
    setTitle(getString(R.string.search_result, searchText))
    if (null== b) {
      if(findViewById(R.id.list_container)!=null){
      val f: Fragment = new SearchResultList()
      f.setArguments(getIntent.getExtras)
      getFragmentManager.beginTransaction().add(R.id.list_container, f).commit()
      }
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
      getFragmentManager.beginTransaction.add(R.id.list_container, newFragment).addToBackStack("books").commit
    }
  }
}

trait OnBookSelectedListener {
  def onBookSelected(position: Int)
}

class SearchResultList extends ListFragment with DoubanList {

  var books:java.util.List[Book]=null
  var adapter:SimpleAdapter=null
  var footer:View=null
  private var currentPage = 1
  private var result:BookSearchResult=null

  private[ui] var mCallback: OnBookSelectedListener = null

  override def onCreate(b: Bundle) {
    super.onCreate(b)
    result = SearchActivity.getBooks(getActivity.getIntent.getExtras)
    books=result.books
    adapter=new BookItemAdapter(getActivity,listToMap(books),R.layout.book_list_item,SearchResult.mapping.values.toArray,SearchResult.mapping.keys.toArray)
    setListAdapter(adapter)
  }

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, bundle: Bundle) = {
    val r=super.onCreateView(inflater,container,bundle)
    footer=inflater.inflate(R.layout.book_list_loader,container,true)
    r
  }

  override def onActivityCreated(bundle: Bundle) {
    super.onActivityCreated(bundle)
    if(null!=footer){
      getListView.addFooterView(footer)
//      updateFooter()
    }
  }

  def updateFooter() {
    getActivity.findViewById(R.id.to_load).asInstanceOf[TextView].setText(getString(R.string.swipe_up_to_load, new Integer(books.size()), new Integer(result.total)))
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

  def load(v: View) {
    if(currentPage*this.count<result.total) future {
      toast(R.string.loading)
      currentPage+=1
      Book.search(getSearchText(getActivity.getIntent.getExtras), "", currentPage , this.count)
    } onComplete {
      case Success(b) => {
          books.addAll(b.books)
          adapter.notifyDataSetChanged()
//          updateFooter()
      }
      case Failure(err) => sys.error(err.getMessage)
    }
  }

  override def onListItemClick(l: ListView, v: View, position: Int, id: Long) {
    mCallback.onBookSelected(position)
    getListView.setItemChecked(position, true)
    getActivity.getIntent.putExtra(SearchResult.BOOK_KEY,result.books.get(position % count))
  }

  class BookItemAdapter(context: Context,data: java.util.List[_ <: java.util.Map[String, _]],resource: Int,from: Array[String],to: Array[Int]) extends SimpleAdapter(context,data,resource,from,to){
    override def getView(position: Int, view: View, parent: ViewGroup): View = {
      val convertView=super.getView(position,view,parent)
      if(null!=convertView){
      val b=books.get(position)
      if(null!=b.current_user_collection) {
        convertView.find[LinearLayout](R.id.status_layout).removeView(convertView.find[ImageView](R.id.wish))
        convertView.find[TextView](R.id.currentState).setText(b.current_user_collection.status match{
          case "wish"=>"想读"
          case "reading"=>"在读"
          case "read"=>"读过"
          case _=>""
        })
      }

      getThisActivity.loadImage(b.image,R.id.book_img,b.title,convertView)
      }
      convertView
    }
  }

}




object SearchResult {
  val ARG_POSITION: String = "position"
  val BOOK_KEY="book"
  val mapping: Map[Int, String] = Map(
    R.id.bookTitle -> "title", R.id.bookAuthor -> "author", R.id.bookPublisher -> "publisher",
    R.id.ratingNum -> "rating.numRaters", R.id.ratedStars -> "rating.average",
    R.id.currentState -> "current_user_collection.status"
  )
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

  override def onStart() {
    super.onStart()
    val args: Bundle = getArguments
    if (args != null) {
      updateArticleView(args.getInt(ARG_POSITION))
    }
    else if (mCurrentPosition != -1) {
      updateArticleView(mCurrentPosition)
    }
  }

  def updateArticleView(position: Int) {
    val book=getActivity.getIntent.getExtras.getSerializable(SearchResult.BOOK_KEY).asInstanceOf[Book]
    getActivity.setTitle(book.title)
    find[LinearLayout](R.id.status_layout).removeView(find[Button](if(null==book.current_user_collection) R.id.delete
      else book.current_user_collection.status match {
      case "read"=>R.id.done
      case "reading"=> R.id.reading
      case "wish"=>R.id.toRead
      case _=>R.id.delete
    }))
    batchSetTextView(SearchResult.mapping++Map(R.id.bookPublishYear->"pubdate",R.id.bookPages->"pages",R.id.bookPrice->"price",
      R.id.book_author_abstract->"author_intro",R.id.book_content_abstract->"summary"),book)
    getThisActivity.loadImage(if(getThisActivity.usingWIfi) book.images.large else book.image,R.id.book_img,book.title)
    mCurrentPosition = position
  }

  override def onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putInt(ARG_POSITION, mCurrentPosition)
  }
}


