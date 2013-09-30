package com.douban.base

import android.content
import android.net.ConnectivityManager
import android.preference.PreferenceManager
import android.view.{View, MenuItem}
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
import android.widget.{ImageView, TextView, SimpleAdapter}
import android.graphics.drawable.Drawable
import java.net.URL
import java.io.{FileOutputStream, File, InputStream}
import com.douban.common.AccessTokenResult
import scala.util.Failure
import org.scaloid.common.LoggerTag
import scala.util.Success
import android.graphics.{Bitmap, BitmapFactory}

/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 4/21/13 5:14 PM
 * @version 1.0
 */
trait Douban {
  protected val count = 10

  def batchSetTextView[T <: View](m: Map[Int, String], bean: Any, holder: T) {
    val values = beanToMap(bean)
    m.foreach {
      case (id, key) => {
        val view = holder.findViewById(id).asInstanceOf[TextView]
        if (null != view) view.setText(values.get(key).toString)
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
    Req.g.toJsonTree(b).getAsJsonArray.asScala.map(beanToMap(_)).toList.asJava
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

  def handle[R](result: => R, handler: (R) => Unit) {
    future {
      result
    } onComplete {
      case Success(t) => handler(t)
      case Failure(m) => debug(m.getMessage)
    }
  }

  protected def replaceActionBar(b: Bundle) {
    super.onCreate(b)
    getActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM)
    getActionBar.setCustomView(R.layout.title_banner)
    setWindowTitle(R.string.app_name)
  }

  def setWindowTitle(title: CharSequence) = find[TextView](R.id.title).setText(title)

  def setWindowTitle(title: Int) = find[TextView](R.id.title).setText(title)

  def menu(v: View) {
  }

  @inline def sharedPref = PreferenceManager.getDefaultSharedPreferences(this)

  @inline def put(key: String, value: Any) {
    sharedPref.edit().putString(key, value.toString)
  }

  @inline def get(key: String) = sharedPref.getString(key, "")

  @inline def contains(key: String): Boolean = sharedPref.contains(key)

  def notifyNetworkState() {
    if (!isOnline) toast(R.string.notify_offline)
  }

  def getAccessToken = {
    if (get(Constant.accessTokenString).isEmpty)
      startActivity(SIntent[LoginActivity])
    get(Constant.accessTokenString)
  }

  def back(i: MenuItem) {
    onBackPressed()
  }

  def back(v: View) {
    onBackPressed()
  }

  @inline def isOnline = {
    val activeNetwork = getApplicationContext.getSystemService(content.Context.CONNECTIVITY_SERVICE).asInstanceOf[ConnectivityManager].getActiveNetworkInfo
    activeNetwork.isConnectedOrConnecting
  }

  def usingWIfi = {
    val activeNetwork = getApplicationContext.getSystemService(content.Context.CONNECTIVITY_SERVICE).asInstanceOf[ConnectivityManager].getActiveNetworkInfo
    activeNetwork.getType == ConnectivityManager.TYPE_WIFI
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
    val cacheFile = new File(getCacheDir, url.dropWhile(_ != '/'))
    if (!updateCache && cacheFile.exists()) {
      val b = Drawable.createFromPath(cacheFile.getAbsolutePath)
      runOnUiThread(holder.findViewById(imgId).asInstanceOf[ImageView].setImageDrawable(b))
    } else future {
      BitmapFromUrl(url)
    } onComplete {
      case Success(b) => {
        runOnUiThread(holder.findViewById(imgId).asInstanceOf[ImageView].setImageBitmap(b))
        if (!cacheFile.exists()) cacheFile.createNewFile()
        val out = new FileOutputStream(cacheFile, false)
        b.compress(Bitmap.CompressFormat.JPEG, 100, out)
        out.flush()
        out.close()
      }
      case Failure(b) => toast(getString(R.string.load_img_fail, name))
    }
  }
}

trait DoubanList extends Fragment with Douban {
  implicit val ctx = this.getActivity

  def simpleAdapter(a: Activity, list: util.List[_ <: Any], itemLayout: Int, m: Map[Int, String]) = {
    new SimpleAdapter(a, listToMap(list), itemLayout, m.values.toArray, m.keys.toArray)
  }

  def batchSetTextView(m: Map[Int, String], bean: Any) {
    super.batchSetTextView(m, bean, getView)
  }

  def getThisActivity = getActivity.asInstanceOf[DoubanActivity]
}
