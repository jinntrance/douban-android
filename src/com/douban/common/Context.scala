package com.douban.common

import android.os.Bundle
import android.preference.PreferenceManager

/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 1/26/13 1:25 AM
 * @version 1.0
 */
object Context extends android.app.Application{
  def sharedPref=PreferenceManager.getDefaultSharedPreferences(this)
  

  def put(key:String,value:Any){
     sharedPref.edit().putString(key,value.toString)
  }
  def get(key:String)=sharedPref.getString(key,"")

  def contains(key:String):Boolean=sharedPref.contains(key)
}
sealed class Context extends android.app.Application