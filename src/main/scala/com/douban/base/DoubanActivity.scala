package com.douban.base

import android.content
import android.net.ConnectivityManager
import android.preference.PreferenceManager
import android.view.{View, MenuItem}
import com.douban.book.{TR, R}
import com.douban.book.ui.LoginActivity
import com.douban.common._
import java.lang.Thread.UncaughtExceptionHandler
import org.scaloid.common._
import scala.concurrent._
import scala.util.Failure
import scala.util.Success
import ExecutionContext.Implicits.global
import collection.JavaConverters._
import java.util
import scala.collection.mutable
import android.os.Bundle
import android.app._
import android.widget.{TextView, SimpleAdapter}
import com.douban.common.AccessTokenResult
import scala.util.Failure
import org.scaloid.common.LoggerTag
import scala.util.Success

/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 4/21/13 5:14 PM
 * @version 1.0
 */

trait Douban{
  protected val count = 10
}

trait DoubanActivity extends SActivity with Douban {
  override implicit val loggerTag = LoggerTag("DoubanBook")

  Thread.setDefaultUncaughtExceptionHandler(new  UncaughtExceptionHandler(){
    def uncaughtException(thread: Thread, ex: Throwable) {
      ex match {
        case exception: DoubanException if exception.tokenExpired =>
          handle(Auth.getTokenByFresh(get(Constant.refreshTokenString), Constant.apiKey, Constant.apiSecret)
            , (t: Option[AccessTokenResult]) => {
              if (None != t) updateToken(t.get)
              else {
                put(Constant.accessTokenString, "")
                toast(R.string.relogin_needed)
              }
            })
        case _ =>
      }
      toast(ex.getMessage)
      ex.printStackTrace()
    }
  })

/*  override def startActivity(intent: Intent) {
    super.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK))
  }*/

  override def getFragmentManager: FragmentManager = super.getFragmentManager

  def handle[R](result: => R,handler:(R) =>Unit ){
    future {
      result
    } onComplete{
      case Success(t)=>handler(t)
      case Failure(m)=>debug(m.getMessage)
    }
  }


  protected def replaceActionBar(b: Bundle) {
    super.onCreate(b)
    getActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM)
    getActionBar.setCustomView(R.layout.title_banner)
    setWindowTitle(R.string.app_name)
  }

  def setWindowTitle(title:CharSequence)= find[TextView](R.id.title).setText(title)
  def setWindowTitle(title:Int)= find[TextView](R.id.title).setText(title)

  def menu(v:View){

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
  def isAuthenticated= {!get(Constant.accessTokenString).isEmpty}
  protected def updateToken(t: AccessTokenResult) {
    put(Constant.accessTokenString, t.access_token)
    put(Constant.refreshTokenString, t.refresh_token)
    put(Constant.userIdString, t.douban_user_id)
  }
}
trait DoubanList extends Fragment with Douban{
  def simpleAdapter(a:Activity,list:util.List[_<:Any],itemLayout:Int,m:Map[Int,String])={
    new SimpleAdapter(a,listToMap(list),itemLayout,m.values.toArray,m.keys.toArray)
  }
  def beanToMap(b:Any):util.Map[String,Any]={
    Req.g.toJsonTree(b).getAsJsonObject.entrySet().asScala.foldLeft(mutable.Map[String,Any]()){
      case (a,e)=>
      if (e.getValue.isJsonPrimitive) a + (e.getKey -> e.getValue.getAsString)
      else  if (e.getValue.isJsonArray)  a+(e.getKey -> e.getValue.getAsJsonArray.iterator().asScala.mkString(","))
      else if (e.getValue.isJsonObject)  a++ beanToMap(e.getValue).asScala
      else a
    }.asJava
  }
  def listToMap[T](b:util.List[_<:Any]):util.List[util.Map[String,Any]]={
    Req.g.toJsonTree(b).getAsJsonArray.asScala.map(beanToMap).toList.asJava
  }
  def batchSetTextView(m:Map[Int,String],bean:Any)={
    val values=beanToMap(bean)
    m.foreach{case (id,key)=>{
      val view=getActivity.findViewById(id)
      if(null!=view) view.asInstanceOf[TextView].setText(values.get(key).toString)
    }}
  }
}
