package com.douban.book

import com.douban.base.{SwipeGestureDoubanActivity, Constant}
import android.view._
import android.os.Bundle
import android.widget.LinearLayout
import org.scaloid.common._
import android.app.Activity
import com.douban.models.Book
import java.util

/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 10/30/13 8:14 PM
 * @version 1.0
 */

class PublicNoteViewActivity extends NoteViewActivity(R.layout.note_view)

class NoteViewActivity(layoutId:Int) extends SwipeGestureDoubanActivity{
  var count=0
  val mapping:Map[Int,Any]=NotesActivity.mapping++Map(R.id.user_avatar->"author_user.avatar")
  var pos = 0

  lazy val dataList:util.List[Map[String,String]]=getIntent.getSerializableExtra(Constant.DATA_LIST) match {
    case a:util.ArrayList[Map[String,String]]=>a
    case _=>this.finish();null
  }

  private def positionFromIntent=getIntent.getIntExtra(Constant.ARG_POSITION,0)

    override def onCreate(b: Bundle){
      super.onCreate(b)
      setContentView(layoutId)
      pos=positionFromIntent
      count=dataList.size()
      replaceActionBar(R.layout.header_note,getString(R.string.annotation))
      display(pos)
    }

    def display(position:Int){
      pos=position
      val a= dataList.get(pos)
      setWindowTitle(a.getOrElse("book.title",getString(R.string.annotation)))
      batchSetValues(mapping,a)
      val container: LinearLayout = find[LinearLayout](R.id.note_content)
      container.removeAllViews()
      container.addView(parse(a.getOrElse("content","")))

      def parse(c:String,layout:SLinearLayout=new SVerticalLayout{}):SLinearLayout={
        c match{
          case r"([\s\S]*?)${pre}<原文开始>([\s\S]+?)${txt}</原文结束>([\s\S]*)${suffix}"=>{
            parse(pre,layout)
            layout+= new SLinearLayout {
              SImageView(R.drawable.add_note_context).<<.wrap.>>
              STextView(txt.trim).<<.wrap.Weight(1.0f).>>
            }
            parse(suffix,layout)
          }
          case r"([\s\S]*?)${pre}<图片(\d+)${imgUrl}>([\s\S]*)${suffix}"=>{
            parse(pre,layout)
            layout += new SLinearLayout{
              val img=SImageView()
              loadImage(a.getOrElse(s"photos.$imgUrl",""),img)
            }
            parse(suffix,layout)
          }
          case ""=> layout
          case txt=> layout+= new SLinearLayout{STextView(txt.trim)}
        }
      }
    }

  override def finish(){
    setResult(Activity.RESULT_OK,getIntent.putExtra(Constant.ARG_POSITION,pos))
    super.finish()
  }

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    if(currentUserIdWithoutLogin.toString == dataList.get(positionFromIntent).getOrElse("author_id","")){
      getMenuInflater.inflate(R.menu.edit_note,menu)
      true
    }else super.onCreateOptionsMenu(menu)
  }

  def editNote(m:MenuItem)={
    val annotation=dataList.get(pos)
    val page=annotation.getOrElse("page_no","")
    val chapter=annotation.getOrElse("chapter","")
    val content=annotation.getOrElse("content","")
    val id=annotation.getOrElse("id","0")
    startActivity(SIntent[AddNoteActivity].putExtra(Constant.ANNOTATION_ID,id).putExtra(Constant.BOOK_PAGE,page).
      putExtra(Constant.ANNOTATION_CHAPTER,chapter).putExtra(Constant.ANNOTATION_CONTENT,content).
      putExtra(Constant.ANNOTATION_IMAGES_NUMBER,annotation.getOrElse("last_photo","0")))
  }
  def deleteNote(m:MenuItem)={
    handle(Book.deleteAnnotation(dataList.get(pos).getOrElse("id","0").toLong),
      (deleted:Boolean)=>{
      toast(if(deleted) R.string.removed_successfully else R.string.removed_unsuccessfully)
    })
  }

  def showNext(): Unit = {
    pos=(pos+1)%count //TODO load new notes
    display(pos)
  }

  def showPre(): Unit = {
    pos=(count+pos-1)%count
    display(pos)
  }
}
