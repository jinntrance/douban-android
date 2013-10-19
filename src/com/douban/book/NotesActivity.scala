package com.douban.book

import com.douban.base._
import android.os.Bundle
import com.douban.models._
import android.view._
import android.widget._
import org.scaloid.common._
import com.douban.base.DBundle
import com.douban.models.AnnotationSearch
import com.douban.models.Annotation
import scala.annotation.tailrec
import com.douban.base.DBundle
import com.douban.models.AnnotationSearch
import com.douban.models.AnnotationSearchResult
import com.douban.models.Annotation

/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 10/2/13 9:32 PM
 * @version 1.0
 */
class NotesActivity extends DoubanActivity {
  var listAdapter:NoteItemAdapter=null
  def viewNote(pos:Int){
    fragmentManager.beginTransaction().replace(R.id.notes_container,new NoteViewFragment().addArguments(DBundle().put(Constant.ARG_POSITION,pos)),Constant.ACTIVITY_NOTE_VIEW).addToBackStack(null).commit()
  }

  lazy val bookId = getIntent.getLongExtra(Constant.BOOK_ID, 0)
  lazy val listFragment: NotesListFragment = new NotesListFragment()

  protected override def onCreate(b: Bundle) {
    super.onCreate(b)
    if (0 == bookId) finish()
    setContentView(R.layout.notes)
    fragmentManager.beginTransaction().replace(R.id.notes_container,listFragment).commit()
  }

  override def onCreateOptionsMenu(menu: Menu) = {
    getMenuInflater.inflate(R.menu.add_note, menu)
    super.onCreateOptionsMenu(menu)
  }

  def search(v: View) = listFragment.search(v)
  def forward(v: View) = {
    listFragment.bookPage=find[EditText](R.id.bookPage).getText.toString
    listFragment.search(findViewById(R.id.rank))
    hidePopup(v)
  }

   override def back(v:View){
    listFragment.bookPage=""
    find[EditText](R.id.bookPage).setText("")
    super.back(v)
  }

  def hidePopup(v:View)={
    hideWhen(R.id.page_num_popup,true)
    hideKeyboard()
  }
  def showPopup(v:View)={
    displayWhen(R.id.page_num_popup,true)
    findViewById(R.id.bookPage).requestFocus()
    displayKeyboard()
  }

  def addNote(m: MenuItem) = listFragment match {
    case l:NotesListFragment=>l.addNote()
    case _=>
  }
  def addNote(v:View) = listFragment match {
    case l:NotesListFragment=>l.addNote()
    case _=>
  }
  def viewPreviousNote(m:MenuItem){
       //TODO
  }
  def viewNextNote(m:MenuItem){
       //TODO
  }
}

class NotesListFragment extends DoubanListFragment[NotesActivity] {
  import R.id._
  lazy val mapping=NotesActivity.mapping++Map(user_avatar->("author_user.avatar",("author_user.name",getString(R.string.load_img_fail))))
  var currentPage = 1
  var total = Int.MaxValue
  var rank = "rank"
  var bookPage = ""
  lazy val adapter:NoteItemAdapter=new NoteItemAdapter(mapping,search())


  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    inflater.inflate(R.layout.notes_list,container,false)
  }

  override def onActivityCreated(b: Bundle){
    super.onActivityCreated(b)
    setListAdapter(adapter)
    getThisActivity.listAdapter=adapter
    getListView.setDivider(getResources.getDrawable(R.drawable.divider))
    getListView.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS)
    getThisActivity.getActionBar.setDisplayShowCustomEnabled(false)
    search()
  }

  def search(bookId: Long = getThisActivity.bookId, order: String = rank, page: Int = currentPage,bookPage:String=bookPage) ={
    listLoader(
    toLoad = 1==page || adapter.count<total,
    result= Book.annotationsOf(bookId, new AnnotationSearch(order, (page-1)*countPerPage,countPerPage,bookPage)) ,
    success = (a:AnnotationSearchResult)=>{
      val size: Int = a.annotations.size
      total=a.total
      val index=a.start + size
      currentPage+=1
      if(1==page) {
        adapter.replaceResult(a.total, size, a.annotations)
        runOnUiThread(adapter.notifyDataSetInvalidated())
      } else {
        adapter.addResult(a.total, size, a.annotations)
        runOnUiThread(adapter.notifyDataSetChanged())
      }
      runOnUiThread{
        getThisActivity.setTitle(getString(R.string.annotation) + s"($index/$total)")
        val l=getThisActivity.find[View](R.id.note_to_add)
        l.setVisibility(if(0==total) View.VISIBLE else View.GONE)
        if(0==total) {
          fragmentManager.beginTransaction().hide(this)
          if(bookPage.isEmpty) l.find[TextView](R.id.note2add_text).setText(getString(R.string.add_note_no_page,bookPage))
        }
        else fragmentManager.beginTransaction().show(this)
      }
      if(index<total)toast(getString(R.string.more_notes_loaded).format(index))
      else toast(R.string.more_loaded_finished)
    }
    )
  }

  def addNote(){
    getThisActivity.startActivity(SIntent[AddNoteActivity].putExtra(Constant.BOOK_ID, getThisActivity.bookId).putExtra(Constant.BOOK_PAGE,bookPage))
  }

  def search(v: View) {
    val order = Map(R.id.rank -> "rank", R.id.collect -> "collect", R.id.page -> "page")
    v.getId match {
      case id: Int if rank!=order.getOrElse(id,"rank") => {
        runOnUiThread(order.keys.foreach(i=>getThisActivity.toggleBackGround(i !=id,i,(R.color.black,R.color.black_light))))
        currentPage = 1
        rank=order(id)
        search()
      }
      case _=>
    }
  }

  override def onListItemClick(l: ListView, v: View, position: Int, id: Long){
    l.setItemChecked(position, true)
    getThisActivity.viewNote(position)
  }
}

