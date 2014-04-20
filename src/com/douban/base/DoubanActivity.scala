package com.douban.base

import android.content
import android.net.{NetworkInfo, ConnectivityManager}
import android.view._
import com.douban.book._
import com.douban.common._
import java.lang.Thread.UncaughtExceptionHandler
import org.scaloid.common._
import scala.concurrent._
import ExecutionContext.Implicits.global
import android.widget._
import android.graphics.drawable.Drawable
import java.net.URL
import java.io.{IOException, FileOutputStream, File, InputStream}
import android.graphics.{Bitmap, BitmapFactory}
import android.content.{Intent, DialogInterface, Context}
import android.telephony.TelephonyManager
import scala.language.implicitConversions
import scala.language.reflectiveCalls
import android.os.Bundle
import org.scaloid.support.v4.{SFragment, SListFragment, SFragmentActivity}
import android.support.v4.app.Fragment
import android.app._
import com.douban.models.{Book, User}
import android.view.inputmethod.InputMethodManager
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu
import scala.util.Failure
import com.douban.common.AccessTokenResult
import org.scaloid.common.LoggerTag
import scala.util.Success
import android.view.ViewGroup.LayoutParams
import uk.co.senab.photoview.PhotoViewAttacher
import scala.util.control.Exception.catching
import android.content.pm.PackageManager

/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 4/21/13 5:14 PM
 * @version 1.0
 */
trait Douban {
  type V = {
    def findViewById(id: Int): View
  }

  implicit val loggerTag = LoggerTag("DoubanBook")

  protected val countPerPage = 12

  implicit def ctx: DoubanActivity = getThisActivity

  implicit val gravity = Gravity.CENTER

  protected val rootView: V

  def storeData(s: java.io.Serializable) = {
    ctx.getApplication match {
      case d: DoubanContext => d.serializableData = s
      case _ =>
    }
  }

  def fetchData: java.io.Serializable = {
    ctx.getApplication match {
      case d: DoubanContext =>
        d.serializableData
      case _ => null
    }
  }

  def getThisActivity: DoubanActivity

  def setViewValue[T <: V](id: Int, value: String, holder: T = rootView, notification: String = "", hideEmpty: Boolean = true,showNonEmpty:Boolean=false): Unit = {
    setViewValueByView(holder.findViewById(id), value, notification, hideEmpty,showNonEmpty)
  }

  def setViewByRes[T <: V](id: Int, resId: Int, holder: T = rootView, notification: String = "", hideEmpty: Boolean = true,showNonEmpty:Boolean=false): Unit = {
    setViewValueByView(holder.findViewById(id), ctx.getString(resId), notification, hideEmpty,showNonEmpty)
  }

  def setViewValueByView(v: => View, value: String, notification: String = "", hideEmpty: Boolean = true,showNonEmpty:Boolean=false): Unit = {
    value.trim match {
      case "" if hideEmpty => v match {
        case view: View => runOnUiThread(view.setVisibility(View.GONE))
        case _ =>
      }
      case value: String =>
        v match {
          case view: View if showNonEmpty => runOnUiThread(view.setVisibility(View.VISIBLE))
          case _ =>
        }
        v match {
        case view: TextView => runOnUiThread(view.setText(value))
        case rating: RatingBar => runOnUiThread(rating.setNumStars(value.toInt))
        case img: ImageView if value != "URL" => loadImage(value, img, notification)
        case _ =>
      }
    }
  }

  def batchSetValues[T <: V](m: Map[Int, Any], values: Map[String, String], holder: T = rootView, separator: String = "/",showNonEmpty:Boolean=false) {
    m.par.foreach {
      case (id, key: String) => setViewValue(id, values.getOrElse(key, ""), holder,showNonEmpty=showNonEmpty)
      case (id, (key: String, format: String)) =>
        setViewValue(id, {
          val v = values.getOrElse(key, "")
          if (v.isEmpty) "" else format.format(v)
        }, holder,showNonEmpty=showNonEmpty)
      case (id, l: List[String]) =>
        setViewValue(id, l.map(values.getOrElse(_, "")).filter(_ != "").mkString(separator), holder,showNonEmpty=showNonEmpty)
      case (id, (urlKey: String, (notifyField: String, format: String))) =>
        setViewValue(id, values.getOrElse(urlKey, "URL"), holder, format.format(values.getOrElse(notifyField, "")),showNonEmpty=showNonEmpty) //TODO add support
    }
  }

