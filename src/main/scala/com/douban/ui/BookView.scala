package com.douban
package ui

import org.scaloid.common.{SIntent, SActivity}
import android.os.Bundle
import android.widget.Button
import android.content.Intent

/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 4/10/13 2:19 PM
 * @version 1.0
 * @see http://developers.douban.com/wiki/?title=api_v2
 */

class BookView extends SActivity{
  protected override def onCreate(b: Bundle) {
    super.onCreate(b)
    find[Button](R.id.shareButton) onClick (
      startActivity(SIntent(Intent.ACTION_SEND_MULTIPLE).setType("*/*").putExtra(Intent.EXTRA_TEXT,"").putExtra(Intent.EXTRA_STREAM,""))
      )
  }
}
