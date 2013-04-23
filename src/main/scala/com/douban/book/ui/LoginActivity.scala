package com.douban.book
package ui

import android.os.Bundle
import android.webkit.{WebView, WebViewClient}
import android.graphics.Bitmap
import org.scaloid.common._
import com.douban.base.{DoubanActivity, Constant}
import com.douban.common._
import Auth._

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
        if (redirectedUrl.contains("error=")) toast(R.string.loginFailed)
        else {
          handle( {
            Auth.getTokenByCode(extractCode(redirectedUrl), Constant.apiKey, Constant.apiSecret)
          } , (t:Option[AccessTokenResult])=>{
              if (None == t) toast(R.string.loginFailed)
              else {
                put(Constant.accessTokenString, t.get.access_token)
                put(Constant.refreshTokenString, t.get.refresh_token)
                put(Constant.userIdString, t.get.douban_user_id)
                view.stopLoading()
              }
          })
        }
      }
      else super.onPageStarted(view, redirectedUrl, favicon)
    }

  }
}