  @inline def hideWhenEmpty(m: (Int, String)) {
    hideWhenEmpty(m._1, m._2)
  }

  def hideWhenEmpty(resId: Int, value: String, holder: V = rootView) = value match {
    case null | "" => holder.findViewById(resId) match {
      case v: View => runOnUiThread(v.setVisibility(View.GONE))
      case _ =>
    }
    case _ =>
  }

  @inline def hideKeyboard() {
    ctx.getWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
  }

  @inline def displayKeyboard() {
    ctx.getWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
  }

  def hideWhen(resId: Int, condition: Boolean, holder: V = rootView) = if (condition) {
    holder.findViewById(resId) match {
      case v: View => runOnUiThread(v.setVisibility(View.GONE))
      case _ =>
    }
  }

  def toggleDisplayWhen(resId: Int, condition: Boolean, holder: V = rootView) = {
    holder.findViewById(resId) match {
      case v: View => runOnUiThread(v.setVisibility(if (condition) View.VISIBLE else View.GONE))
      case _ =>
    }
  }

  /**
   *
   * @return the visible one
   */
  def toggleBetween(view1: Int, view2: Int, holder: V = rootView): View = {
    val v1 = holder.findViewById(view1)
    val v2 = holder.findViewById(view2)
    if (v1.getVisibility == View.GONE) {
      v2.setVisibility(View.GONE)
      v1.setVisibility(View.VISIBLE)
      v1
    } else {
      v1.setVisibility(View.GONE)
      v2.setVisibility(View.VISIBLE)
      v2
    }
  }

  def toggleBackGround(firstOneAsBackground: Boolean, viewId: Int, res: (Int, Int), holder: V = rootView): Boolean =
    toggleBackGround(firstOneAsBackground, holder.findViewById(viewId), res)

  def toggleBackGround(firstOneAsBackground: Boolean, view: View, res: (Int, Int)): Boolean = {
    val chosen = if (firstOneAsBackground) res._1 else res._2
    view match {
      case img: ImageView => runOnUiThread(img.setImageResource(chosen))
      case txt: TextView => runOnUiThread(txt.setBackgroundResource(chosen))
      case _ =>
    }
    !firstOneAsBackground
  }

  def isOnline = {
    ctx.getSystemService(content.Context.CONNECTIVITY_SERVICE).asInstanceOf[ConnectivityManager].getActiveNetworkInfo match {
      case activeNetwork: NetworkInfo => activeNetwork.isConnectedOrConnecting
      case _ => false
    }

  }

  def usingWIfi = {
    ctx.getSystemService(content.Context.CONNECTIVITY_SERVICE).asInstanceOf[ConnectivityManager].getActiveNetworkInfo
    match {
      case activeNetwork: NetworkInfo => activeNetwork.getType == ConnectivityManager.TYPE_WIFI
      case _ => false
    }
  }

  def using2G: Boolean = {
    import TelephonyManager._
    ctx.getSystemService(Context.TELEPHONY_SERVICE).asInstanceOf[TelephonyManager].getNetworkType match {
      case NETWORK_TYPE_GPRS | NETWORK_TYPE_EDGE | NETWORK_TYPE_CDMA | NETWORK_TYPE_1xRTT | NETWORK_TYPE_IDEN => true
      case _ => false
    }
  }

  @inline def BitmapFromUrl(url: String) = {
    BitmapFactory.decodeStream(new URL(url).getContent.asInstanceOf[InputStream])
  }

  private def calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int = {
    val height: Int = options.outHeight
    val width: Int = options.outWidth
    var inSampleSize = 1.0f
    if (height > reqHeight || width > reqWidth) {
      val heightRatio = height / reqHeight.toFloat
      val widthRatio = width / reqWidth.toFloat
      inSampleSize = if (heightRatio <= widthRatio) heightRatio else widthRatio
    }
    if(inSampleSize<=1) 1
    else Math.round(inSampleSize)
  }

  def BitmapFromLocalFile(filePath: String, width: Int, height: Int, fillWidth: Boolean = false) = {
    val options: BitmapFactory.Options = new BitmapFactory.Options
    options.inInputShareable=true
    options.inDither=false
    options.inTempStorage=new Array[Byte](32 * 1024)
    options.inJustDecodeBounds = true
    BitmapFactory.decodeFile(filePath, options)
    // Calculate inSampleSize
    options.inSampleSize = calculateInSampleSize(options, width, height)
    // Decode bitmap with inSampleSize set
    options.inJustDecodeBounds = false
    val bm = BitmapFactory.decodeFile(filePath, options)
    if (fillWidth) {
      catching(classOf[OutOfMemoryError]).opt(
      Bitmap.createScaledBitmap(bm, width, Math.round(bm.getHeight.toFloat * width / bm.getWidth), true)
      ) match{
        case None=>bm
        case Some(bitmap)=>bitmap
      }
    }
    else bm
  }

