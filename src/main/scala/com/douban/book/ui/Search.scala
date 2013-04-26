package com.douban.book
package ui

import android.os.Bundle
import android.view.{MenuItem, Menu, KeyEvent, View}
import android.widget.{ImageView, EditText}
import com.douban.models.Book

import org.scaloid.common._
import android.content.Intent
import com.douban.base.DoubanActivity
import scala.concurrent._
import scala.util.{Failure, Success}
import ExecutionContext.Implicits.global
import com.douban.book.R


/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 4/5/13 8:50 PM
 * @version 1.0
 */
class Search extends DoubanActivity{
  private val count=10
  private var searchText=""
  private var canLogout=false
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
    future {
      Book.search(searchText, "", page, this.count)
    } onComplete {
      case Success(books) => startActivity(SIntent[SearchResult])
      case Failure(error) => println(error.getMessage)
    }
  }

  def login(i:MenuItem) {
    startActivity(SIntent[LoginActivity])
  }


  override def onCreateOptionsMenu(menu: Menu) ={
    getMenuInflater.inflate(R.menu.main,menu)
    true
  }

  override def onBackPressed() {
    if(canLogout) super.onBackPressed()
    else {
      canLogout=true
      toast(R.string.press_again_to_logout)
    }
  }


  override def onActivityResult(requestCode:Int, resultCode:Int, intent:Intent) {
//    val scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent)
//    if (scanResult != null) {
      // handle scan result
//    }
    // else continue with any other code you need in the method
  }
}
