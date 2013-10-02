package com.douban.book

import com.douban.base.{Constant, DoubanListFragment, DoubanActivity}
import android.os.Bundle
import com.douban.models.{AnnotationSearch, Book}
import android.view.{ViewGroup, View}
import scala.concurrent._
import android.content.Context
import android.widget.SimpleAdapter
import ExecutionContext.Implicits.global

/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 10/2/13 9:32 PM
 * @version 1.0
 */
class NotesActivity extends DoubanActivity{
  lazy val bookId=getIntent.getLongExtra(Constant.BOOK_ID,0)
  var notesListFragment: NotesListFragment = null
  protected override def onCreate(b: Bundle) {
    super.onCreate(b)
    if(0==bookId) finish()
    setContentView(R.layout.notes)
    notesListFragment=new NotesListFragment()
    getFragmentManager.beginTransaction().replace(R.id.notes_list,notesListFragment).commit()
  }
  def search(v:View)=notesListFragment.search(v)
}

class NotesListFragment extends DoubanListFragment[NotesActivity]{
  var currentPage=1
  override def onCreate(b: Bundle) {
    super.onCreate(b)
    val a=Book.annotationsOf(getThisActivity.bookId)
  }

  def search(bookId:Long=getThisActivity.bookId,order:String="rank",page:Int=currentPage)= future {
       val a=Book.annotationsOf(bookId,new AnnotationSearch(order=order,page =page))
       getThisActivity.setTitle(getString(R.string.annotation)+s"(${a.start+a.annotations.size}/${a.total}})")
     }

  def search(v:View) {
    val order=Map(R.id.rank->"rank",R.id.collect->"collect",R.id.page->"page")
    v.getId match{
      case id:Int if order.contains(id) =>{
        v.setBackgroundColor(R.color.black_light)
        order.keys.filter(_!=id).foreach(rootView.findViewById(_).setBackgroundColor(R.color.black))
        currentPage=1
        search(order=order(id))
      }
    }
  }
  class NoteItemAdapter(context: Context, data: java.util.List[_ <: java.util.Map[String, _]], resource: Int, from: Array[String], to: Array[Int]) extends SimpleAdapter(context, data, resource, from, to) {
    override def getView(position: Int, view: View, parent: ViewGroup): View = {
      val convertView = super.getView(position, view, parent)
      if (null != convertView) {

      }
      convertView
    }
  }
}

