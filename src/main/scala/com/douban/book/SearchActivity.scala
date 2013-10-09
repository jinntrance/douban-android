package com.douban.book

import android.os.{Handler, Bundle}
import android.view.{WindowManager, View}
import android.widget.SearchView
import org.scaloid.common._
import android.content.Intent
import com.douban.base.{Constant, DoubanActivity}
import com.google.zxing.integration.android.{IntentResult, IntentIntegrator}
import Constant._
import android.app.Activity

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
    find[SearchView](R.id.searchBookText) setOnQueryTextListener new SearchView.OnQueryTextListener() {
      def onQueryTextSubmit(p1: String): Boolean = search(p1)

      def onQueryTextChange(p1: String): Boolean = true
    }
    slidingMenu
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
    new IntentIntegrator(this).initiateScan(IntentIntegrator.ONE_D_CODE_TYPES)
  }

  def search(v: View) {
    search(find[SearchView](R.id.searchBookText).getQuery.toString.trim)
  }

  def search(txt: String) = {
    if (txt.nonEmpty) {
      hideKeyboard()
      startActivity(SIntent[SearchResultActivity].putExtra(SEARCH_TEXT_KEY, txt))
    }
    txt.isEmpty
  }

  override def onActivityResult(requestCode: Int, resultCode: Int, intent: Intent) {
/*        if (scannerCode == requestCode && resultCode == Activity.RESULT_OK) {
          val contents = intent.getStringExtra("SCAN_RESULT")
          val format = intent.getStringExtra("SCAN_RESULT_FORMAT")
        }*/
    IntentIntegrator.parseActivityResult(requestCode, resultCode, intent) match {
      case s: IntentResult => {
        info(s"scanning result ${s.getContents}")
        startActivity(SIntent[BookActivity].putExtra(Constant.ISBN, s.getContents))
      }
      case _ => toast(R.string.scan_failed)
    }
  }
}
