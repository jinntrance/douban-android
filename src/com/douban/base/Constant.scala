package com.douban.base

import com.douban.common.Auth

/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 1/26/13 9:01 PM
 * @version 1.0
 */
object Constant {
  var ANNOTATION_IMAGES_NUMBER="noteImageNumber"

  var ACTIVITY_NOTE_ADDITION: String="noteAddition"

  var ANNOTATION_ID="noteId"
  var ANNOTATION_CHAPTER="noteChapter"
  var ANNOTATION_CONTENT="noteCotent"

  var DATA_LIST: String="dataList"

  def PUBLIC="public"

  var TAGS: String="tags"

  val FRAGMENT_FAV_BOOKS: String="fav_books"
  val COLLECTION="collection"
  val ACTIVITY_NOTE_VIEW: String="noteView"
  val ACTIVITY_TAG: String="tagAdder"
  val READING_STATUS: String="readingStatus"
  val AVATAR: String="avatar"
  val NOTES_NUM: String="notesNum"
  val COLLE_NUM: String="collectionNum"

  def USERNAME: String="username"

  val BOOK_PAGE="bookPage"
  val IMAGE: String="image"
  var UPDATED="updated"
  val apiKey = Auth.api_key
  val apiSecret = "95125490b60b01ee"
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

  val ACTIVITY_SEARCH="search"
}
