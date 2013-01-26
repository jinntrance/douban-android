package com.douban.ui

import android.os.Bundle
import android.webkit.{WebViewClient, WebView}
import android.graphics.Bitmap
import android.app.Activity
import com.douban.R
import com.douban.common._
import com.douban.common.AccessToken
import com.douban.common.AuthorizationCode

/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 1/26/13 12:36 AM
 * @version 1.0
 */
class LoginActivity extends Activity{
  private var wv:WebView=null
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.auth)
    wv=findViewById(R.id.webView).asInstanceOf[WebView]
    wv.setWebViewClient(new DoubanWebViewClient)
    wv.loadUrl(AuthorizationCode().authUrl)

  }
}
class DoubanWebViewClient extends WebViewClient{
  override def onPageStarted(view: WebView, url: String, favicon: Bitmap) {
    if (url.startsWith(Auth.redirect_url)) {
      val token: Token = new AccessToken(Auth.extractCode(url), Auth.redirect_url)
      view.stopLoading()
      val url = token.tokenUrl
      val t=Req.post[AccessTokenResult](url, token) //TODO to handle the error code
      Auth.access_token=t.get.access_token  //to store the access token and the refresh token
      Auth.refresh_token=t.get.refresh_token
    }
    else super.onPageStarted(view, url, favicon)
  }
}
