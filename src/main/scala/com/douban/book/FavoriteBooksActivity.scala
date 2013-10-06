package com.douban.book

import com.douban.base.{DoubanListFragment, DoubanActivity}
import android.view.{View, ViewGroup, LayoutInflater}
import android.os.Bundle

/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 10/7/13 1:25 AM
 * @version 1.0
 */
class FavoriteBooksActivity extends DoubanActivity{
}


class FavoriteBooksListFragment extends DoubanListFragment[DoubanActivity]{
  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = inflater.inflate(R.layout.fav_books_item,container)

  override def onActivityCreated(savedInstanceState: Bundle){

  }
}