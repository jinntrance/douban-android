package com.douban.base

import org.scaloid.common._
import android.support.v4.app.FragmentActivity
import com.douban.book.ui.LoginActivity
import scala.concurrent._
import ExecutionContext.Implicits.global
import android.preference.PreferenceManager
import scala.util.Success
import scala.util.Failure
import org.scaloid.common.LoggerTag
import android.view.{View, MenuItem}
import android.net.ConnectivityManager
import android.content
import android.content.Intent
import java.lang.Thread.UncaughtExceptionHandler
import com.douban.common.{AccessTokenResult, Auth, DoubanException}
import android.os.Bundle
import com.douban.book.R

/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 4/21/13 5:14 PM
 * @version 1.0
 */
trait DoubanActivity extends FragmentActivity with SActivity  {
  override implicit val tag = LoggerTag("com.douban.book")
  Thread.setDefaultUncaughtExceptionHandler(new  UncaughtExceptionHandler(){
    def uncaughtException(thread: Thread, ex: Throwable) {
      if(ex.isInstanceOf[DoubanException]&&ex.asInstanceOf[DoubanException].tokenExpired){
        handle(Auth.getTokenByFresh(get(Constant.refreshTokenString),Constant.apiKey,Constant.apiSecret)
        ,(t:Option[AccessTokenResult])=>{
            if(None!=t) updateToken(t.get)
            else {
              put(Constant.accessTokenString,"")
              toast(R.string.relogin_needed)
            }
          })
      }
      toast(ex.getMessage)
      ex.printStackTrace()
    }
  })

  override def startActivity(intent: Intent) {
    super.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK))
  }

  @inline def handle[R](result: => R,handler:(R) =>Unit ){
    future {
      result
    } onComplete{
      case Success(t)=>handler(t)
      case Failure(m)=>debug(m.getMessage)
    }
  }
  @inline def sharedPref=PreferenceManager.getDefaultSharedPreferences(this)


  @inline def put(key:String,value:Any){
    sharedPref.edit().putString(key,value.toString)
  }
  @inline def get(key:String)=sharedPref.getString(key,"")

  @inline def contains(key:String):Boolean=sharedPref.contains(key)

  def getAccessToken= {
    if (get(Constant.accessTokenString).isEmpty)
      startActivity(SIntent[LoginActivity])
    get(Constant.accessTokenString)
  }
  def back(i:MenuItem) {
    onBackPressed()
  }
  def back(v:View){
    onBackPressed()
  }
  @inline def isOnline={
    val activeNetwork =getApplicationContext.getSystemService(content.Context.CONNECTIVITY_SERVICE).asInstanceOf[ConnectivityManager].getActiveNetworkInfo
    activeNetwork.isConnectedOrConnecting
  }
  @inline def usingWIfi={
    val activeNetwork =getApplicationContext.getSystemService(content.Context.CONNECTIVITY_SERVICE).asInstanceOf[ConnectivityManager].getActiveNetworkInfo
    activeNetwork.getType==ConnectivityManager.TYPE_WIFI
  }
  def othened= {!get(Constant.accessTokenString).isEmpty}
  protected def updateToken(t: AccessTokenResult) {
    put(Constant.accessTokenString, t.access_token)
    put(Constant.refreshTokenString, t.refresh_token)
    put(Constant.userIdString, t.douban_user_id)
  }
}
