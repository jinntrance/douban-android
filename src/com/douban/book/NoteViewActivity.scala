package com.douban.book

import com.douban.base.{SwipeGestureDoubanActivity, Constant, DoubanFragment, DoubanActivity}
import android.view.{View, ViewGroup, LayoutInflater}
import android.os.Bundle
import android.widget.LinearLayout
import org.scaloid.common.{STextView, SImageView, SVerticalLayout, SLinearLayout}
import android.app.Activity
import com.douban.models.Annotation
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
  var currentOffset=0
  var count=0
  val mapping:Map[Int,Any]=NotesActivity.mapping++Map(R.id.user_avatar->"author_user.avatar")
  var pos = 0
  lazy val dataList:util.List[Map[String,String]]=getIntent.getSerializableExtra(Constant.DATA_LIST) match {
    case a:util.ArrayList[Map[String,String]]=>a
    case _=>this.finish();null
  }

    override def onCreate(b: Bundle){
      super.onCreate(b)
      setContentView(layoutId)
      pos=getIntent.getIntExtra(Constant.ARG_POSITION,0)
      count=dataList.size()
      replaceActionBar(R.layout.header_note,dataList.get(pos).getOrElse("book.title",getString(R.string.annotation)))
      display(pos)
    }

    def display(position:Int){
      pos=position
      currentOffset=position % count
      val a= dataList.get(currentOffset)
      batchSetValues(mapping,a)
      val container: LinearLayout = find[LinearLayout](R.id.note_content)
      container.addView(parse(a.getOrElse("content","")))

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
              loadImage(a.getOrElse(s"photos.$imgUrl",""),img)
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

  override def finish(){
    setResult(Activity.RESULT_OK,getIntent.putExtra(Constant.ARG_POSITION,pos))
    super.finish()
  }

  def showNext(): Unit = {
    pos=(pos+1)%count
    display(pos)
  }

  def showPre(): Unit = {
    pos=(pos-1)%count
    display(pos)
  }
}
