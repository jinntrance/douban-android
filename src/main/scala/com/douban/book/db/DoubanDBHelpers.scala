package com.douban.book.db

import com.douban.base.DBHelper
import com.douban.models.AnnotationPosted

/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 4/28/13 5:15 PM
 * @version 1.0
 * @see http://developers.douban.com/wiki/?title=api_v2
 */
class ScannerHistoryHelper(c: android.content.Context) extends DBHelper[ScannerHistory](c, "scanner_history")

case class ScannerHistory(isbn: String)

class SearchHistoryHelper(c: android.content.Context) extends DBHelper[SearchHistory](c, "search_history")

case class SearchHistory(search: String)

class AnnotationUploaderHelper(c: android.content.Context) extends DBHelper[AnnotationUploader](c, "annotations_to_post")

case class AnnotationUploader(bookId: String, annotation: AnnotationPosted)