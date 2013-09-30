package com.douban.book.ui

import com.douban.base.{Constant, DoubanActivity}
import android.os.Bundle
import com.douban.book.R
import com.douban.models.Book
import android.widget._
import android.view.View
import com.douban.models.Collection

/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 9/29/13 2:47 AM
 * @version 1.0
 */
class CollectionActivity extends DoubanActivity{
  protected override def onCreate(b: Bundle) {
    super.onCreate(b)
    setContentView(R.layout.collection)
    val book=getIntent.getExtras.getSerializable("book").asInstanceOf[Book]
    val collection: Collection = book.current_user_collection
    if(null!=collection){
      val currentStatus=find[Button](collection.status match{
        case "read"=>R.id.read
        case "reading"=>R.id.reading
        case "wish"=>R.id.wish
      })
      check(currentStatus)
      find[EditText](R.id.comment).setText(collection.comment)
      find[RatingBar](R.id.rating).setRating(collection.rating.value.toFloat)
      val tagsContainer: LinearLayout = find[LinearLayout](R.id.tags_container)
//      collection.tags.asScala.foreach(tag=>tagsContainer.addView(STextView(tag).asInstanceOf[TextView]))
    }else {
      val id=getIntent.getExtras.getInt(Constant.STATE_ID)
      check(find[Button](if(0==id) R.id.wish else id))
    }
  }

  def check(v:View){
    v match{
      case b:Button=>{
        val mark='✓'
        b.setText(b.getText+mark.toString)
        List(R.id.read,R.id.reading,R.id.wish).filter(_!=b.getId).foreach(id=>{
          val button=find[Button](id)
          button.setText(button.getText.toString.takeWhile(_!=mark))
        })
      }
    }
  }
}
