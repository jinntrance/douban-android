package com.douban.book

import android.os.Bundle
import android.view.{LayoutInflater, View, ViewGroup}
import com.douban.base.{DoubanActivity, DoubanFragment}


/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 10/7/13 3:24 PM
 * @version 1.0
 */
class MenuActivity {
}

class MenuFragment extends DoubanFragment[DoubanActivity] {
  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = inflater.inflate(R.layout.menu, container, false)

}
