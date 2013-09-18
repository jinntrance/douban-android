package com.douban.book.ui

import com.douban.base.{DoubanList, DoubanActivity}
import android.os.Bundle
import com.douban.book.R
import android.widget.ListView
import android.view._
import com.douban.models.Book
import android.app.{Fragment, ListFragment}
import scala.concurrent._
import org.scaloid.common._
import scala.util.Success
import scala.util.Failure
import ExecutionContext.Implicits.global

/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 5/3/13 5:25 PM
 * @version 1.0
 * @see http://developers.douban.com/wiki/?title=api_v2
 */

class SearchResultActivity extends DoubanActivity{
  private var currentPage = 1
  private var searchText = ""
  protected override def onCreate(b: Bundle) {
    super.onCreate(b)
    import SearchActivity._
    searchText=getSearchText(b)
    setContentView(R.layout.book_list)
    setWindowTitle(getString(R.string.search_result,searchText))
  }

  override def onStart() {
    super.onStart()
    val t=getFragmentManager.beginTransaction()
    val l=new SearchResultList()
    t.add(R.id.list_container,l)
    t.commit()
  }

  def load(v:View){
    future {
      toast(R.string.loading)
      Book.search(searchText, "", currentPage, this.count)
    } onComplete {
      case Success(books) => {

      }
      case Failure(err) => sys.error(err.getMessage)
    }
  }

}
class SearchResultList extends ListFragment with DoubanList{


  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle) =inflater.inflate(R.id.list_container,container)

  override def onActivityCreated(b: Bundle) {
    super.onActivityCreated(b)
    val result=SearchActivity.getBooks(b)
    setListAdapter(simpleAdapter(getActivity,result.books,R.layout.book_list_item,Map(
    "title"->R.id.bookTitle,"author"->R.id.bookAuthor, "publisher"->R.id.bookPublisher,
      "numRaters"->R.id.ratingNum,"average"->R.id.ratedStars

    )))
  }

  override def onListItemClick(l: ListView, v: View, position: Int, id: Long) {
    super.onListItemClick(l, v, position, id)
  }
}
class SearchResultDetail extends Fragment{
  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle) = {

    super.onCreateView(inflater, container, savedInstanceState)
  }
}


