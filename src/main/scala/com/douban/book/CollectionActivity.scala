package com.douban.book

import com.douban.base.{DoubanFragment, Constant, DoubanActivity}
import android.os.Bundle
import com.douban.models.{CollectionPosted, Book, Collection}
import android.widget._
import android.view.View
import collection.JavaConverters._

/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 9/29/13 2:47 AM
 * @version 1.0
 */
class CollectionActivity extends DoubanActivity {
  var status="wish"
  var privacy="public" //private
  var book:Book=null
  var collection:Collection=null
  val mapping=Map("read"-> R.id.read, "reading"-> R.id.reading,  "wish" -> R.id.wish)
  val reverseMapping=mapping.map(_.swap)
  protected override def onCreate(b: Bundle) {
    super.onCreate(b)
    setContentView(R.layout.collection)
    replaceActionBar(R.layout.header_edit,getString(R.string.add_collection))
    book = getIntent.getSerializableExtra(Constant.BOOK_KEY).asInstanceOf[Book]
    val collection: Collection = book.current_user_collection
    if (null != collection) {
      val currentStatus = find[Button](mapping(collection.status))
      check(currentStatus)
      find[EditText](R.id.comment).setText(collection.comment)
      find[RatingBar](R.id.rating).setRating(collection.rating.value.toFloat)
      val tagsContainer: LinearLayout = find[LinearLayout](R.id.tags_container)
      collection.tags.asScala.foreach(tag=>tagsContainer.addView(tag))
    } else {
      val id = getIntent.getExtras.getInt(Constant.STATE_ID)
      check(find[Button](if (0 == id) R.id.wish else id))
    }
  }

  def check(v: View) {
    v match {
      case b: Button => {
        val mark = '✓'
        status=reverseMapping(b.getId)
        b.setText(b.getText + mark.toString)
        List(R.id.read, R.id.reading, R.id.wish).filter(_ != b.getId).foreach(id => {
          val button = find[Button](id)
          button.setText(button.getText.toString.takeWhile(_ != mark))
        })
      }
    }
  }

  def submit(v:View){
    val layout=find[LinearLayout](R.id.tags_container)
    val tags =(0 to layout.getChildCount).map(i=>layout.getChildAt(i).asInstanceOf[TextView].getText).mkString(" ")
    val p=CollectionPosted(status,tags,find[EditText](R.id.comment).getText.toString.trim,find[RatingBar](R.id.rating).getNumStars,privacy)
    Book.postCollection(book.id,p,false)
  }


}

class TagFragment extends DoubanFragment
