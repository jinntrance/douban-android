package com.douban.book

import org.scaloid.common._
import android.os.Bundle
import android.widget.{TextView, LinearLayout}
import com.douban.base.{DoubanFragment, DoubanActivity, Constant}
import com.douban.models.{ReviewRating, Collection, Book}
import android.app.{Activity, AlertDialog}
import android.view._
import Constant._
import scala.concurrent._
import ExecutionContext.Implicits.global
import android.content.{Intent, DialogInterface}
import scala.util.Success

/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 4/10/13 2:19 PM
 * @version 1.0
 * @see http://developers.douban.com/wiki/?title=api_v2
 */
class BookActivity extends DoubanActivity {
  val COLLECTION_MODIFICATION_REQUEST=1
  var book: Option[Book] = None
  var contentCollapsed = true
  var authorCollapsed = true
  var catalogCollapsed = true
  lazy val fragment: SearchResultDetail = findFragment[SearchResultDetail](R.id.book_view_fragment)

  protected override def onCreate(b: Bundle) {
    super.onCreate(b)
    getIntent.getExtras match {
      case extras: Bundle => {
        val isbn = extras.getString(Constant.ISBN)
        val bookId = extras.getString(Constant.BOOK_ID)
        val bk = extras.getSerializable(Constant.BOOK_KEY)
        val sp=waitToLoad()
        if (null==bk) future{
          if (null != isbn && !isbn.isEmpty) Some(Book.byISBN(isbn))
          else if (null!=bookId&&bookId.nonEmpty) Some(Book.byId(bookId.toLong))
          else None
        }onComplete {
          case Success(Some(bb:Book))=>{
            book=Some(bb)
            fragment.updateBookView()
            sp.dismiss()
          }
          case _=>{
            toast(R.string.search_no_result)
            finish()
          }
        }
        else  book=bk.asInstanceOf[Option[Book]]
      }
      case _ =>
    }
    setContentView(R.layout.book_view_container)
    slidingMenu
    //    find[Button](R.id.shareButton) onClick (
    //      startActivity(SIntent(Intent.ACTION_SEND_MULTIPLE).setType("*/*").putExtra(Intent.EXTRA_TEXT,"").putExtra(Intent.EXTRA_STREAM,""))
    //      )
  }

  override def onCreateOptionsMenu(menu: Menu) = {
    getMenuInflater.inflate(R.menu.add_note, menu)
    super.onCreateOptionsMenu(menu)
  }

  def collect(view: View) {
    getIntent.putExtra(STATE_ID, view.getId)
    startActivityForResult(SIntent[CollectionActivity].putExtras(getIntent),COLLECTION_MODIFICATION_REQUEST)
  }

  override def onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
    if(resultCode==Activity.RESULT_OK&&requestCode==COLLECTION_MODIFICATION_REQUEST) {
      data.getSerializableExtra(Constant.COLLECTION) match{
        case c:Collection=>book.get.updateCollection(c)
        case _=>
      }
    }
  }

  def deCollect(v: View) {
    new AlertDialogBuilder("删除收藏","之前的短评将会消失，确定要删除收藏么？"){
        positiveButton(onClick={future{
            book match {
              case Some(b: Book) if Book.deleteCollection(b.id) => {
                toast(R.string.decollect_successfully)
                b.updateCollection(book.get.updateCollection(null))
                fragment.updateBookView()
              }
              case _ => toast(R.string.decollect_unsuccessfully)
            }
          }})
    }.show()
  }

  def addNote(m: MenuItem) = {
    startActivity(SIntent[AddNoteActivity].putExtra(Constant.BOOK_ID, book.get.id))
  }

  def toggleAuthor(v: View) = {
    toggleBetween(R.id.book_author_abstract, R.id.book_author_abstract_longtext)
    authorCollapsed = toggleBackGround(authorCollapsed, R.id.author_arrow, (android.R.drawable.arrow_up_float, android.R.drawable.arrow_down_float))
  }

  def toggleContent(v: View) = {
    toggleBetween(R.id.book_content_abstract, R.id.book_content_abstract_longtext)
    contentCollapsed = toggleBackGround(contentCollapsed, R.id.content_arrow, (android.R.drawable.arrow_up_float, android.R.drawable.arrow_down_float))
  }

  def toggleCatalog(v: View) = {
    toggleBetween(R.id.book_catalog_abstract, R.id.book_catalog_abstract_longtext)
    catalogCollapsed = toggleBackGround(catalogCollapsed, R.id.catalog_arrow, (android.R.drawable.arrow_up_float, android.R.drawable.arrow_down_float))
  }

  def viewNotes(v: View) = {
    startActivity(SIntent[NotesActivity].putExtra(Constant.BOOK_ID, book.get.id))
  }
}

class SearchResultDetail extends DoubanFragment[BookActivity] {
  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, b: Bundle) = {
    inflater.inflate(R.layout.book_view, container, false)
  }

  override def onActivityCreated(b: Bundle){
    super.onActivityCreated(b)
    updateBookView()
  }

  def updateBookView() {
    getThisActivity.book match {
      case Some(book) => {
        getActivity.setTitle(book.title)
        Map(R.id.subtitle_layout -> book.subtitle, R.id.book_author -> book.author_intro, R.id.book_content -> book.summary, R.id.book_catalog -> book.catalog
        ).foreach(hideWhenEmpty)

        val toDel = book.current_user_collection match {
          case c: Collection => {
            getView.find[TextView](R.id.tags_txt).setText(c.tags.mkString(" "))
            setViewValue(R.id.recommend,SearchResult.getStar(c.rating),getView)
            hideWhenEmpty(R.id.comment_quote,c.comment)
            c.status match {
              case "read" => List(R.id.reading, R.id.wish)
              case "reading" => List(R.id.read, R.id.wish)
              case "wish" => List(R.id.reading, R.id.read)
              case _ => List(R.id.delete)
            }
          }
          case _ => {
            hideWhen(R.id.book_layout_tags,condition = true)
            List(R.id.delete)
          }
        }
        val l = rootView.find[LinearLayout](R.id.status_layout)
        toDel.foreach(id => l.removeView(rootView.findViewById(id)))

        batchSetValues(SearchResult.mapping ++ Map( R.id.bookSubtitle -> "subtitle", R.id.bookPages -> "pages", R.id.bookPrice -> "price",
          R.id.book_author_abstract -> "author_intro", R.id.book_author_abstract_longtext -> "author_intro",
          R.id.book_content_abstract -> "summary", R.id.book_content_abstract_longtext -> "summary",
          R.id.book_catalog_abstract -> "catalog", R.id.book_catalog_abstract_longtext -> "catalog",
          R.id.comment -> ("current_user_collection.comment","%s」") ), beanToMap(book))
        getThisActivity.loadImageWithTitle(if (getThisActivity.usingWIfi || !getThisActivity.using2G) book.images.large else book.images.small,R.id.book_img,book.title)
/*        displayWhen(R.id.content_arrow,rootView.find[TextView](R.id.book_content_abstract).getLineCount>4)
        displayWhen(R.id.author_arrow,rootView.find[TextView](R.id.book_author_abstract).getLineCount>4)
        displayWhen(R.id.catalog_arrow,rootView.rootView.find[TextView](R.id.book_catalog_abstract).getLineCount>4)*/
      }
      case _ =>
    }
  }
}

