package com.douban.base

import android.content
import android.net.ConnectivityManager
import android.preference.PreferenceManager
import android.view.{ViewGroup, View, MenuItem}
import com.douban.book.{LoginActivity, R}
import com.douban.common._
import java.lang.Thread.UncaughtExceptionHandler
import org.scaloid.common._
import scala.concurrent._
import ExecutionContext.Implicits.global
import collection.JavaConverters._
import java.util
import scala.collection.mutable
import android.os.Bundle
import android.app._
import android.widget.{Button, ImageView, TextView, SimpleAdapter}
import android.graphics.drawable.Drawable
import java.net.URL
import java.io.{FileOutputStream, File, InputStream}
import com.douban.common.AccessTokenResult
import scala.util.Failure
import org.scaloid.common.LoggerTag
import scala.util.Success
import android.graphics.{Bitmap, BitmapFactory}
import android.content.Context
import android.telephony.TelephonyManager
import android.widget.LinearLayout.LayoutParams
import scala.language.implicitConversions
import scala.language.reflectiveCalls

/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 4/21/13 5:14 PM
 * @version 1.0
 */
trait Douban {

  type V={
    def findViewById(id:Int):View
  }

  protected val countPerPage = 12

  implicit val ctx:Context

  def getCurrentView:V

  protected lazy val rootView:V=getCurrentView

  def getThisActivity:DoubanActivity

  def batchSetTextView[T <: V](m: Map[Int, String], bean: Any, holder: T=rootView) {
    val values = beanToMap(bean)
    m.foreach {
      case (id, key) => {
        val value=values.get(key)
        if (null!=value ) {
          val view = holder.findViewById(id)
          if(null!=view && view.isInstanceOf[TextView]) view.asInstanceOf[TextView].setText(value)
        }
      }
    }
  }

  def beanToMap(b: Any, keyPre: String = ""): util.Map[String, String] =
    Req.g.toJsonTree(b).getAsJsonObject.entrySet().asScala.foldLeft(mutable.Map[String, String]()) {
      case (a, e) => {
        val key = keyPre + e.getKey
        if (e.getValue.isJsonPrimitive) a + (key -> e.getValue.getAsString)
        else if (e.getValue.isJsonArray) a + (key -> e.getValue.getAsJsonArray.iterator().asScala.filter(_.isJsonPrimitive).map(_.getAsString).mkString(", "))
        else if (e.getValue.isJsonObject) a ++ beanToMap(e.getValue, key + ".").asScala
        else a
      }
    }.asJava

  def listToMap[T](b: util.List[_ <: Any]): util.List[util.Map[String, String]] = {
    new util.ArrayList[util.Map[String, String]](Req.g.toJsonTree(b).getAsJsonArray.asScala.map(beanToMap(_)).asJavaCollection)
  }

  def string2TextView(s:String)(implicit ctx:Context):View={
    val t=new TextView(getThisActivity)
    t.setText(s)
    t
  }

   implicit def string2Button(s:String)(implicit ctx:Context):View={
    val t=new Button(getThisActivity)
    t.setText(s)
    t
  }

  implicit def javaList2Scala[T](l:java.util.List[T]):mutable.Buffer[T]=l.asScala
  implicit def scalaList2java[T](l:scala.List[T]):java.util.List[T]=l.asJava
  implicit def scalaBuffer2java[T](l:mutable.Buffer[T]):java.util.List[T]=l.asJava

  def hideWhenEmpty(m:(Int,String)){
    hideWhenEmpty(m._1,m._2)
  }

  def hideWhenEmpty(resId:Int,value:String,holder:V=rootView){
     if(null==value||value.isEmpty) {
       val v=holder.findViewById(resId)
       if(null!=v)  v.setVisibility(View.GONE)
     }
  }

  def hideWhen(resId:Int,condition:Boolean,holder:V=rootView){
     if(condition) {
       val v=holder.findViewById(resId)
       if(null!=v)  v.setVisibility(View.GONE)
     }
  }

  def displayWhen(resId:Int,condition:Boolean,holder:V=rootView)={
    val v=holder.findViewById(resId)
    if(null!=v) v.setVisibility(if(condition)View.VISIBLE else View.GONE)
  }
  /**
   *
   * @return the visible one
   */
  def toggleBetween(view1:Int,view2:Int,holder:V=rootView):View={
    val v1=holder.findViewById(view1)
    val v2=holder.findViewById(view2)
    if(v1.getVisibility==View.GONE){
      v2.setVisibility(View.GONE)
      v1.setVisibility(View.VISIBLE)
      v1
    } else {
      v1.setVisibility(View.GONE)
      v2.setVisibility(View.VISIBLE)
      v2
    }
  }
  def toggleBackGround(firstOneAsBackground:Boolean,viewId:Int,res:(Int,Int),holder:V=rootView):Boolean=toggleBackGround(firstOneAsBackground,holder.findViewById(viewId),res)


  def toggleBackGround(firstOneAsBackground:Boolean,view:View,res:(Int,Int)):Boolean={
    val chosen=if(firstOneAsBackground) res._1 else res._2
    view match {
      case img:ImageView=>img.setImageResource(chosen)
      case _=>
    }
    !firstOneAsBackground
  }
}

