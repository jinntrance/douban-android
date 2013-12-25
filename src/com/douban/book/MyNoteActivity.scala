package com.douban.book

import com.douban.base.{Constant, DBundle, DoubanActivity}
import android.view.{LayoutInflater, ViewGroup, View}
import android.os.Bundle
import com.douban.models._
import android.widget.{AdapterView, ListView}
import org.scaloid.common._
import android.content.Intent
import android.app.Activity
import java.util
import com.douban.models.ListSearchPara
import com.douban.models.AnnotationSearchResult

/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 10/7/13 3:53 PM
 * @version 1.0
 */
class MyNoteActivity extends DoubanActivity{
  private var currentPage = 1
  private var total = Int.MaxValue
  private val mapping=NotesActivity.mapping++Map(R.id.book_img->"book.images.medium")
  private val REQUEST_CODE=0
  lazy val listAdapter=new MyNoteItemAdapter(mapping,firstLoad)
  lazy val listView=find[ListView](R.id.my_notes)
  protected override def onCreate(b: Bundle) {
    super.onCreate(b)
    setContentView(R.layout.mynotes)
    listView.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS)
    listView.onItemClick((l: AdapterView[_], v: View, position: Int, id: Long)=>{
      viewNote(position)
    })
    listView.setAdapter(listAdapter)
    firstLoad()
  }

  def firstLoad()=load()

  def load(page:Int=currentPage){
    listLoader(
    toLoad = 1==page || listAdapter.getCount<total,
    result=Book.annotationsOfUser(currentUserId,new ListSearchPara(listAdapter.getCount,countPerPage)),
    success= (a:AnnotationSearchResult)=>{
      val size: Int = a.annotations.size
      total=a.total
      put(Constant.NOTES_NUM,total)
      val index=a.start + size
      currentPage+=1
      if(1==page) {
        listAdapter.replaceResult(a.total, size, a.annotations)
        runOnUiThread(listAdapter.notifyDataSetInvalidated())
      } else {
        listAdapter.addResult(a.total, size, a.annotations)
        runOnUiThread(listAdapter.notifyDataSetChanged())
      }
      setTitle(getString(R.string.annotation) + s"($index/$total)")
      if(index<total)toast(getString(R.string.more_notes_loaded).format(index))
      else toast(R.string.more_loaded_finished)
    }
    )
  }
  def viewNote(pos:Int){
    startActivityForResult(SIntent[MyNoteViewActivity].putExtra(Constant.ARG_POSITION,pos).putExtra(Constant.DATA_LIST,new util.ArrayList(listAdapter.getData)),REQUEST_CODE)
  }

  override def onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
    super.onActivityResult(requestCode,resultCode,data)
    if(requestCode==REQUEST_CODE&&resultCode==Activity.RESULT_OK){
      val p=data.getIntExtra(Constant.ARG_POSITION,-1)
      if(-1!=p)
        listView.setSelection(p)
    }
  }
}

class MyNoteItemAdapter(mapping:Map[Int,Any],load: => Unit)(implicit ctx: DoubanActivity) extends NoteItemAdapter(mapping,load,R.layout.my_notes_item)

class MyNoteViewActivity  extends NoteViewActivity(R.layout.mynote_view){

  override val mapping= NotesActivity.mapping++Map(R.id.book_img->"book.images.medium",R.id.bookTitle -> "book.title", R.id.bookAuthor -> List("book.author", "book.translator"), R.id.bookPublisher -> List("book.publisher", "book.pubdate"))

}
