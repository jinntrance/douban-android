package com.douban.book

import com.douban.base.DoubanActivity
import android.view.{ViewGroup, View}

/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 10/7/13 3:53 PM
 * @version 1.0
 */
class MyNoteActivity extends DoubanActivity{

}

class MyNoteItemAdapter(mapping:Map[Int,Any],load: => Unit)(implicit ctx: DoubanActivity)  extends NoteItemAdapter(mapping,load,R.layout.my_notes_item){
  override def getView(position: Int, view: View, parent: ViewGroup): View = {
    val convertView=super.getView(position, view, parent)

    convertView
  }
}