trait DoubanActivity extends SActivity with Douban {
  override implicit val loggerTag = LoggerTag("DoubanBook")
  Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
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

  def getThisActivity=this

  def getCurrentView:V=this

  def handle[R](result: => R, handler: (R) => Unit) {
    future {
      result
    } onComplete {
      case Success(t) => handler(t)
      case Failure(m) => debug(m.getMessage)
    }
  }

  def replaceActionBar(layoutId:Int=R.layout.header,title:String=getString(R.string.app_name)) {
    getActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM)
    getActionBar.setCustomView(layoutId)
    setWindowTitle(title)
  }

  def setWindowTitle(title: CharSequence) = find[TextView](R.id.title).setText(title)

  def setWindowTitle(title: Int) = find[TextView](R.id.title).setText(title)

  def menu(v: View) {
  }

  def put(key: String, value: Any) {
    defaultSharedPreferences.edit().putString(key, value.toString).commit()
  }

  def get(key: String) = defaultSharedPreferences.getString(key, "")

  def contains(key: String): Boolean = defaultSharedPreferences.contains(key)

  def notifyNetworkState() {
    if (!isOnline) toast(R.string.notify_offline)
  }

  def getAccessToken = {
    if (!contains(Constant.accessTokenString))
      startActivity(SIntent[LoginActivity])
    get(Constant.accessTokenString)
  }

  def back(i: MenuItem) {
    onBackPressed()
  }

  def back(v: View) {
    onBackPressed()
  }

  def isOnline = {
    val activeNetwork = getApplicationContext.getSystemService(content.Context.CONNECTIVITY_SERVICE).asInstanceOf[ConnectivityManager].getActiveNetworkInfo
    activeNetwork.isConnectedOrConnecting
  }

  def usingWIfi = {
    val activeNetwork = getApplicationContext.getSystemService(content.Context.CONNECTIVITY_SERVICE).asInstanceOf[ConnectivityManager].getActiveNetworkInfo
    activeNetwork.getType == ConnectivityManager.TYPE_WIFI
  }

  def using2G:Boolean ={
    import TelephonyManager._
    val t=getSystemService(Context.TELEPHONY_SERVICE).asInstanceOf[TelephonyManager].getNetworkType match{
      case NETWORK_TYPE_GPRS|NETWORK_TYPE_EDGE|NETWORK_TYPE_CDMA|NETWORK_TYPE_1xRTT|NETWORK_TYPE_IDEN=>"2G"
      case _=>"3G"
    }
    t=="2G"
  }

  def isAuthenticated = {
    !get(Constant.accessTokenString).isEmpty
  }

  protected def updateToken(t: AccessTokenResult) {
    put(Constant.accessTokenString, t.access_token)
    put(Constant.refreshTokenString, t.refresh_token)
    put(Constant.userIdString, t.douban_user_id)
  }

  def BitmapFromUrl(url: String) = {
    BitmapFactory.decodeStream(new URL(url).getContent.asInstanceOf[InputStream])
  }

  def loadImage[T <: {def findViewById(id : Int) : View}](url: String, imgId: Int, name: String = "", holder: T = this, updateCache: Boolean = false) {
    val cacheFile = new File(getExternalCacheDir, url.dropWhile(_ != '/'))
    if (!updateCache && cacheFile.exists()) {
      val b = Drawable.createFromPath(cacheFile.getAbsolutePath)
      runOnUiThread(holder.findViewById(imgId).asInstanceOf[ImageView].setImageDrawable(b))
    } else future {
      BitmapFromUrl(url)
    } onComplete {
      case Success(b) => {
        runOnUiThread(holder.findViewById(imgId).asInstanceOf[ImageView].setImageBitmap(b))
        if (!cacheFile.exists()&&cacheFile.getParentFile.mkdirs) {
           cacheFile.createNewFile()
        }
        val out = new FileOutputStream(cacheFile, false)
        b.compress(Bitmap.CompressFormat.JPEG, 100, out)
        out.close()
      }
      case Failure(b) => toast(getString(R.string.load_img_fail, name))
    }
  }
}

trait DoubanListFragment[T<:DoubanActivity] extends ListFragment with Douban {
  lazy implicit val loggerTag=getThisActivity.loggerTag

  def simpleAdapter(a: Activity, list: util.List[_ <: Any], itemLayout: Int, m: Map[Int, String]) = {
    new SimpleAdapter(a, listToMap(list), itemLayout, m.values.toArray, m.keys.toArray)
  }

  def getThisActivity:T=getActivity.asInstanceOf[T]

  implicit val ctx:Context=getThisActivity

  override lazy val rootView=getView

  def getCurrentView:V=getView
}

trait DoubanFragment[T<:DoubanActivity] extends Fragment with Douban{

  lazy implicit val loggerTag=getThisActivity.loggerTag

  def getThisActivity:T=getActivity.asInstanceOf[T]

  implicit val ctx:Context=getThisActivity

  def getCurrentView:V=getView

  override lazy val rootView=getView

}
