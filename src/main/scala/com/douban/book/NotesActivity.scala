package com.douban.book

import com.douban.base._
import android.os.Bundle
import com.douban.models.Book
import android.view._
import scala.concurrent._
import android.widget.{EditText, ListView, BaseAdapter}
import ExecutionContext.Implicits.global
import scala.collection.mutable
import com.douban.base.DBundle
import com.douban.models.AnnotationSearch
import com.douban.models.Annotation
import org.scaloid.common._

/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 10/2/13 9:32 PM
 * @version 1.0
 */
class NotesActivity extends DoubanActivity {
  lazy val bookId = getIntent.getLongExtra(Constant.BOOK_ID, 0)
  lazy val listFragment: NotesListFragment = findFragment[NotesListFragment](R.id.notes_list_fragment)

  def getNote(i:Int):Map[String,String]=listFragment.adapter.getItem(i)

  protected override def onCreate(b: Bundle) {
    super.onCreate(b)
    if (0 == bookId) finish()
    waitToLoad()
    setContentView(R.layout.notes)
  }

  override def onCreateOptionsMenu(menu: Menu) = {
    getMenuInflater.inflate(R.menu.add_note, menu)
    super.onCreateOptionsMenu(menu)
  }

  def search(v: View) = listFragment.search(v)
  def forward(v: View) = {
    listFragment.bookPage=find[EditText](R.id.bookPage).getText.toString
    listFragment.search()
    hidePopup(v)
  }

  def hidePopup(v:View)=hideWhen(R.id.page_num_popup,true)
  def showPopup(v:View)=displayWhen(R.id.page_num_popup,true)

  def addNote(m: MenuItem) = {
    startActivity(SIntent[AddNoteActivity].putExtra(Constant.BOOK_ID, bookId))
  }
}

class NotesListFragment extends DoubanListFragment[NotesActivity] {
  import R.id._
  lazy val mapping=Map(page_num->("page_no","P%s"),chapter_name->"chapter",note_time->"time",username->"author_user.name",note_content->"content",user_avatar->("author_user.avatar",("author_user.name",getString(R.string.load_img_fail))))
  var currentPage = 1
  var total = 0
  var rank = "rank"
  var bookPage = ""
  lazy val adapter:NoteItemAdapter=new NoteItemAdapter(mutable.Buffer[Map[String,String]]())

  override def onActivityCreated(b: Bundle){
    super.onActivityCreated(b)
    setListAdapter(adapter)
    getListView.setDivider(getResources.getDrawable(R.drawable.divider))
    getListView.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS)
    search()
  }

  def search(bookId: Long = getThisActivity.bookId, order: String = rank, page: Int = 1,bookPage:String=bookPage) = future {
    Book.annotationsOf(bookId, new AnnotationSearch(order, (page-1)*countPerPage,countPerPage,bookPage))
  }onSuccess{
    case a=> getThisActivity.runOnUiThread{
      total=a.total
      val index=a.start + a.annotations.size
      currentPage=page
      if(1==page) adapter.replaceCollections(a.annotations)
      else adapter.addNewCollections(a.annotations)
      if(1==page) adapter.notifyDataSetInvalidated()
      else adapter.notifyDataSetChanged()
      getThisActivity.setTitle(getString(R.string.annotation) + s"($index/$total)")
      if(index<total)toast(getString(R.string.more_notes_loaded).format(index))
      else toast(R.string.more_loaded_finished)
      getThisActivity.finishedLoading()
    }
  }

  def search(v: View) {
    loadData(v)
  }

  def loadData(v: View,page:Int=1) {
    val order = Map(R.id.rank -> "rank", R.id.collect -> "collect", R.id.page -> "page")
    v.getId match {
      case id: Int if rank!=order.getOrElse(id,"rank") => {
        order.keys.foreach(toggleBackGround(true,_,(R.color.black,R.color.black_light)))
        v.setBackgroundColor(R.color.black_light)
        currentPage = page
        rank=order(id)
        search()
      }
    }
  }

  override def onListItemClick(l: ListView, v: View, position: Int, id: Long){
    getListView.setItemChecked(position, true)
    getFragmentManager.beginTransaction().replace(R.id.notes_container,new NoteViewFragment().addArguments(DBundle().put(Constant.ARG_POSITION,position))).addToBackStack("viewNote").commit()
  }

  class NoteItemAdapter(var data: mutable.Buffer[Map[String, String]]) extends BaseAdapter {
    private var count=0
    override def getView(position: Int, view: View, parent: ViewGroup): View = if(getCount==0) null else{
      val convertView = if(null!=view) view else getThisActivity.getLayoutInflater.inflate(R.layout.notes_item,null)
      val currentLine: Map[String, String] = data.get(position)
      data.get(position).getOrElse("page_no","") match {
        case "" =>
        case _=>hideWhen(R.id.chapter_name,true,convertView)
      }

      batchSetValues(mapping,currentLine,convertView)
      if(position+2>=count&&count<total) search(page=currentPage+1)
      convertView
    }

    def getCount: Int = count

    def getItem(p1: Int): Map[String,String] =data.get(p1)

    def getItemId(p1: Int): Long = p1

    def addNewCollections(list:java.util.List[Annotation])={
      data++=list.map(beanToMap(_))
      count+=list.size()
    }
    def replaceCollections(list:java.util.List[Annotation])={
      data=list.map(beanToMap(_))
      count=data.size
    }
  }
}

class NoteViewFragment extends DoubanFragment[NotesActivity]{
  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = inflater.inflate(R.layout.note_view,container,false)

  override def onActivityCreated(b: Bundle){
        batchSetValues(getThisActivity.listFragment.mapping,getThisActivity.getNote(getThisActivity.getIntent.getIntExtra(Constant.ARG_POSITION,0)))
  }
}