  def loadImageWithTitle(url: String, resId: Int, title: String, holder: V = rootView, updateCache: Boolean = false): Unit
  = holder.findViewById(resId) match {
    case img: ImageView => loadImage(url, img, ctx.getString(R.string.load_img_fail, title), updateCache)
    case _ =>
  }

  def loadImage(url: String, img: ImageView, notification: String = "", updateCache: Boolean = false, fillWidth: Boolean = false): Unit = {
    val width =
      if (fillWidth) getThisActivity.screenWidth
      else if (img.getWidth > 0) img.getWidth
      else img.getDrawable match {
        case w: Drawable => w.getIntrinsicWidth
        case _ => getThisActivity.screenWidth
      }
    val height =
      if (fillWidth) getThisActivity.screenHeight
      else if (img.getHeight > 0) img.getHeight
      else img.getDrawable match {
        case w: Drawable => w.getIntrinsicHeight
        case _ => getThisActivity.screenHeight
      }
    val cacheFile = new File(ctx.getExternalCacheDir, url.dropWhile(_ != '/'))
    runOnUiThread(img.setTag(url))

    Future {
      if (updateCache || !cacheFile.exists()) {
        val b = BitmapFromUrl(url)
        if (!cacheFile.exists() && cacheFile.getParentFile.mkdirs) {
          cacheFile.createNewFile()
        }
        val out = new FileOutputStream(cacheFile, false)
        b.compress(Bitmap.CompressFormat.JPEG, 100, out)
        out.close()
        b.recycle()
      }
      BitmapFromLocalFile(cacheFile.getAbsolutePath, width, height, fillWidth)
    } onComplete {
      case Success(b) =>
        runOnUiThread{
          img.setImageBitmap(b)
        }

      case Failure(b) => toast(notification)
    }
  }

  def handle[R](result: => R, handler: (R) => Unit) {
    Future {
      result
    } onComplete {
      case Success(t) => handler(t)
      case Failure(m) => error(m.getMessage)
    }
  }

  private var _sp: ProgressDialog = null

  def waitToLoad(cancel: => Unit = {}, msg: Int = R.string.loading)(implicit ctx: Context): ProgressDialog = if (isOnline) runOnUiThread {
    _sp = spinnerDialog("", ctx.getString(msg))
    //    _sp.getWindow.requestFeature(Window.FEATURE_NO_TITLE)
    _sp.setCanceledOnTouchOutside(true)
    _sp.setCancelable(true)
    _sp.setOnCancelListener(new DialogInterface.OnCancelListener() {
      def onCancel(p1: DialogInterface) {
        cancel
        _sp.dismiss()
      }
    })
    _sp.show()
    _sp
  } else {
    notifyNetworkState()
    null
  }

  def stopWaiting(sp: ProgressDialog = null) = runOnUiThread {
    if (null != _sp) _sp.cancel()
    if (null != sp) sp.cancel()
  }

  @inline def notifyNetworkState() {
    if (!isOnline) toast(R.string.notify_offline)
  }


  def listLoader[R](toLoad: Boolean = false, result: => R = {}, success: (R) => Unit = (r: R) => {}, failed: => Unit = {},
                    unfinishable: Boolean = true) = if (toLoad) {
    val sp: ProgressDialog = if (unfinishable) waitToLoad() else waitToLoad(getThisActivity.finish())
    Future {
      result
    } onComplete {
      case Success(t) =>
        success(t)
        if (null != sp) sp.dismiss()
      case Failure(m) =>
        failed
        error(m.getMessage)
        if (null != sp) sp.dismiss()
      case _ =>
        failed
        if (null != sp) sp.dismiss()
    }
  }
  protected def isIntentUnavailable(action: String): Boolean = {
    val packageManager = getThisActivity.getPackageManager
    val intent = new Intent(action)
    packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).isEmpty
  }

}

trait DoubanActivity extends SFragmentActivity with Douban {

