package com.douban.book

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.{View, ViewGroup}
import android.widget.{AdapterView, ListView}
import com.douban.base.{Constant, DoubanActivity, ItemAdapter}
import com.douban.book.R.id._
import com.douban.book.db.{AnnotationUploader, AnnotationUploaderHelper}
import com.douban.models.{Annotation, AnnotationPosted, Book}
import org.scaloid.common.{SIntent, _}

/**
 * @author joseph
 * @since  8/30/14.
 */
class NotesDraftActivity extends DoubanActivity{
  private val mapping = Map(R.id.bookTitle -> "annotation.chapter",page_num ->("annotation.page", "P%s"),
     note_time -> "", note_content -> "annotation.content")
  lazy val listAdapter = new NotesDraftItemAdapter(mapping)
  lazy val listView = find[ListView](R.id.notes_draft)
  lazy val dbHelper = AnnotationUploaderHelper(this.ctx)
  val requestCode = 99
  var clickedPos = -1

  protected override def onCreate(b: Bundle): Unit = {
    super.onCreate(b)
    setContentView(R.layout.notes_draft)
    listView.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS)
    listView.onItemClick((l: AdapterView[_], v: View, position: Int, id: Long) => {
      val annt2Post=listAdapter.getItem(position)
      clickedPos = position
      startActivityForResult(SIntent[AddNoteActivity].putExtra(Constant.ANNOTATION_POSTED, annt2Post.annotation).
        putExtra(Constant.BOOK_ID,annt2Post.bookId),requestCode)
    })
    val notes = dbHelper.findData(size = Int.MaxValue)
    listAdapter.addResult(notes.size,notes.size,notes)
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

  override def onActivityResult(requestCode: Int, resultCode: Int, intent: Intent) {
    super.onActivityResult(requestCode, resultCode, intent)
    if (this.requestCode == requestCode && resultCode == Activity.RESULT_OK) {
      val annotation = intent.getSerializableExtra(Constant.ANNOTATION_POSTED).asInstanceOf[AnnotationPosted]
      val prev=listAdapter.getItem(clickedPos)
      listAdapter.getItems.set(clickedPos,new AnnotationUploader(prev.bookId,prev.bookTitle,annotation))
      listAdapter.notifyDataSetChanged()
    }
  }
  class NotesDraftItemAdapter(mapping: Map[Int, Any])(implicit activity: DoubanActivity) extends ItemAdapter[AnnotationUploader](R.layout.notes_draft_item, mapping, load = {}) with Serializable{
    override def getView(position: Int, view: View, parent: ViewGroup): View = {
      val convertView = super.getView(position, view, parent)
      convertView.findViewById(R.id.note_send) onClick {_:(View) => {
        send(position)
      }}
      convertView.findViewById(R.id.remove_note) onClick {_:(View) => {
        new AlertDialogBuilder(getString(R.string.remove_note), getString(R.string.remove_note_confirm)) {
          positiveButton(onClick = {
            remove(position)
          })
        }.show()
      }}
      convertView
    }
  }
}

