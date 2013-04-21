package com.douban.book
package ui

import android.os.Bundle
import android.webkit.{WebViewClient, WebView}
import android.graphics.Bitmap
import org.scaloid.common._
import com.douban.base.{DoubanActivity, Context, Constant}
import com.douban.common._
import Auth._
import scala.concurrent._
import scala.util.Success
import ExecutionContext.Implicits.global

class LoginActivity extends DoubanActivity {

  override def onCreate(b: Bundle) {
    super.onCreate(b)
    setContentView(R.layout.login)
    val wv = find[WebView](R.id.authView)
    wv.setWebViewClient(new DoubanWebViewClient)
    wv.loadUrl(getAuthUrl(Constant.apiKey, scope = Constant.scope))
  }

  class DoubanWebViewClient extends WebViewClient {
    override def onPageStarted(view: WebView, redirectedUrl: String, favicon: Bitmap) {
      if (redirectedUrl.startsWith(redirect_url)) {
        if (redirectedUrl.contains("error=")) LoginActivity.this.notify(R.string.loginFailed)
        else {
          future {
            Auth.getTokenByCode(extractCode(redirectedUrl), Constant.apiKey, Constant.apiSecret)
          } onComplete {
            case Success(t) =>
              if (None == t) LoginActivity.this.notify(R.string.loginFailed)
              else {
                Context.put(Constant.accessTokenString, t.get.access_token)
                Context.put(Constant.refreshTokenString, t.get.refresh_token)
                Context.put(Constant.userIdString, t.get.douban_user_id)
                view.stopLoading()
              }
          }
        }
      }
      else super.onPageStarted(view, redirectedUrl, favicon)
    }

  }

  def notify(id: Int) {
    toast(id)
  }
}

