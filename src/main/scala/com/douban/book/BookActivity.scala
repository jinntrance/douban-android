package com.douban.book

import org.scaloid.common._
import android.os.Bundle
import android.widget.{ImageView, TextView, LinearLayout}
import com.douban.base.{DoubanList, DoubanActivity, Constant}
import com.douban.models.Book
import android.app.Fragment
import android.view._
import Constant._

/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 4/10/13 2:19 PM
 * @version 1.0
 * @see http://developers.douban.com/wiki/?title=api_v2
 */
class BookActivity extends DoubanActivity {
  var book: Book = null
  var contentCollapsed = true
  var authorCollapsed = true

  protected override def onCreate(b: Bundle) {
    super.onCreate(b)
    val bk = getIntent.getExtras.getSerializable(Constant.BOOK_KEY)
    book = b match {
      case x if null != bk => bk.asInstanceOf[Book]
      case x if !b.getString(Constant.ISBN).isEmpty => Book.byISBN(b.getString(Constant.ISBN))
      case x if !b.getString(Constant.BOOK_ID).isEmpty => Book.byId(b.getString(Constant.BOOK_ID).toLong)
      case _ => null
    }
    val fragment = new SearchResultDetail()
    getIntent.getExtras.putSerializable(BOOK_KEY, book)
    getFragmentManager.beginTransaction().replace(R.id.book_view, fragment).commit()
    //    find[Button](R.id.shareButton) onClick (
    //      startActivity(SIntent(Intent.ACTION_SEND_MULTIPLE).setType("*/*").putExtra(Intent.EXTRA_TEXT,"").putExtra(Intent.EXTRA_STREAM,""))
    //      )
  }

  override def onCreateOptionsMenu(menu: Menu) = {
    getMenuInflater.inflate(R.menu.add_note, menu)
    super.onCreateOptionsMenu(menu)
  }

  def collect(view: View) {
    startActivity(SIntent[CollectionActivity].putExtra(BOOK_KEY, getIntent.getExtras.getSerializable(BOOK_KEY)))
  }

  def addNote(m: MenuItem) = {
    startActivity(SIntent[AddNoteActivity].putExtra(Constant.BOOK_ID, book.id))
  }

  def toggleAuthor(v: View) = {
    v match {
      case img: ImageView => if (authorCollapsed) {
        find[TextView](R.id.book_author_abstract).setVisibility(View.GONE)
        find[TextView](R.id.book_author_abstract_longtext).setVisibility(View.VISIBLE)
        img.setImageResource(android.R.drawable.arrow_up_float)
        authorCollapsed = false
      } else {
        img.setImageResource(android.R.drawable.arrow_down_float)
        find[TextView](R.id.book_author_abstract).setVisibility(View.VISIBLE)
        find[TextView](R.id.book_author_abstract_longtext).setVisibility(View.GONE)
        authorCollapsed = true
      }
    }
  }

  def toggleContent(v: View) = {
    v match {
      case img: ImageView => if (contentCollapsed) {
        find[TextView](R.id.book_content_abstract).setVisibility(View.GONE)
        find[TextView](R.id.book_content_abstract_longtext).setVisibility(View.VISIBLE)
        img.setImageResource(android.R.drawable.arrow_up_float)
        contentCollapsed = false
      } else {
        img.setImageResource(android.R.drawable.arrow_down_float)
        find[TextView](R.id.book_content_abstract).setVisibility(View.VISIBLE)
        find[TextView](R.id.book_content_abstract_longtext).setVisibility(View.GONE)
        contentCollapsed = true
      }
    }
  }
}

class SearchResultDetail extends Fragment with DoubanList {
  var book: Book = null

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    inflater.inflate(R.layout.book_view, container, false)
  }

  override def onStart() {
    super.onStart()
    updateBookView()
  }

  def updateBookView() {
    val bk = getActivity.getIntent.getExtras.getSerializable(BOOK_KEY)
    if (null != bk) {
      book = bk.asInstanceOf[Book]
      getActivity.setTitle(book.title)
      val toDel = if (null == book.current_user_collection) List(R.id.delete)
      else book.current_user_collection.status match {
        case "read" => List(R.id.reading, R.id.wish)
        case "reading" => List(R.id.read, R.id.wish)
        case "wish" => List(R.id.reading, R.id.read)
        case _ => List(R.id.delete)
      }
      val l = getView.find[LinearLayout](R.id.status_layout)
      toDel.foreach(id => l.removeView(getView.findViewById(id)))

      batchSetTextView(SearchResult.mapping ++ Map(R.id.bookPublishYear -> "pubdate", R.id.bookPages -> "pages", R.id.bookPrice -> "price",
        R.id.book_author_abstract -> "author_intro", R.id.book_author_abstract_longtext -> "author_intro",
        R.id.book_content_abstract -> "summary", R.id.book_content_abstract_longtext -> "summary"), book)
      getView.find[TextView](R.id.ratingNum).setText(s"(${book.rating.numRaters})")
      getThisActivity.loadImage(if (getThisActivity.usingWIfi) book.images.large else book.image, R.id.book_img, book.title)
    }
  }
}