  def findFragment[T <: Fragment](fragmentId: Int): T = fragmentManager.findFragmentById(fragmentId) match {
    case f: Fragment => f.asInstanceOf[T]
    case _ => new Fragment().asInstanceOf[T]
  }

  def login(v: View) {
    login()
  }

  override def setTitle(title: CharSequence): Unit = runOnUiThread(super.setTitle(title))

  private var keyboardUp = true

  def toggleKeyboard(v: View) {
    val manager = getSystemService(Context.INPUT_METHOD_SERVICE).asInstanceOf[InputMethodManager]
    keyboardUp = toggleBackGround(keyboardUp, v, (R.drawable.keyboard_up, R.drawable.keyboard_down))
    manager.toggleSoftInput(0, 0)
  }

  val LOGIN_REQUEST = 12

  @inline def login() = startActivityForResult(SIntent[LoginActivity], LOGIN_REQUEST)

  protected override def onCreate(b: Bundle) {
    super.onCreate(b)
    val extras=getIntent.getExtras
    debug(extras.keySet().map(k=>(k,extras.get(k))).mkString("\n"))
    getActionBar.setDisplayHomeAsUpEnabled(true)
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
          case e: IOException => toast(R.string.notify_offline)
          case e: Throwable =>
            error(e.getStackTrace.map(_.toString).mkString("\n"))
            e.printStackTrace()
            longToast(e.getMessage)
            getThisActivity.finish()
          case _ =>
        }
        toast(ex.getMessage)
      }
    })
  }

  lazy val slidingMenu = {
    val sm = new SlidingMenu(this)
    sm.setMode(SlidingMenu.LEFT)
    sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN)
    //    sm.setShadowWidthRes(R.dimen.sliding_menu_width)
    //        sm.setShadowDrawable(R.drawable.shadow)
    sm.setBehindOffsetRes(R.dimen.sliding_menu_behind_offset)
    sm.setBehindWidthRes(R.dimen.sliding_menu_width)
    sm.setFadeDegree(0.35f)
    //    sm.setAboveOffsetRes(R.dimen.sliding_menu_above_offset)
    runOnUiThread(sm.attachToActivity(this, SlidingMenu.SLIDING_CONTENT))
    sm.setMenu(R.layout.menu)

    if (isAuthenticated) {
      sm.findViewById(R.id.menu_login).setVisibility(View.GONE)
      val userId: Long = currentUserId
      lazy val user = User.byId(userId)
      Future {
        val u = getOrElse(Constant.USERNAME, user.name)
        val a = getOrElse(Constant.AVATAR, user.large_avatar)
        val c = getOrElse(Constant.COLLE_NUM, Book.collectionsOfUser(userId).total).toInt
        val n = getOrElse(Constant.NOTES_NUM, Book.annotationsOfUser(userId).total).toInt
        (u, a, c, n)
      } onComplete {
        case Success((username, a, c, n)) =>
          setViewValue(R.id.username, username)
          setViewValue(R.id.menu_favbooks, s"${getString(R.string.favorite)}($c)", sm)
          setViewValue(R.id.menu_mynote, s"${getString(R.string.my_note)}($n)", sm)
          loadImageWithTitle(a, R.id.user_avatar, username, sm)
          put(Constant.AVATAR, a)
          put(Constant.USERNAME, username)
          put(Constant.COLLE_NUM, c)
          put(Constant.NOTES_NUM, n)
        case Failure(e) =>
          warn("can not login")
          e.printStackTrace()
        case _ =>
      }
    }
    else sm.findViewById(R.id.menu_logoned).setVisibility(View.GONE)
    sm
  }
  override implicit val ctx: DoubanActivity = this
  /*  override def startActivity(intent: Intent) {
        super.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK))
      }*/
  lazy val fragmentManager = getSupportFragmentManager

  override def getThisActivity = this

  override lazy val rootView: V = this

  def replaceActionBar(layoutId: Int = R.layout.header, title: String = getString(R.string.app_full_name)) {
    getActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM)
    getActionBar.setCustomView(layoutId)
    setWindowTitle(title)
  }

  @inline def restoreDefaultActionBar() = {
    getActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE)
  }

  def setWindowTitle(title: CharSequence) = setViewValue(R.id.title, title.toString)

  def setWindowTitle(title: Int) = setViewValue(R.id.title, title.toString)

  override def onOptionsItemSelected(item: MenuItem) = {
    item.getItemId match {
      case android.R.id.home =>
        //        NavUtils.navigateUpFromSameTask(this)
        slidingMenu.toggle()
        true
      case _ => super.onOptionsItemSelected(item)
    }
  }

  def put(key: String, value: Any) {
    val edit = defaultSharedPreferences.edit()
    value match {
      case l: List[String] => edit.putString(key, l.mkString(Constant.SEPERATOR)).commit()
      case i: Any => edit.putString(key, value.toString).commit()
    }
  }

  def currentUserId: Long = {
    if (!isAuthenticated) {
      login()
    } else if(isOnline && isExpire){
      put(Constant.accessTokenString, "")
      longToast(R.string.reauth)
      handle(Auth.getTokenByFresh(get(Constant.refreshTokenString),
        Constant.apiKey,Constant.apiSecret), (t:Option[AccessTokenResult])=> {t match {
        case Some(tk)=>
          updateToken(tk)
          longToast(R.string.reauth_successfully)
        case _=>
          longToast(R.string.relogin_needed)
      }
    })

    }
    if (isAuthenticated) get(Constant.userIdString).toLong
    else {
      notifyNetworkState()
      //finish() //TODO
      0L
    }
  }

  override def onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == LOGIN_REQUEST && resultCode == Activity.RESULT_OK && isAuthenticated) {
      super.onRestart()
    }
  }

  def currentUserIdWithoutLogin: Long = {
    get(Constant.userIdString) match {
      case l: String if isAuthenticated && l.nonEmpty => l.toLong
      case _ => 0l
    }
  }

  def get(key: String): String = defaultSharedPreferences.getString(key, null)

  def getOrElse(key: String, alt: => Any): String = defaultSharedPreferences.getAll.get(key) match {
    case v: String => v
    case _ => alt.toString
  }

  @inline def contains(key: String): Boolean = defaultSharedPreferences.contains(key) && get(key).nonEmpty

  @inline def getAccessToken = {
    if (!contains(Constant.accessTokenString))
      login()
    get(Constant.accessTokenString)
  }

  def back(i: MenuItem) {
    onBackPressed()
  }

  def back(v: View) {
    onBackPressed()
  }

  @inline def isAuthenticated = contains(Constant.accessTokenString)

  @inline def isExpire = getOrElse(Constant.expireTime, 0).toLong <= System.currentTimeMillis()

  protected def updateToken(t: AccessTokenResult) {
    put(Constant.accessTokenString, t.access_token)
    put(Constant.refreshTokenString, t.refresh_token)
    put(Constant.expireTime, t.expires_in*1000 + System.currentTimeMillis())
    put(Constant.userIdString, t.douban_user_id)
    Req.init(t.access_token)
  }

  def sideMenu(v: View) = {
    v.getId match {
      case R.id.menu_search if !getThisActivity.isInstanceOf[SearchActivity] =>
        startActivity(SIntent[SearchActivity].setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
      case R.id.menu_mynote if !getThisActivity.isInstanceOf[MyNoteActivity] =>
        startActivity(SIntent[MyNoteActivity])
      case R.id.menu_favbooks if !getThisActivity.isInstanceOf[FavoriteBooksActivity] =>
        startActivity(SIntent[FavoriteBooksActivity])
      case R.id.menu_settings if !getThisActivity.isInstanceOf[SettingsActivity] =>
        startActivity(SIntent[SettingsActivity])
      case _ =>
    }
    slidingMenu.toggle()
  }

  def screenWidth = getResources.getDisplayMetrics.widthPixels
  def screenHeight = getResources.getDisplayMetrics.heightPixels

  def popup(v: View,reLoad:Boolean):Unit = {
    v match {
      case img: ImageView =>
        val imageDialog = new Dialog(this)
        imageDialog.getWindow.requestFeature(Window.FEATURE_NO_TITLE)
        val layout = getLayoutInflater.inflate(R.layout.image_popup, null)
        val window = imageDialog.getWindow
        window.setLayout(
          screenWidth,
          LayoutParams.WRAP_CONTENT)
        val url = img.getTag.toString
        val popupImg: ImageView = layout.find[ImageView](R.id.image_popup)
        if (reLoad && url.nonEmpty && url.startsWith("http"))
          loadImage(url, popupImg, fillWidth = true)
        else
          popupImg.setImageDrawable(img.getDrawable)
        toggleBetween(R.id.image_popup,R.id.popup_progress,layout)
        imageDialog.setContentView(layout)
        imageDialog.setCancelable(true)
        imageDialog.show()
      case _ =>
    }
  }

  def popup(v: View):Unit = popup(v,reLoad = true)

  def restartApplication(delay: Int = 3000) {
    val intent = PendingIntent.getActivity(getBaseContext, 0, new Intent(getIntent), getIntent.getFlags)
    getSystemService(Context.ALARM_SERVICE) match {
      case manager: AlarmManager => manager.set(AlarmManager.RTC, System.currentTimeMillis() + delay, intent)
      case _ =>
    }
    Future {
      blocking(Thread.sleep(delay))
      System.exit(2)
    }
  }

  override def startActivity(intent: Intent) {
    super.startActivity(intent)
    overridePendingTransition(R.anim.right_to_enter, R.anim.left_to_exit)
  }

  override def startActivityForResult(intent: Intent, requestCode: Int, options: Bundle) = {
    super.startActivityForResult(intent, requestCode, options)
    overridePendingTransition(R.anim.right_to_enter, R.anim.left_to_exit)
  }

  override def startActivityForResult(intent: Intent, requestCode: Int) = {
    super.startActivityForResult(intent, requestCode)
    overridePendingTransition(R.anim.right_to_enter, R.anim.left_to_exit)
  }
}

