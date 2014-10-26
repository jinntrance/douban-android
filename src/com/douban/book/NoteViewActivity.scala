package com.douban.book

import java.util

import android.app.Activity
import android.os.Bundle
import android.view._
import android.widget.{LinearLayout, ScrollView}
import com.douban.base.{Constant, SwipeGestureDoubanActivity}
import com.douban.models.{Annotation, Book}
import com.google.gson.Gson
import org.scaloid.common._
import uk.co.senab.photoview.PhotoViewAttacher

/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 10/30/13 8:14 PM
 * @version 1.0
 */

class PublicNoteViewActivity extends NoteViewActivity(R.layout.note_view)

class NoteViewActivity(layoutId: Int) extends SwipeGestureDoubanActivity {
  var count = 0
  val mapping: Map[Int, Any] = NotesActivity.mapping ++ Map(R.id.user_avatar -> "author_user.avatar")
  var pos = 0

  lazy val dataList: util.List[Annotation] = fetchData match {
    case a: util.ArrayList[Annotation] => a
    case _ => this.finish(); null
  }

  private def positionFromIntent = getIntent.getIntExtra(Constant.ARG_POSITION, 0)

  lazy private val scrollView = find[ScrollView](R.id.scrollView)

  override def onCreate(b: Bundle) {
    super.onCreate(b)
    setContentView(layoutId)
    pos = positionFromIntent
    count = dataList.size()
    replaceActionBar(R.layout.header_note, getString(R.string.annotation))
    display(pos)
    toast(R.string.swipe_to_view_notes)
  }

  def display(position: Int) {
    scrollView.smoothScrollTo(0, 0)
    pos = position
    val a = dataList.get(pos)
    setWindowTitle(a.book.title)
    batchSetValues(mapping, a)
    val container: LinearLayout = find[LinearLayout](R.id.note_content)
    val black = getResources.getColor(R.color.text_black)
    container.removeAllViews()
    container.addView(parse(a.content))
    def parse(c: String, layout: SLinearLayout = new SVerticalLayout {}): SLinearLayout = {
      c match {
        case r"([\s\S]*?)${pre}<原文开始>([\s\S]+?)${txt}</原文结束>([\s\S]*)${suffix}" =>
          parse(pre, layout)
          layout += new SLinearLayout {
            SImageView(R.drawable.add_note_context).<<.wrap.>>
            STextView(txt.trim).<<.wrap.Weight(1.0f).>>
          }
          parse(suffix, layout)
        case r"([\s\S]*?)${pre}<图片(\d+)${imgUrl}>([\s\S]*)${suffix}" =>
          parse(pre, layout)
          layout += new SLinearLayout {
            val img = SImageView() //.onClick(popup(_))
            new PhotoViewAttacher(img)
            loadImage(a.photos.get(imgUrl), img, fillWidth = true)
          }
          parse(suffix, layout)
        case "" => layout
        case txt => layout += new SLinearLayout {
          STextView(txt.replaceAll( """\s+$""", "\n")).textColor(black)
        }
      }
    }
  }

  override def finish() {
    setResult(Activity.RESULT_OK, getIntent.putExtra(Constant.ARG_POSITION, pos))
    super.finish()
  }

  def viewBook(v: View) {
    val bookId: String = dataList.get(pos).book_id
    val title: String = dataList.get(pos).book.title
    if (bookId.toLong > 0)
      startActivity(SIntent[BookActivity].putExtra(Constant.BOOK_ID, bookId).putExtra(Constant.BOOK_TITLE, title))
  }

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    if (currentUserIdWithoutLogin.toString == dataList.get(positionFromIntent).author_id) {

      getMenuInflater.inflate(R.menu.edit_note, menu)
      //      findViewById(R.id.more_icon).setVisibility(View.VISIBLE)
      true
    } else super.onCreateOptionsMenu(menu)
  }

  def showOptions(v: View) = {
    openOptionsMenu()
  }

  def editNote(m: MenuItem) = {
    val annt = dataList.get(pos)
    startActivity(SIntent[AddNoteActivity].putExtra(Constant.ANNOTATION, new Gson().toJson(annt)))
  }

  def deleteNote(m: MenuItem) = {
    handle(Book.deleteAnnotation(dataList.get(pos).id),
      (deleted: Boolean) => {
        toast(if (deleted) R.string.removed_successfully else R.string.removed_unsuccessfully)
      })
  }

  def showNext(): Unit = {
    pos = (pos + 1) % count //TODO load new notes
    display(pos)
  }

  def showPre(): Unit = {
    pos = (count + pos - 1) % count
    display(pos)
  }
}
