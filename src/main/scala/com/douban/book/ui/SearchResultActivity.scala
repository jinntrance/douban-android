package com.douban.book.ui

import com.douban.base.DoubanFragmentActivity
import android.os.Bundle
import com.douban.book.R

/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 5/3/13 5:25 PM
 * @version 1.0
 * @see http://developers.douban.com/wiki/?title=api_v2
 */

class SearchResultActivity extends DoubanFragmentActivity{
  protected override def onCreate(b: Bundle) {
    super.onCreate(b)
    setContentView(R.layout.book_list)
    val books=b.getSerializable("books")
    setContentView(R.layout.search)
  }
}