trait DoubanListFragment[T <: DoubanActivity] extends SListFragment with Douban {

  override def getThisActivity: T = super.activity.asInstanceOf[T]

  override lazy val activity: T = getThisActivity

  override implicit lazy val ctx: DoubanActivity = activity

  override lazy val rootView: View = getView

  def addArguments(args: Bundle): Fragment = {
    this.setArguments(args)
    this
  }

  def popup(img: View) {
    img match {
      case image: ImageView => startActivity[ImagePopupActivity]
      case _ =>
    }
  }
}

trait DoubanFragment[T <: DoubanActivity] extends SFragment with Douban {

  override def getThisActivity: T = getActivity.asInstanceOf[T]

  override lazy val activity: T = getThisActivity

  override implicit lazy val ctx: DoubanActivity = activity

  override lazy val rootView: View = getView

  def addArguments(args: Bundle): Fragment = {
    this.setArguments(args)
    this
  }
}

case class DBundle(b: Bundle = new Bundle()) {
  def put[T](key: String, value: T): Bundle = {
    value match {
      case s: String => b.putString(key, s)
      case i: Int => b.putInt(key, i)
      case l: Long => b.putLong(key, l)
      case s: Serializable => b.putSerializable(key, s)
      case _ =>
    }
    b
  }

  def put(bd: Bundle): Bundle = {
    b.putAll(bd)
    b
  }
}

