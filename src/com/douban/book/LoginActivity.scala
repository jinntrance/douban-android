package com.douban.book

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.view.{LayoutInflater, Menu, MenuItem}
import android.webkit.{WebView, WebViewClient}
import android.widget.ImageView
import com.douban.base.{Constant, DoubanActivity}
import com.douban.common.Auth._
import com.douban.common._
import org.scaloid.common._

class LoginActivity extends DoubanActivity {
  private[this] var refreshItem: MenuItem = null
  private[this] var iv: ImageView = null

  override def onCreate(b: Bundle) {
    super.onCreate(b)
    notifyNetworkState()
    if (!isOnline) {
      notifyNetworkState()
      finish()
    }
    if (isAuthenticated) finish()
    setContentView(R.layout.login)
    find[WebView](R.id.authView).setWebViewClient(new DoubanWebViewClient)
  }

  def refresh(i: MenuItem) {
    refreshMenuItem()
    find[WebView](R.id.authView).loadUrl(getAuthUrl(Constant.apiKey, redirectUrl = Constant.apiRedirectUrl ,scope = Constant.scope))
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
      if (redirectedUrl.startsWith(redirect_url)) {
        if (redirectedUrl.contains("error=")) toast(R.string.login_failed)
        else {
          val sp = waitToLoad(LoginActivity.this.finish(), msg = R.string.logining)
          handle({
            Auth.getTokenByCode(extractCode(redirectedUrl), Constant.apiKey, Constant.apiSecret, Constant.apiRedirectUrl)
          }, (t: Option[AccessTokenResult]) => {
            if (None == t) toast(R.string.login_failed)
            else {
              updateToken(t.get)
              longToast(R.string.login_successfully)
              restartApplication()
            }
            stopWaiting(sp)
          })
          view.stopLoading()
        }
      }
      else super.onPageStarted(view, redirectedUrl, favicon)
      refreshMenuItem()
    }

    override def onPageFinished(view: WebView, url: String) {
      super.onPageFinished(view, url)
      refreshItem.getActionView.clearAnimation()
      refreshItem.setActionView(null)
    }
  }

}

