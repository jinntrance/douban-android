package com.douban.base

import org.scaloid.common._
import android.support.v4.app.FragmentActivity
import com.douban.book.ui.LoginActivity
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import android.preference.PreferenceManager
import scala.util.Success
import scala.util.Failure
import org.scaloid.common.LoggerTag
import android.view.MenuItem
import android.net.{ConnectivityManager, NetworkInfo}
import android.content.Context
import java.lang.Thread.UncaughtExceptionHandler
import com.douban.common.{AccessTokenResult, Auth, DoubanException}
import com.douban.book.R

/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 4/21/13 5:14 PM
 * @version 1.0
 */
trait DoubanActivity extends FragmentActivity  with SContext with TraitActivity[DoubanActivity]  {
  def basis=this
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

  def handle[R](result: => R,handler:(R) =>Unit ){
    future {
      result
    } onComplete{
      case Success(t)=>handler(t)
      case Failure(m)=>debug(m.getMessage)
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
  def back(i:MenuItem) {
    onBackPressed()
  }
  def isOnline={
    val activeNetwork =getApplicationContext.getSystemService(Context.CONNECTIVITY_SERVICE).asInstanceOf[ConnectivityManager].getActiveNetworkInfo
    activeNetwork.isConnectedOrConnecting
  }
  def usingWIfi={
    val activeNetwork =getApplicationContext.getSystemService(Context.CONNECTIVITY_SERVICE).asInstanceOf[ConnectivityManager].getActiveNetworkInfo
    activeNetwork.getType==ConnectivityManager.TYPE_WIFI
  }
  protected def updateToken(t: AccessTokenResult) {
    put(Constant.accessTokenString, t.access_token)
    put(Constant.refreshTokenString, t.refresh_token)
    put(Constant.userIdString, t.douban_user_id)
  }
}
