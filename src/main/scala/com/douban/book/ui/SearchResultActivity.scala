package com.douban.book.ui

import com.douban.base.DoubanActivity
import android.os.Bundle
import com.douban.book.R
import android.support.v4.app.{FragmentActivity, ListFragment}
import android.widget.ListView
import android.view.View

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
    val books=b.getSerializable("books")
    setContentView(R.layout.search)
  }
}
class SearchResultFragment extends ListFragment{

  override def onActivityCreated(savedInstanceState: Bundle) {
    super.onActivityCreated(savedInstanceState)
    getListView.setTextFilterEnabled(true)
  }

  override def onListItemClick(l: ListView, v: View, position: Int, id: Long) {
    super.onListItemClick(l, v, position, id)
  }
}

