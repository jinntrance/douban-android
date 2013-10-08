package com.douban.book

import com.douban.base.{Constant, DoubanActivity}
import android.os.Bundle
import android.view.View
import org.scaloid.common._
import com.douban.common.AccessTokenResult

/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 10/7/13 3:55 PM
 * @version 1.0
 */
class SettingsActivity extends DoubanActivity{
  override def onCreate(b: Bundle) {
     super.onCreate(b)
     setContentView(R.layout.settings)
     if(!isAuthenticated) setViewValue(R.id.toggleLoginText,"登录豆瓣")
  }
  def toggleLogin(v:View){
     if(isAuthenticated) {
       defaultSharedPreferences.edit().clear().commit()
       toast("成功注销")
     } else getAccessToken
  }
  def delCache(v:View){
    getExternalCacheDir.delete()
    getExternalCacheDir.createNewFile()
    toast("删除缓存成功")
  }
  def about(v:View){
    startActivity(SIntent[AboutActivity])
  }
}
class AboutActivity extends DoubanActivity {
  override def onCreate(b: Bundle) {
    super.onCreate(b)
    setContentView(R.layout.about)
  }
}
