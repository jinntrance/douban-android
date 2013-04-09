package com.douban.ui

import android.os.Bundle
import com.douban.R
import android.view.{KeyEvent, View}
import android.widget.{EditText, TextView}
import com.douban.models.Book

import org.scaloid.common._


/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 4/5/13 8:50 PM
 * @version 1.0
 */
class Search extends SActivity{
  private val page=10
  protected override def onCreate(b: Bundle) {
    super.onCreate(b)
    setContentView(R.layout.search)
    find[EditText](R.id.searchBookText) onKey (
      (v:View,k:Int,e:KeyEvent)=> {if(k==KeyEvent.KEYCODE_ENTER) search(v); true}
    )
  }
  def search(v:View){
    val results=Book.search(find[EditText](R.id.searchBookText).getText.toString,"",count=10)
    startActivity(SIntent[SearchResult])
  }
}
