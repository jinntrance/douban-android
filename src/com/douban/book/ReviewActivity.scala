package com.douban.book

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.view.{LayoutInflater, Menu, MenuItem, Window}
import android.webkit.{WebChromeClient, WebView, WebViewClient}
import android.widget.ImageView
import com.douban.base.{Constant, DoubanActivity}
import org.scaloid.common._

/**
 * Copyright by <a href="http://www.josephjctang.com"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 2/15/14 4:47 PM
 * @version 1.0
 */
class ReviewActivity extends DoubanActivity {

  @inline
  def getReviewsUrl(bookId: String) = s"http://book.douban.com/subject/$bookId/reviews"

  var reviewsUrl = ""

  private[this] var refreshItem: MenuItem = null
  private[this] var iv: ImageView = null
  lazy val webView = find[WebView](R.id.reviews)

  override def onCreate(b: Bundle) {
    getWindow.requestFeature(Window.FEATURE_PROGRESS)
    super.onCreate(b)
    getIntent.getStringExtra(Constant.BOOK_ID) match {
      case bookId: String if bookId.nonEmpty =>
        reviewsUrl = getReviewsUrl(bookId)
        setTitle(getIntent.getExtras.getString(Constant.BOOK_TITLE, getString(R.string.review)))
      case _ =>
        longToast(R.string.books_not_found)
        this.finish()
    }
    setContentView(R.layout.reviews)
    val settings = webView.getSettings
    settings.setJavaScriptEnabled(true)
    settings.setJavaScriptCanOpenWindowsAutomatically(true)
    settings.setSupportMultipleWindows(true)
    settings.setSupportZoom(true)
    settings.setDisplayZoomControls(true)
    settings.setBuiltInZoomControls(true)
    settings.setUserAgentString("Mozilla/5.0 (Linux; U; Android 4.1.2; en-us; XT928 Build/6.7.2_GC-404) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30")
    webView.setWebViewClient(new DoubanWebViewClient)
    webView.setWebChromeClient(new WebChromeClient {
      override def onProgressChanged(view: WebView, newProgress: Int): Unit = {
        ReviewActivity.this.setProgress(newProgress)
      }
    })
  }

  def refresh(i: MenuItem) {
    refreshMenuItem()
    webView.loadUrl(reviewsUrl)
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
