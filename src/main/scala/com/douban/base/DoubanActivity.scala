package com.douban.base

import org.scaloid.common.{LoggerTag, SIntent, SContext, SActivity}
import android.support.v4.app.FragmentActivity
import com.douban.book.ui.LoginActivity
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import android.preference.PreferenceManager

/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 4/21/13 5:14 PM
 * @version 1.0
 */
trait DoubanActivity extends FragmentActivity with SActivity with SContext{
  override implicit val tag = LoggerTag("com.douban.book")
  def getToken={
    if(get(Constant.accessTokenString).isEmpty) startActivity(SIntent[LoginActivity])
    get(Constant.accessTokenString)
  }
  def handle[R](result: => R,handler:(R) =>Unit ){
    future {
      result
    } onComplete{
      case Success(t)=>handler(t)
      case Failure(m)=>println(m.getMessage)
    }
  }
  def sharedPref=PreferenceManager.getDefaultSharedPreferences(this)

  def put(key:String,value:Any){
    sharedPref.edit().putString(key,value.toString)
  }
  def get(key:String)=sharedPref.getString(key,"")

  def contains(key:String):Boolean=sharedPref.contains(key)

  def getAccessToken= {
    if (get(Constant.accessTokenString).isEmpty)
      startActivity(SIntent[LoginActivity])
    get(Constant.accessTokenString)
  }
}
