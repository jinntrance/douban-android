package com.douban.base

import org.scaloid.common.{SIntent, SContext, SActivity}
import android.support.v4.app.{FragmentActivity, ActivityCompat}
import com.douban.book.ui.LoginActivity
import scala.concurrent._
import scala.util.Success

/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 4/21/13 5:14 PM
 * @version 1.0
 */
trait DoubanActivity extends FragmentActivity with SActivity with SContext{
  def getToken={
    if(Context.get(Constant.accessTokenString).isEmpty) startActivity(SIntent[LoginActivity])
    Context.get(Constant.accessTokenString)
  }
}
