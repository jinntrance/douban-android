package com.douban.book

import android.os.{Handler, Bundle}
import android.view.{KeyEvent, View}
import android.widget.EditText
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
  private var searchText = ""
  private val scannerCode = 0
  private var doubleBackToExitPressedOnce = false

  protected override def onCreate(b: Bundle) {
    super.onCreate(b)
    setContentView(R.layout.search)
    find[EditText](R.id.searchBookText) onKey (
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
      }, 2000)
    }
  }

  def scan(v: View) {
    new IntentIntegrator(this).initiateScan()
  }

  def search(v: View) {
    var pd: ProgressDialog = null
    var canceled = false
    searchText = find[EditText](R.id.searchBookText).getText.toString.trim()
    if (!searchText.isEmpty) {
      runOnUiThread(pd = ProgressDialog.show(this, getString(R.string.search), getString(R.string.searching), false, true, new DialogInterface.OnCancelListener() {
        def onCancel(p1: DialogInterface) {
          canceled = true
        }
      }))
      future {
        Book.search(searchText, "", count = this.count)
      } onComplete {
        case Success(books) => {
          if (books.total == 0) toast(R.string.search_no_result)
          else {
            debug("search result total:" + books.total)
            if (!canceled) startActivity(SIntent[SearchResultActivity].putExtra(BOOKS_KEY, books).putExtra(SEARCH_TEXT_KEY, searchText))
          }
          pd.cancel()
        }
        case Failure(err) => {
          error(err.getMessage)
        }
      }
    }
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

object SearchActivity {
  def setBooks(i: Intent, b: BookSearchResult) = {
    i.putExtra(BOOKS_KEY, b)
  }

  def getBooks(b: Bundle) = {
    b.getSerializable(BOOKS_KEY).asInstanceOf[BookSearchResult]
  }

  def setSearchText(i: Intent, t: String) = {
    i.putExtra(SEARCH_TEXT_KEY, t)
  }

  def getSearchText(b: Bundle) = {
    b.getString(SEARCH_TEXT_KEY)
  }
}