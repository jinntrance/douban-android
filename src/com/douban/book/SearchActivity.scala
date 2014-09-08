package com.douban.book

import android.os.{Handler, Bundle}
import android.view.View
import android.widget.SearchView
import com.douban.book.db.AnnotationUploaderHelper
import com.douban.models.Book
import org.scaloid.common._
import android.content.Intent
import com.douban.base.{Constant, DoubanActivity}
import Constant._
import android.app.Activity
import com.google.zxing.client.android.Intents
import com.google.zxing.client.android.Intents.Scan

/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 4/5/13 8:50 PM
 * @version 1.0
 */
class SearchActivity extends DoubanActivity {

  private val scanRequestCode = 1

  protected override def onCreate(b: Bundle) {
    super.onCreate(b)
    setContentView(R.layout.search)
    findViewById(R.id.searchBookText) match {
      case s: SearchView => s setOnQueryTextListener new SearchView.OnQueryTextListener() {
        def onQueryTextSubmit(p1: String): Boolean = search(p1)

        def onQueryTextChange(p1: String): Boolean = true
      }
      case _ =>
    }
    handle({
      val dbHelper = AnnotationUploaderHelper(this.ctx)
      dbHelper.findData(Int.MaxValue).foldLeft(0)((acc,a)=>{
        if(Book.postAnnotation(a.bookId,a.annotation).isDefined){
          acc+1
        } else acc
      })
    },(postedNum:Int)=>{
        if(postedNum>0)
          toast(getResources.getString(R.string.draft_posted,postedNum))
    })
    slidingMenu
  }

  override def onBackPressed() {
    doubleBackToExit()
  }

  def scan(v: View) {
    startActivityForResult(new Intent(Intents.Scan.ACTION).putExtra(Scan.MODE, Scan.ONE_D_MODE)
      , scanRequestCode)
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
    super.onActivityResult(requestCode, resultCode, intent)
    if (scanRequestCode == requestCode && resultCode == Activity.RESULT_OK) {
      val contents = intent.getStringExtra(Scan.RESULT)
      val format = intent.getStringExtra(Scan.RESULT_FORMAT)
      info(s"scanning result $contents and $format")
      startActivity(SIntent[BookActivity].putExtra(Constant.ISBN, contents))
    }
  }
}
