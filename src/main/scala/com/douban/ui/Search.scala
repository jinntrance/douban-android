package com.douban.ui

import android.os.Bundle
import com.douban.R
import android.view.{KeyEvent, View}
import android.widget.{EditText, TextView}
import com.douban.models.Book

import org.scaloid.common._
import android.view.View.OnKeyListener


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
    find[EditText](R.id.searchBookText) setOnKeyListener(new OnKeyListener {
      def onKey(v:View , keyCode: Int, event: KeyEvent) = {if(KeyEvent.KEYCODE_ENTER==keyCode) search(null);true}
    })



  }
  def search(v:View){
    val results=Book.search(find[EditText](R.id.searchBookText).getText.toString,"",count=10)
    startActivity(SIntent[SearchResult])
  }
}
