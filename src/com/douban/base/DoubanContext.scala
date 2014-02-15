package com.douban.base

import com.douban.common.Req
import android.preference.PreferenceManager
import android.view.ViewConfiguration
import java.lang.reflect.Field

/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 4/25/13 10:52 PM
 * @version 1.0
 */
class DoubanContext extends android.app.Application {
  var serializableData:java.io.Serializable=null
  override def onCreate() {
    val token = PreferenceManager.getDefaultSharedPreferences(this).getString(Constant.accessTokenString, "")
    if (!token.isEmpty) Req.init(token)
    val config: ViewConfiguration = ViewConfiguration.get(this)
    val menuKeyField: Field = classOf[ViewConfiguration].getDeclaredField("sHasPermanentMenuKey")
    if (menuKeyField != null) {
      menuKeyField.setAccessible(true)
      menuKeyField.setBoolean(config, false)
    }
  }
}
