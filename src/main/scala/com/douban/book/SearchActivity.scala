package com.douban.book

import android.os.{Handler, Bundle}
import android.view.{KeyEvent, View}
import android.widget.SearchView
import com.douban.models.{BookSearchResult, Book}
import org.scaloid.common._
import android.content.{DialogInterface, Intent}
import com.douban.base.{Constant, DoubanActivity}
import scala.concurrent._
import scala.util.{Failure, Success}
import ExecutionContext.Implicits.global
import com.google.zxing.integration.android.IntentIntegrator
import Constant._
import android.app.ProgressDialog

/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 4/5/13 8:50 PM
 * @version 1.0
 */
class SearchActivity extends DoubanActivity {

  private var doubleBackToExitPressedOnce = false

  protected override def onCreate(b: Bundle) {
    super.onCreate(b)
    setContentView(R.layout.search)
    find[SearchView](R.id.searchBookText) onKey (
      (v: View, k: Int, e: KeyEvent) => {
        if (k == KeyEvent.KEYCODE_ENTER) {
          search(v)
          true
        } else false
      })
  }

  override def onResume() {
    super.onResume()
    doubleBackToExitPressedOnce = false
  }

  override def onBackPressed() {
    if (doubleBackToExitPressedOnce) super.onBackPressed()
    else {
      doubleBackToExitPressedOnce = true
      longToast(R.string.double_back_to_exit)
      new Handler().postDelayed(new Runnable() {
        def run() = {
          doubleBackToExitPressedOnce = false
        }
      }, 1000)
    }
  }

  def scan(v: View) {
    new IntentIntegrator(this).initiateScan()
  }

  def search(v: View) {
    val searchText = find[SearchView](R.id.searchBookText).getQuery.toString.trim
    if (!searchText.isEmpty) startActivity(SIntent[SearchResultActivity].putExtra(SEARCH_TEXT_KEY, searchText))
  }

  override def onActivityResult(requestCode: Int, resultCode: Int, intent: Intent) {
    val scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent)
    /*    if (scannerCode == requestCode && resultCode == Activity.RESULT_OK) {
          val contents = intent.getStringExtra("SCAN_RESULT")
          val format = intent.getStringExtra("SCAN_RESULT_FORMAT")
        }*/
    if (null != scanResult) {
      info(s"scanning result ${scanResult.getContents}")
      startActivity(SIntent[BookActivity].putExtra(Constant.ISBN, scanResult.getContents))
    }
    else toast(R.string.scan_failed)
  }
}
