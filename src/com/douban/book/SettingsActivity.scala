package com.douban.book

import com.douban.base.DoubanActivity
import android.os.Bundle
import android.view.View
import org.scaloid.common._

/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>/
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 10/7/13 3:55 PM
 * @version 1.0
 */
class SettingsActivity extends DoubanActivity {
  override def onCreate(b: Bundle) {
    super.onCreate(b)
    setContentView(R.layout.settings)
    if (!isAuthenticated) setViewByRes(R.id.toggleLoginText, R.string.login_douban)
  }

  def toggleLogin(v: View) {
    if (isAuthenticated) {
      defaultSharedPreferences.edit().clear().commit()
      longToast(R.string.signed_out)
      setViewByRes(R.id.toggleLoginText, R.string.login_douban)
    } else {
      currentUserId
      if (isAuthenticated) setViewByRes(R.id.toggleLoginText, R.string.logout)
    }
    restartApplication()
  }

  def delCache(v: View) {
    getExternalCacheDir.delete()
    getExternalCacheDir.createNewFile()
    getCacheDir.delete()
    getCacheDir.createNewFile()
    toast(R.string.del_cache_successfully)
  }

  def about(v: View) {
    startActivity(SIntent[AboutActivity])
  }
}

class AboutActivity extends DoubanActivity {
  override def onCreate(b: Bundle) {
    super.onCreate(b)
    setContentView(R.layout.about)
  }
}
