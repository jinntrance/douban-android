package com.douban.book

import java.io.File

import android.widget.ToggleButton
import com.douban.base.{Constant, DoubanActivity}
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
    def delHelper(dir:File) = dir match {
      case f:File if f.exists()=>
        f.delete()
        f.mkdirs()
      case _=>
    }
    delHelper(getExternalCacheDir)
    delHelper(getCacheDir)
    toast(R.string.del_cache_successfully)
  }

  def about(v: View) {
    startActivity(SIntent[AboutActivity])
  }

  def toggleSyncIn2G(v:View) {
    v match {
      case b:ToggleButton=>
        defaultSharedPreferences.edit().putBoolean(
          Constant.SYNC_IN_2G,b.getText==getResources.getString(R.string.toggle_turn_on)).commit()
      case _=>
    }
  }
  def viewDraft(v:View) {
    startActivity(SIntent[NotesDraftActivity])
  }
}

class AboutActivity extends DoubanActivity {
  override def onCreate(b: Bundle) {
    super.onCreate(b)
    setContentView(R.layout.about)
  }
}