class NoteViewFragment extends DoubanFragment[NotesActivity]{
  var currentOffset=0
  var count=0
  lazy val mapping=NotesActivity.mapping++Map(R.id.user_avatar->("author_user.avatar",("author_user.name",getString(R.string.load_img_fail))))
  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = inflater.inflate(R.layout.note_view,container,false)

  override def onActivityCreated(b: Bundle){
    super.onActivityCreated(b)
    count=getThisActivity.listAdapter.getCount
    val pos: Int = getArguments.getInt(Constant.ARG_POSITION)
    getThisActivity.replaceActionBar(R.layout.header_note,getThisActivity.listAdapter.getBean(pos).book.title)
    display(pos)
  }

  def display(position:Int){

    currentOffset=position % count
    val a= getThisActivity.listAdapter.getBean(currentOffset)
    batchSetValues(mapping,getThisActivity.listAdapter.getItem(currentOffset),getView)
    val container: LinearLayout = getView.find[LinearLayout](R.id.note_content)
    container.addView(parse(a.content))

    def parse(c:String,layout:SLinearLayout=new SVerticalLayout{}):SLinearLayout={
      c match{
        case r"([\s\S]*?)${pre}<原文开始>([\s\S]+?)${txt}</原文结束>([\s\S]*)${suffix}"=>{
          parse(pre,layout)
          layout+= new SLinearLayout {
            SImageView(R.drawable.add_note_context).<<.wrap.>>
            STextView(txt).<<.wrap.Weight(1.0f).>>
          }
          parse(suffix,layout)
        }
        case r"([\s\S]*?)${pre}<图片(\d+)${imgUrl}>([\s\S]*)${suffix}"=>{
          parse(pre,layout)
          layout += new SLinearLayout{
            val img=SImageView()
            loadImage(a.photos.get(imgUrl),img)
          }
          parse(suffix,layout)
        }
        case ""=> layout
        case txt=> layout+= new SLinearLayout{STextView(txt)}
      }
    }
  }


  def displayPrevious()=display(count+currentOffset-1)
  def displayNext()=display(currentOffset+1)
}

object NotesActivity{
  import R.id._
  val mapping=Map(page_num->("page_no","P%s"),chapter_name->"chapter",note_time->"time",username->"author_user.name",note_content->"content")
}

class NoteItemAdapter(mapping:Map[Int,Any],load: => Unit)(implicit ctx: DoubanActivity) extends ItemAdapter[Annotation](R.layout.notes_item,mapping,load=load) {
  override def getView(position: Int, view: View, parent: ViewGroup): View = {
    val convertView = super.getView(position,view,parent)
    getItem(position).getOrElse("page_no","") match {
      case "" =>
      case _=>ctx.hideWhen(R.id.chapter_name,true,convertView)
    }
    convertView
  }
}
