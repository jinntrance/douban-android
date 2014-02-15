package com.douban.book

import com.douban.base.{Constant, DoubanActivity}
import android.view.{LayoutInflater, Menu, MenuItem}
import android.widget.ImageView
import android.os.Bundle
import android.webkit.{WebViewClient, WebView}
import android.view.animation.AnimationUtils
import android.content.Context
import android.graphics.Bitmap

/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 2/15/14 4:47 PM
 * @version 1.0
 */
class ReviewActivity extends DoubanActivity{

  @inline
  def getReviewsUrl(bookId:String)=s"http://book.douban.com/subject/$bookId/reviews"

  var reviewsUrl=""

  private[this] var refreshItem: MenuItem = null
  private[this] var iv: ImageView = null

  override def onCreate(b: Bundle) {
    super.onCreate(b)
    getIntent.getStringExtra(Constant.BOOK_ID) match{
      case bookId:String if bookId.nonEmpty=>
         reviewsUrl=getReviewsUrl(bookId)
         setTitle(getIntent.getExtras.getString(Constant.BOOK_TITLE,getString(R.string.review)))
      case _ =>
        toast("无该图书")
        this.finish()
    }
    setContentView(R.layout.reviews)
    find[WebView](R.id.reviews).setWebViewClient(new DoubanWebViewClient)
  }

  def refresh(i: MenuItem) {
    refreshMenuItem()
    find[WebView](R.id.reviews).loadUrl(reviewsUrl)
  }

  private def refreshMenuItem() {
    iv.startAnimation(AnimationUtils.loadAnimation(this, R.anim.refresh))
    refreshItem.setActionView(iv)
  }

  override def onCreateOptionsMenu(menu: Menu) = {
    getMenuInflater.inflate(R.menu.login, menu)
    refreshItem = menu.findItem(R.id.menu_refresh)
    iv = getSystemService(Context.LAYOUT_INFLATER_SERVICE).asInstanceOf[LayoutInflater].inflate(R.layout.refresh, null).asInstanceOf[ImageView]
    refresh(refreshItem)
    super.onCreateOptionsMenu(menu)
  }

  private class DoubanWebViewClient extends WebViewClient {
    override def onPageStarted(view: WebView, redirectedUrl: String, favicon: Bitmap) {
      super.onPageStarted(view, redirectedUrl, favicon)
      refreshMenuItem()
    }

    override def onPageFinished(view: WebView, url: String) {
      super.onPageFinished(view, url)
      refreshItem.getActionView.clearAnimation()
      refreshItem.setActionView(null)
    }
  }


}
