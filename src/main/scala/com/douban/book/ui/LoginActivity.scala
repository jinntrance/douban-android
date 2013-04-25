package com.douban.book
package ui

import android.os.Bundle
import android.webkit.{WebView, WebViewClient}
import android.graphics.Bitmap
import org.scaloid.common._
import com.douban.base.{DoubanActivity, Constant}
import com.douban.common._
import Auth._
import android.view.{MenuItem, Menu}

class LoginActivity extends DoubanActivity {
  val wv = find[WebView](R.id.authView)
  override def onCreate(b: Bundle) {
    super.onCreate(b)
    setContentView(R.layout.login)
    wv.setWebViewClient(new DoubanWebViewClient)
  }

  def refresh(i:MenuItem){
    wv.loadUrl(getAuthUrl(Constant.apiKey, scope = Constant.scope))
  }

  override def onCreateOptionsMenu(menu: Menu) = {getMenuInflater.inflate(R.menu.login,menu); true}

  class DoubanWebViewClient extends WebViewClient {
    override def onPageStarted(view: WebView, redirectedUrl: String, favicon: Bitmap) {
      if (redirectedUrl.startsWith(redirect_url)) {
        if (redirectedUrl.contains("error=")) toast(R.string.login_failed)
        else {
          toast(R.string.waiting_for_auth)
          handle( {
            Auth.getTokenByCode(extractCode(redirectedUrl), Constant.apiKey, Constant.apiSecret)
          } , (t:Option[AccessTokenResult])=>{
              if (None == t) toast(R.string.login_failed)
              else {
                put(Constant.accessTokenString, t.get.access_token)
                put(Constant.refreshTokenString, t.get.refresh_token)
                put(Constant.userIdString, t.get.douban_user_id)
                Req.init(t.get.access_token)
                toast(R.string.login_successfully)
              }
          })
          view.stopLoading()
          finish()
        }
      }
      else super.onPageStarted(view, redirectedUrl, favicon)
    }

  }
}

