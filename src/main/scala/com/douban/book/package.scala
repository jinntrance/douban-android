package com.douban

import scala.collection.mutable

import scala.language.implicitConversions
import scala.language.reflectiveCalls
import collection.JavaConverters._
import org.scaloid.common._
import scala.concurrent._
import ExecutionContext.Implicits.global
import com.douban.common.Req
import android.content.Context
import android.view.View
import android.widget.{Button, TextView}

/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 10/8/13 2:05 AM
 * @version 1.0
 */
package object book{

  implicit def javaList2Scala[T](l: java.util.List[T]): mutable.Buffer[T] = l.asScala

  implicit def scalaList2java[T](l: scala.List[T]): java.util.List[T] = l.asJava

  implicit def scalaBuffer2java[T](l: mutable.Buffer[T]): java.util.List[T] = l.asJava

  def beanToMap[TYPE<:Any](b: TYPE, keyPre: String = "",separator:String="/"): Map[String, String]={

    def beanToMapHelper(b: Any, keyPre: String = "",separator:String="/"): mutable.Map[String, String] =
      Req.g.toJsonTree(b).getAsJsonObject.entrySet().asScala.foldLeft(mutable.Map[String, String]()) {
        case (a, e) => {
          val key = keyPre + e.getKey
          if (e.getValue.isJsonPrimitive) a + (key -> e.getValue.getAsString)
          else if (e.getValue.isJsonArray) a + (key -> e.getValue.getAsJsonArray.iterator().asScala.filter(_.isJsonPrimitive).map(_.getAsString).mkString(separator))
          else if (e.getValue.isJsonObject) a ++ beanToMapHelper(e.getValue, key + ".",separator)
          else a
        }
      }
    Map() ++ beanToMapHelper(b,keyPre,separator)
  }

  @inline def string2TextView(s: String)(implicit ctx: Context): View = {
    val t = new TextView(ctx)
    t.setText(s)
    t
  }

  implicit def string2Button(s: String)(implicit ctx: Context): View = {
    val t = new Button(ctx)
    t.setText(s)
    t
  }
}
