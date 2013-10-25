package com.douban.book

import com.douban.base.{DoubanFragment, Constant, DBundle, DoubanActivity}
import android.view.{LayoutInflater, ViewGroup, View}
import android.os.Bundle
import com.douban.models.{AnnotationSearchResult, ListSearch, Book}
import android.widget.{AdapterView, TextView, ListView}
import org.scaloid.common._
import com.douban.models.AnnotationSearchResult
import java.io.Serializable

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
  lazy val listAdapter=new MyNoteItemAdapter(mapping,load())
  protected override def onCreate(b: Bundle) {
    super.onCreate(b)
    setContentView(R.layout.mynotes)
    val listView=find[ListView](R.id.my_notes)
    listView.setAdapter(listAdapter)
    listView.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS)
    listView.onItemClick((l: AdapterView[_], v: View, position: Int, id: Long)=>{
      viewNote(position)
    })
  }
  def load(page:Int=currentPage){
    listLoader(
    toLoad = 1==page || listAdapter.getCount<total,
    result=Book.annotationsOfUser(currentUserId.toString,new ListSearch(listAdapter.getCount,countPerPage)),
    success= (a:AnnotationSearchResult)=>{
      val size: Int = a.annotations.size
      total=a.total
      val index=a.start + size
      currentPage+=1
      if(1==page) {
        listAdapter.replaceResult(a.total, size, a.annotations)
        runOnUiThread(listAdapter.notifyDataSetInvalidated())
      } else {
        listAdapter.addResult(a.total, size, a.annotations)
        runOnUiThread(listAdapter.notifyDataSetChanged())
      }
      runOnUiThread{
        setTitle(getString(R.string.annotation) + s"($index/$total)")
      }
      if(index<total)toast(getString(R.string.more_notes_loaded).format(index))
      else toast(R.string.more_loaded_finished)
    }
    )
  }
  def viewNote(pos:Int){
    fragmentManager.beginTransaction().add(R.id.notes_container,new MyNoteViewFragment(listAdapter).addArguments(DBundle().put(Constant.ARG_POSITION,pos)),Constant.ACTIVITY_NOTE_VIEW).addToBackStack(null).commit()
  }
}

class MyNoteItemAdapter(mapping:Map[Int,Any],load: => Unit)(implicit ctx: DoubanActivity)  extends NoteItemAdapter(mapping,load,R.layout.my_notes_item){
}

class MyNoteViewFragment(listAdapter:NoteItemAdapter)  extends NoteViewFragment(listAdapter){

  override lazy val mapping= NotesActivity.mapping++Map(R.id.book_img->"book.images.medium",R.id.bookTitle -> "title", R.id.bookAuthor -> List("author", "translator"), R.id.bookPublisher -> List("publisher", "pubdate"))

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = inflater.inflate(R.layout.mynote_view,container,false)
}