class ItemAdapter[B <: AnyRef](layoutId: Int, mapping: Map[Int, Any], load: => Unit = {})(implicit activity: DoubanActivity) extends BaseAdapter {
  private var total = Long.MaxValue
  private var count = 0
  private val list = new java.util.ArrayList[B]()

  def getCount: Int = count

  def getTotal = total

  override def getItem(index: Int): B = list.get(index)

  def getItems = list

  def getItemId(position: Int): Long = position

  def addResult(total: Long, loadedSize: Int, items: java.util.List[B]) {
    this.total = total
    this.count += loadedSize
    list.addAll(items)
  }

  protected def selfLoad() = load

  def replaceResult(total: Long, loadedSize: Int, items: java.util.List[B]) {
    list.clear()
    count = 0
    addResult(total, loadedSize, items)
  }

  def getView(position: Int, view: View, parent: ViewGroup): View = if (count == 0 || position >= count) null
  else {
    val convertView = if (null != view) view else activity.getLayoutInflater.inflate(layoutId, null)
    activity.batchSetValues(mapping, beanToMap(list(position)), convertView,showNonEmpty = true)
    if (count < total && position + 1 == count) {
      selfLoad()
    }
    convertView
  }
}

case class ListResult[T](total: Int, list: java.util.List[T])

trait SwipeGestureDoubanActivity extends DoubanActivity {
  lazy val detector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {

    override def onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float) = {
      val offset: Float = e2.getRawX - e1.getRawX
      val threshold: Int = (0.4 * screenWidth).toInt
      if (offset > threshold) {
        showPre()
        true
      } else if (offset < -threshold) {
        showNext()
        true
      } else false
    }

  })

  override def dispatchTouchEvent(event: MotionEvent) = {
    detector.onTouchEvent(event) || super.dispatchTouchEvent(event)
  }

  def showNext()

  def showPre()
}