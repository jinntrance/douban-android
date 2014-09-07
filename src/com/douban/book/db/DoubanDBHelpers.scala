package com.douban.book.db

import com.douban.base.DBHelper
import com.douban.models.{Annotation, Bean, Tag, AnnotationPosted}

/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 4/28/13 5:15 PM
 * @version 1.0
 * @see http://developers.douban.com/wiki/?title=api_v2
 */
class ScannerHistoryHelper(c: android.content.Context) extends DBHelper[ScannerHistory](c, "scanner_history")

case class ScannerHistory(isbn: String) extends Serializable

class SearchHistoryHelper(c: android.content.Context) extends DBHelper[SearchHistory](c, "search_history")

case class SearchHistory(search: String) extends Serializable

class AnnotationUploaderHelper(c: android.content.Context) extends DBHelper[AnnotationUploader](c, "annotations_to_post")

object AnnotationUploaderHelper{
  def apply(c: android.content.Context)=new AnnotationUploaderHelper(c)
}

case class AnnotationUploader(bookId: Long, bookTitle:String, annotation: AnnotationPosted) extends Serializable {
  def toAnnotation={
    new Annotation(0,bookId.toString,null,null,null,annotation.chapter,annotation.page,
      if("public" == annotation.privacy) 2 else 1,null,null,null,null,null,0,false,null)
  }
}

case class MyTagsHelper(implicit c: android.content.Context) extends DBHelper[Tag](c, "my_tags")