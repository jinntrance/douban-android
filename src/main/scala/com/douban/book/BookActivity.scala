package com.douban.book

import org.scaloid.common._
import android.os.Bundle
import android.widget.{ImageView, TextView, LinearLayout}
import com.douban.base.{DoubanFragment, DoubanActivity, Constant}
import com.douban.models.Book
import android.app.AlertDialog
import android.view._
import Constant._
import collection.JavaConverters._
import scala.concurrent._
import ExecutionContext.Implicits.global
import android.content.DialogInterface

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
  var catalogCollapsed = true
  var fragment:SearchResultDetail=null

  protected override def onCreate(b: Bundle) {
    super.onCreate(b)
    setContentView(R.layout.book_view_container)
    val extras=getIntent.getExtras
    val bk = SearchResult.selectedBook
    book = if(null!=book) book else extras match {
      case x if !extras.getString(Constant.ISBN).isEmpty => Book.byISBN(b.getString(Constant.ISBN))
      case x if !extras.getString(Constant.BOOK_ID).isEmpty => Book.byId(b.getString(Constant.BOOK_ID).toLong)
      case x if null != SearchResult.selectedBook => SearchResult.selectedBook
      case _ => {
        this.finish()
        null
      }
    }
    fragment = new SearchResultDetail()
    getFragmentManager.beginTransaction().replace(R.id.book_view_id, fragment).commit()
    //    find[Button](R.id.shareButton) onClick (
    //      startActivity(SIntent(Intent.ACTION_SEND_MULTIPLE).setType("*/*").putExtra(Intent.EXTRA_TEXT,"").putExtra(Intent.EXTRA_STREAM,""))
    //      )
  }

  override def onCreateOptionsMenu(menu: Menu) = {
    getMenuInflater.inflate(R.menu.add_note, menu)
    super.onCreateOptionsMenu(menu)
  }

  def collect(view: View) {
    getIntent.putExtra(STATE_ID,view.getId)
    startActivity(SIntent[CollectionActivity].putExtras(getIntent))
  }

  def deCollect(v:View){
    new AlertDialog.Builder(this)
      .setTitle("删除收藏")
      .setMessage("之前的短评将会消失，确定要删除收藏么？")
      .setPositiveButton("删除", new DialogInterface.OnClickListener {
      def onClick(dialog: DialogInterface, which: Int) {
        future{
          if(Book.deleteCollection(book.id)) {
            toast(R.string.decollect_successfully)
            book.updateCollection(null)
            fragment.updateBookView()
          } else toast(R.string.decollect_unsuccessfully)
        }
      }
    }).setNegativeButton("取消", new DialogInterface.OnClickListener {
      def onClick(dialog: DialogInterface, which: Int) {
        dialog.cancel()
      }
    }).show()
  }

  def addNote(m: MenuItem) = {
    startActivity(SIntent[AddNoteActivity].putExtra(Constant.BOOK_ID, book.id))
  }

  def toggleAuthor(v: View) = {
    toggleBetween(R.id.book_author_abstract,R.id.book_author_abstract_longtext)
    authorCollapsed=toggleBackGround(authorCollapsed,R.id.author_arrow,(android.R.drawable.arrow_up_float,android.R.drawable.arrow_down_float))
  }

  def toggleContent(v: View) = {
    toggleBetween(R.id.book_content_abstract,R.id.book_content_abstract_longtext)
    contentCollapsed=toggleBackGround(contentCollapsed,R.id.content_arrow,(android.R.drawable.arrow_up_float,android.R.drawable.arrow_down_float))
  }
  def toggleCatalog(v: View) = {
    toggleBetween(R.id.book_catalog_abstract,R.id.book_catalog_abstract_longtext)
    catalogCollapsed=toggleBackGround(catalogCollapsed,R.id.catalog_arrow,(android.R.drawable.arrow_up_float,android.R.drawable.arrow_down_float))
  }

  def viewNotes(v:View)={
    startActivity(SIntent[CollectionActivity].putExtra(Constant.BOOK_ID,book.id))
  }

}

class SearchResultDetail extends DoubanFragment[BookActivity] {
  var book: Book = null

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, b: Bundle) = inflater.inflate(R.layout.book_view, container, false)

  override def onActivityCreated(b:Bundle){
   super.onActivityCreated(b)
   if(null==b) updateBookView()
 }

  def updateBookView() {
    if (null != getThisActivity.book) {
      book = getThisActivity.book
      getActivity.setTitle(book.title)
      Map(R.id.subtitle_layout->book.subtitle,R.id.book_author->book.author_intro,R.id.book_content->book.summary,R.id.book_catalog->book.catalog
      ).foreach(hideWhenEmpty)

      val toDel = if (null == book.current_user_collection) {
        rootView.find[LinearLayout](R.id.book_layout_tags).setVisibility(View.GONE)
        List(R.id.delete)
      }
      else {
        val container=rootView.find[LinearLayout](R.id.tags_container)
        val tags=book.current_user_collection.tags
        if(null!=tags&&0<tags.size())  container.addView(string2TextView(tags.asScala.mkString(" ")))
        val r=Array("很差","较差","还行","推荐","力荐")
        val rat=book.current_user_collection.rating
        if(null!=rat) {
          val rating=rat.value.toInt
          val txt=if(rating>0 && rating<=5) rating+"星"+r(rating-1) else ""
          rootView.find[TextView](R.id.recommend).setText(txt)
        }
        book.current_user_collection.status match {
        case "read" => List(R.id.reading, R.id.wish)
        case "reading" => List(R.id.read, R.id.wish)
        case "wish" => List(R.id.reading, R.id.read)
        case _ => List(R.id.delete)
      }}
      val l = rootView.find[LinearLayout](R.id.status_layout)
      toDel.foreach(id => l.removeView(rootView.findViewById(id)))

      batchSetTextView(SearchResult.mapping ++ Map(
        R.id.bookTranslators->"translator",R.id.bookSubtitle->"subtitle",R.id.bookPublishYear -> "pubdate", R.id.bookPages -> "pages", R.id.bookPrice -> "price",
        R.id.book_author_abstract -> "author_intro", R.id.book_author_abstract_longtext -> "author_intro",
        R.id.book_content_abstract -> "summary", R.id.book_content_abstract_longtext -> "summary",
        R.id.book_catalog_abstract -> "catalog",R.id.book_catalog_abstract_longtext -> "catalog",
        R.id.comment->"current_user_collection.comment"), book)
      rootView.find[TextView](R.id.ratingNum).setText(s"(${book.rating.numRaters})")
      getThisActivity.loadImage(if (getThisActivity.usingWIfi|| !getThisActivity.using2G) book.images.large else book.image, R.id.book_img, book.title)
      val a=rootView.find[TextView](R.id.book_content_abstract)
      val al=rootView.find[TextView](R.id.book_content_abstract_longtext)
      if(a.getLineHeight > al.getLineHeight)
        rootView.find[ImageView](R.id.content_arrow).setVisibility(View.GONE)
      if(rootView.find[TextView](R.id.book_author_abstract).getLineHeight > rootView.find[TextView](R.id.book_author_abstract_longtext).getLineHeight)
        rootView.find[ImageView](R.id.author_arrow).setVisibility(View.GONE)
    }
  }
}

