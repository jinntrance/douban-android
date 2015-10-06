package com.douban.base

import com.douban.common.Auth

/**
 * Copyright by <a href="http://www.josephjctang.com"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 1/26/13 9:01 PM
 * @version 1.0
 */
object Constant {
  val COUNT_PER_PAGE = "pageCount"

  val USER_ID = "userId"

  val SYNC_IN_2G: String = "sync_in_2g"

  val ANNOTATION_POSTED = "annotationPosted"

  val expireTime: String = "expireTime"

  val ANNOTATION = "annotation"

  val SEPARATOR: String = "`"

  val COLLECTION_SEARCH = "collectionSearch"

  val BOOK_TITLE = "bookTitle"


  val ACTIVITY_NOTE_ADDITION: String = "noteAddition"

  val DATA_LIST: String = "dataList"

  val PUBLIC = "public"

  val TAGS: String = "tags"

  val FRAGMENT_FAV_BOOKS: String = "fav_books"
  val COLLECTION = "collection"
  val ACTIVITY_NOTE_VIEW: String = "noteView"
  val ACTIVITY_TAG: String = "tagAdder"
  val READING_STATUS: String = "readingStatus"
  val AVATAR: String = "avatar"
  val NOTES_NUM: String = "notesNum"
  val COLLE_NUM: String = "collectionNum"

  def USERNAME: String = "username"

  val BOOK_PAGE = "bookPage"
  val IMAGE: String = "image"
  val UPDATED = "updated"
  val apiKey = "0f86acdf44c03ade2e94069dce40b09a" //TODO change apiKey and secret every time
  val apiSecret = "95125490b60b01ee"
  val apiRedirectUrl = "http://josephjctang.com/douban-android/"
  val scope = Seq("douban_basic_common", "book_basic_r", "book_basic_w").mkString(",")
  val accessTokenString = "accessToken"
  val refreshTokenString = "refreshToken"
  val userIdString = "userId"
  val ISBN = "ISBN"
  val BOOK_ID = "BOOK_ID"
  val STATE_ID = "stateId"
  val BOOK_KEY = "book"
  val ARG_POSITION = "position"
  val BOOKS_KEY = "books"
  val SEARCH_TEXT_KEY = "searchText"

  val ACTIVITY_SEARCH = "search"
}
