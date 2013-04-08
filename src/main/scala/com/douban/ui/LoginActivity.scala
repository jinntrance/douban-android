package com.douban
package ui

import android.os.Bundle
import android.webkit.{WebViewClient, WebView}
import android.graphics.Bitmap
import com.douban.R
import org.scaloid.common._
import com.douban.base.{Context, Constant}
import com.douban.common._
import Auth._

class LoginActivity extends SActivity with SContext{

  override def onCreate(b:Bundle) {
    super.onCreate(b)
    setContentView(R.layout.login)
    val wv=find[WebView](R.id.authView)
    wv.setWebViewClient(new DoubanWebViewClient)
    wv.loadUrl(new AuthorizationCode authUrl)
  }
  class DoubanWebViewClient extends WebViewClient {
    override def onPageStarted(view: WebView, redirectedUrl: String, favicon: Bitmap) {
      if (redirectedUrl.startsWith(redirect_url)) {
        if(redirectedUrl.contains("error=")) LoginActivity.this.notify(R.string.loginFailed)
        else {
        val token:Token = new AccessToken(extractCode(redirectedUrl),redirect_url)
        val t=Req.post[AccessTokenResult](token.tokenUrl, token)
        if(None==t) LoginActivity.this.notify(R.string.loginFailed)
        else {
        Auth.access_token=t.get.access_token  //to store the access token and the refresh token
        Auth.refresh_token=t.get.refresh_token
        Context.put(Constant.accessTokenString,t.get.access_token)
        Context.put(Constant.refreshTokenString,t.get.refresh_token)
        Context.put(Constant.userIdString,t.get.douban_user_id)
        view.stopLoading()
}  }}
      else super.onPageStarted(view, redirectedUrl, favicon)
    }

  }
  def notify(id:Int){
    toast(id)
    }
}

