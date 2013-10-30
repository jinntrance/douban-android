package com.douban.book

import com.douban.base.{Constant, DoubanFragment, DoubanActivity}
import android.view.{View, ViewGroup, LayoutInflater}
import android.os.Bundle
import android.widget.EditText

import org.scaloid.common._
import scala.concurrent._
import com.douban.models.Book
import scala.util.Success
import com.douban.models.AnnotationPosted
import ExecutionContext.Implicits.global

/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 9/30/13 11:51 AM
 * @version 1.0
 */
class AddNoteActivity extends DoubanActivity {
  var bookPage=""
  var chapter=""
  var public=true
  override def onCreate(b: Bundle){
    super.onCreate(b)
    setContentView(R.layout.add_note_container)
    getIntent.getExtras.getString(Constant.BOOK_PAGE) match{
      case p:String if p.nonEmpty => {
        bookPage=p
        fragmentManager.beginTransaction().replace(R.id.add_note_container,new AddNoteFragment).commit()
      }
      case _=> editChapter(null)
    }

  }

  def submit(v:View){
    findViewById(R.id.bookPage) match{
      case bp:EditText=>{
        bookPage=bp.getText.toString.trim
        chapter=find[EditText](R.id.chapter_name).getText.toString.trim
        if(bookPage.nonEmpty||chapter.nonEmpty) fragmentManager.beginTransaction().replace(R.id.add_note_container,new AddNoteFragment).commit()
      }
      case _=> future {
        getIntent.getLongExtra(Constant.BOOK_ID,0) match {
          case bookId:Long if bookId>0 => {
            toast("正在保存到豆瓣帐号...")
            val a=new AnnotationPosted(find[EditText](R.id.note_input).text.toString,bookPage.toInt,chapter,if(public) "public" else "private")
            Book.postAnnotation(bookId,a)
          }
          case _=>None
        }
      }onComplete{
        case Success(Some(a))=>runOnUiThread{
          toast(R.string.annotation_added)
          this.onBackPressed()
        }
        case _=> toast(R.string.annotation_fails_to_add)
      }
    }
  }
  def editChapter(v:View){
    fragmentManager.beginTransaction().replace(R.id.add_note_container,new AddChapterFragment()).addToBackStack(null).commit()
  }


  def checkPrivacy(v: View) {
    public = toggleBackGround(public, v, (R.drawable.private_icon, R.drawable.public_icon))
  }

  def addQuote(v:View){
    val text=find[EditText](R.id.note_input)
    val start: Int = text.getSelectionStart
    val end: Int = text.getSelectionEnd
    val (newString:String,newEnd:Int)=text.getText.toString.substring(start,end).trim match{
      case r"<原文开始>([\s\S]*)${selection}</原文结束>"=>(selection,end-13)
      case s:String=>(s"<原文开始>$s</原文结束>",end+13)
    }
    text.getText.replace(start,end,newString)
    text.setSelection(start,newEnd)
  }

  @inline def wrap(txt:String,wrapper:String)={
    s"<$wrapper>$txt</$wrapper>"
  }

}

class AddChapterFragment extends DoubanFragment[AddNoteActivity]{
  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, b: Bundle): View = inflater.inflate(R.layout.note_add_chapter,container,false)

  override def onActivityCreated(b: Bundle) {
    super.onActivityCreated(b)
    activity.replaceActionBar(R.layout.header_edit,"添加页码/章节名")
    setViewValue(R.id.bookPage,activity.bookPage,hideEmpty = false)
    setViewValue(R.id.chapter_name,activity.chapter,hideEmpty = false)
  }
}

class AddNoteFragment extends DoubanFragment[AddNoteActivity]{
  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, b: Bundle): View = inflater.inflate(R.layout.note_add,container,false)

  override def onActivityCreated(b: Bundle) {
    super.onActivityCreated(b)
    activity.replaceActionBar(R.layout.header_edit_note,if(activity.bookPage.isEmpty) activity.chapter else "P"+activity.bookPage)
  }
}

