package com.douban

import org.scalatest.FunSuite
import com.google.gson.GsonBuilder

/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 12/27/12 11:31 PM
 * @version 1.0
 */
trait BaseTest extends FunSuite {
  val gp=new GsonBuilder().setPrettyPrinting().create()
  def prettyJSON(p: Any) {
    if (p==None) println(p)
    else println(gp.toJson(p))
  }
}
