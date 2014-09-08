package com.douban.book

import android.os.Bundle
import android.view.{View, ViewGroup}
import android.widget.{AdapterView, ListView}
import com.douban.base.{ItemAdapter, Constant, DoubanActivity}
import com.douban.book.R.id._
import com.douban.book.db.{AnnotationUploaderHelper, AnnotationUploader}
import com.douban.models.{ Annotation, Book}
import com.google.gson.Gson
import org.scaloid.common.SIntent
import org.scaloid.common._

/**
 * @author joseph
 * @since  8/30/14.
 */
class NotesDraftActivity extends DoubanActivity{
  private val mapping = Map(R.id.bookTitle -> "bookTitle",page_num ->("page_no", "P%s"),
     note_time -> "time", note_content -> "content")
  lazy val listAdapter = new NotesDraftItemAdapter(mapping)
  lazy val listView = find[ListView](R.id.notes_draft)
  lazy val dbHelper = AnnotationUploaderHelper(this.ctx)

  protected override def onCreate(b: Bundle): Unit = {
    super.onCreate(b)
    setContentView(R.layout.notes_draft)
    listView.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS)
    listView.onItemClick((l: AdapterView[_], v: View, position: Int, id: Long) => {
      val annt2Post=listAdapter.getItem(position)
      startActivity(SIntent[AddNoteActivity].putExtra(Constant.ANNOTATION_POSTED, annt2Post).
        putExtra(Constant.BOOK_ID,annt2Post.bookId))
    })
    listAdapter.getItems.addAll(dbHelper.findData(size = Int.MaxValue))
    listView.setAdapter(listAdapter)
  }
  def send(pos: Int) {
    val annt=listAdapter.getItem(pos)
    handle({
       Book.postAnnotation(annt.bookId,annt.annotation)
    },(a:Option[Annotation])=> a match {
      case Some(an)=>
        toast(R.string.annotation_added)
        runOnUiThread(remove(pos))
      case _=> toast(R.string.annotation_fails_to_add)
    })
  }
  def remove(pos:Int) ={
    listAdapter.getItems.remove(pos)
    listAdapter.notifyDataSetChanged()
  }

  override def onStop(): Unit = {
    dbHelper.deleteAll()
    dbHelper.insertAll(listAdapter.getItems.toList)
    super.onStop()
  }
}

class NotesDraftItemAdapter(mapping: Map[Int, Any])(implicit ctx: DoubanActivity) extends ItemAdapter[AnnotationUploader](R.layout.notes_draft_item, mapping, load = {}) with Serializable
