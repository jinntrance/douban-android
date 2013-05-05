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
import android.app.Activity


/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 4/5/13 8:50 PM
 * @version 1.0
 */
class SearchActivity extends DoubanActivity {
  private val count = 10
  private var searchText = ""
  private val waitTime=2000
  private var lastTouchTime=0l
  private val scannerCode = 0

  protected override def onCreate(b: Bundle) {
    super.onCreate(b)
    setContentView(R.layout.search)
    find[EditText](R.id.searchBookText) onKey (
      (v: View, k: Int, e: KeyEvent) => {
        k match {
          case KeyEvent.KEYCODE_ENTER=> search(v)
          case KeyEvent.KEYCODE_BACK=>onBackPressed()
        }
        true
      }
      )
    find[ImageView](R.id.scanISBN) onClick (
      startActivityForResult(SIntent("com.google.zxing.client.android.SCAN").putExtra("SCAN_MODE", "ONE_D_MODE,QR_CODE_MODE"), scannerCode)
      )
  }

  def search(v: View) {
    searchText = find[EditText](R.id.searchBookText).getText.toString
    search(1)
  }

  def search(page: Int) {
    future {
      Book.search(searchText, "", page, this.count)
    } onComplete {
      case Success(books) => startActivity(SIntent[SearchResultFragment].putExtra("books", books))
      case Failure(error) => println(error.getMessage)
    }
  }

  def login(i: MenuItem) {
    startActivity(SIntent[LoginActivity])
  }

  def about(i: MenuItem) {
    startActivity(SIntent[AboutActivity])
  }

  override def onCreateOptionsMenu(menu: Menu) = {
    getMenuInflater.inflate(R.menu.main, menu)
    if(isAuthenticated) menu.findItem(R.id.login).setVisible(false)
    true
  }

  override def onBackPressed() {
    val currentTime=System.currentTimeMillis
    if (currentTime-lastTouchTime<waitTime) finish()
    else {
      toast(R.string.press_again_to_logout)
      lastTouchTime=currentTime
    }
  }


  override def onActivityResult(requestCode: Int, resultCode: Int, intent: Intent) {
    if (scannerCode == requestCode && resultCode == Activity.RESULT_OK) {
      val contents = intent.getStringExtra("SCAN_RESULT")
      val format = intent.getStringExtra("SCAN_RESULT_FORMAT")
    }

  }
}
