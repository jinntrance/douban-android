package com.douban.book.ui

import android.support.v4.app.ListFragment
import android.widget.ListView
import android.view.{ViewGroup, LayoutInflater, View}
import android.os.Bundle
import com.douban.base.DoubanFragment

/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 4/9/13 12:25 AM
 * @version 1.0
 */
class SearchResult extends ListFragment with DoubanFragment{


  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle) = super.onCreateView(inflater, container, savedInstanceState)

  override def onListItemClick(l: ListView, v: View, position: Int, id: Long) {
    super.onListItemClick(l, v, position, id)
  }
}
