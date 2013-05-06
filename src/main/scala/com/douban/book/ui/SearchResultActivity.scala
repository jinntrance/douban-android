package com.douban.book.ui

import com.douban.base.{DoubanList, DoubanActivity}
import android.os.Bundle
import com.douban.book.R
import android.support.v4.app._
import android.widget.{SimpleAdapter, ListView}
import android.view._
import com.douban.models.{BookSearchResult, Book}
import collection.JavaConverters._

/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 5/3/13 5:25 PM
 * @version 1.0
 * @see http://developers.douban.com/wiki/?title=api_v2
 */

class SearchResultActivity extends DoubanActivity{
  protected override def onCreate(b: Bundle) {
    super.onCreate(b)
    setContentView(R.layout.book_list)
  }
}
object SearchResultList extends ListFragment with DoubanList{


  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle) =inflater.inflate(R.id.book_list,container)

  override def onActivityCreated(b: Bundle) {
    super.onActivityCreated(b)
    val result=b.getSerializable(SearchActivity.booksKey).asInstanceOf[BookSearchResult]

    setListAdapter(simpleAdapter(getActivity,result.books,R.layout.book_list_item,Map(
    "title"->R.id.bookTitle,"author"->R.id.bookAuthor, "publisher"->R.id.bookPublisher,
      "numRaters"->R.id.ratingNum,"average"->R.id.ratedStars

    )))
  }

  override def onListItemClick(l: ListView, v: View, position: Int, id: Long) {
    super.onListItemClick(l, v, position, id)
  }
}
object SearchResultDetail extends Fragment{
  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle) = {

    super.onCreateView(inflater, container, savedInstanceState)
  }
}

