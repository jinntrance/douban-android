package com.douban.book
package ui

import org.scaloid.common._
import android.os.Bundle
import android.widget.Button
import android.content.Intent
import com.douban.base.Constant
import com.douban.models.Book

/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 4/10/13 2:19 PM
 * @version 1.0
 * @see http://developers.douban.com/wiki/?title=api_v2
 */

class BookActivity extends SActivity{
  protected override def onCreate(b: Bundle) {
    super.onCreate(b)
   val book:Book= b match {
      case x if !b.getString(Constant.ISBN).isEmpty=>Book.byISBN(b.getString(Constant.ISBN))
      case x if !b.getString(Constant.BOOK_ID).isEmpty=>Book.byId(b.getString(Constant.BOOK_ID).toLong)
      case _=>null
    }
    find[Button](R.id.shareButton) onClick (
      startActivity(SIntent(Intent.ACTION_SEND_MULTIPLE).setType("*/*").putExtra(Intent.EXTRA_TEXT,"").putExtra(Intent.EXTRA_STREAM,""))
      )
  }
}
