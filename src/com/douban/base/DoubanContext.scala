package com.douban.base

import android.preference.PreferenceManager
import android.view.ViewConfiguration
import com.douban.common.Req
import com.google.zxing.client.android.PreferencesActivity

import scala.util.control.Exception.catching

/**
 * Copyright by <a href="http://www.josephjctang.com"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 4/25/13 10:52 PM
 * @version 1.0
 */
class DoubanContext extends android.app.Application {
  var serializableData: java.io.Serializable = null

  override def onCreate() {
    val pref = PreferenceManager.getDefaultSharedPreferences(this)
    //silent the beep when scanning a book
    pref.edit().putBoolean(PreferencesActivity.KEY_PLAY_BEEP, false).putBoolean(PreferencesActivity.KEY_VIBRATE, true).commit()
    val token = pref.getString(Constant.accessTokenString, "")
    if (!token.isEmpty) Req.init(token)
    val config: ViewConfiguration = ViewConfiguration.get(this)
    //always show the more action in actionBar
    catching(classOf[NoSuchFieldException]).opt(
      classOf[ViewConfiguration].getDeclaredField("sHasPermanentMenuKey")) match {
      case Some(field) =>
        field.setAccessible(true)
        field.setBoolean(config, false)
      case _ =>
    }
  }
}
