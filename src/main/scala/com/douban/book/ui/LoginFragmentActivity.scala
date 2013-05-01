package com.douban.book
package ui

import android.os.Bundle
import android.webkit.{WebView, WebViewClient}
import android.graphics.Bitmap
import org.scaloid.common._
import com.douban.base.{DoubanFragmentActivity, Constant}
import com.douban.common._
import Auth._
import android.view.{MenuItem, Menu}

class LoginFragmentActivity extends DoubanFragmentActivity {
  override def onCreate(b: Bundle) {
    super.onCreate(b)
    setContentView(R.layout.login)
    find[WebView](R.id.authView).setWebViewClient(new DoubanWebViewClient)
    refresh(null)
  }

  def refresh(i:MenuItem){
    find[WebView](R.id.authView).loadUrl(getAuthUrl(Constant.apiKey, scope = Constant.scope))
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
                updateToken(t.get)
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

