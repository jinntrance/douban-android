package com.douban.ui

import android.os.Bundle
import android.preference.PreferenceManager

/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 1/26/13 1:25 AM
 * @version 1.0
 */
object InitialActivity extends android.app.Application{
  val shredPref= PreferenceManager.getDefaultSharedPreferences(this)
}
sealed class InitialActivity extends android.app.Application