package com.douban

import scala.collection.mutable

import scala.language.implicitConversions
import scala.language.reflectiveCalls
import collection.JavaConverters._
import com.douban.common.Req
import android.content.Context
import android.view.View
import android.widget.TextView
import scala.util

/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 10/8/13 2:05 AM
 * @version 1.0
 */
package object book {

  implicit class Regex(sc: StringContext) {
    def r = new util.matching.Regex(sc.parts.mkString, sc.parts.tail.map(_ => "x"): _*)
  }

  implicit def javaList2Scala[T](l: java.util.List[T]): mutable.Buffer[T] = l match {
    case list: java.util.List[T] => l.asScala
    case _ => mutable.Buffer()
  }

  implicit def javaIterator2Scala[T](l: java.lang.Iterable[T]) = l match {
    case list: java.lang.Iterable[T] => l.asScala
    case _ => Nil
  }

  implicit def javaUtilIterator2Scala[T](l: java.util.Iterator[T]) = l match {
    case list: java.util.Iterator[T] => l.asScala
    case _ => Nil
  }

  implicit def scalaList2java[T](l: scala.List[T]): java.util.List[T] = l.asJava

  implicit def scalaBuffer2java[T](l: mutable.Buffer[T]): java.util.List[T] = l.asJava

  @inline def string2TextView(s: String)(implicit ctx: Context): View = {
    val t = new TextView(ctx)
    t.setText(s)
    t
  }
}