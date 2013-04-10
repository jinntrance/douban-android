package com.douban
package ui

import android.os.Bundle
import com.douban.R
import android.view.{KeyEvent, View}
import android.widget.{ImageView, EditText, TextView}
import com.douban.models.Book

import org.scaloid.common._
import com.google.zxing.integration.android.IntentIntegrator
import android.content.Intent


/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 4/5/13 8:50 PM
 * @version 1.0
 */
class Search extends SActivity{
  private val count=10
  private var searchText=""
  protected override def onCreate(b: Bundle) {
    super.onCreate(b)
    setContentView(R.layout.search)
    find[EditText](R.id.searchBookText) onKey (
      (v:View,k:Int,e:KeyEvent)=> {if(k==KeyEvent.KEYCODE_ENTER) search(v); true}
    )
    find[ImageView](R.id.scanISBN) onClick(
       startActivity(SIntent("com.google.zxing.client.android.SCAN").putExtra("SCAN_MODE", "ONE_D_MODE,QR_CODE_MODE"))
      )
  }
  def search(v:View){
    searchText=find[EditText](R.id.searchBookText).getText.toString
    search(1)
  }
  def search(page:Int){
    val results=Book.search(searchText,"",page,this.count)
    startActivity(SIntent[SearchResult])
  }

  override def onActivityResult(requestCode:Int, resultCode:Int, intent:Intent) {
    val scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent)
    if (scanResult != null) {
      // handle scan result
    }
    // else continue with any other code you need in the method
  }
}
