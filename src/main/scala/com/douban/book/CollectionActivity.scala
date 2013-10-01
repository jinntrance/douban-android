package com.douban.book

import com.douban.base.{DoubanFragment, Constant, DoubanActivity}
import android.os.Bundle
import com.douban.models.{CollectionPosted, Book, Collection}
import android.widget._
import android.view.{ViewGroup, LayoutInflater, View}
import collection.JavaConverters._
import scala.concurrent._
import ExecutionContext.Implicits.global
import org.scaloid.common._
import scala.util.{Failure, Success}
import android.app.Activity
import android.content.Intent

/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 9/29/13 2:47 AM
 * @version 1.0
 */
class CollectionActivity extends DoubanActivity {
  var collectionFrag:CollectionFragment=null
  var book:Book=null
  protected override def onCreate(b: Bundle) {
    super.onCreate(b)
    setContentView(R.layout.collection_container)
    book = getIntent.getSerializableExtra(Constant.BOOK_KEY).asInstanceOf[Book]
    if(null==collectionFrag) collectionFrag=new CollectionFragment()
    getFragmentManager.beginTransaction().replace(R.id.collection_container,collectionFrag).commit()
  }
  def check(v: View) {
    if(null!=collectionFrag) collectionFrag.check(v)
  }

  def submit(v: View) {
    if(null!=collectionFrag) collectionFrag.submit(v)
  }

  def addTag(v:View){
    getFragmentManager.beginTransaction().add(R.id.collection_container,new TagFragment()).addToBackStack("tags").commit()
  }
}

class CollectionFragment extends DoubanFragment[CollectionActivity]{
  var status="wish"
  var privacy="public" //private
  val mapping=Map("read"-> R.id.read, "reading"-> R.id.reading,  "wish" -> R.id.wish)
  val reverseMapping=mapping.map(_.swap)
  var collection:Collection=null

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, b: Bundle): View =inflater.inflate(R.layout.collection,container,false)

  override def onActivityCreated(b: Bundle) {
    super.onActivityCreated(b)
    getThisActivity.replaceActionBar(R.layout.header_edit,getString(R.string.add_collection))
    val book=getThisActivity.book
    collection =  book.current_user_collection
    if (null != collection) {
      updateCollection(collection)
    } else {
      val id = getActivity.getIntent.getExtras.getInt(Constant.STATE_ID)
      check(getView.find[Button](if (0 == id) R.id.wish else id))
      future {
        getThisActivity.getAccessToken
        Book.collectionOf(book.id)
      }onSuccess{case c=>{
        collection= book.updateCollection(c)
        updateCollection(c)
      }}
    }
  }

  def updateCollection(collection: Collection) {
    val currentStatus = getView.find[Button](mapping(collection.status))
    check(currentStatus)
    getView.find[EditText](R.id.comment).setText(collection.comment)
    getView.find[RatingBar](R.id.rating).setRating(collection.rating.value.toFloat)
    val tagsContainer: LinearLayout = getView.find[LinearLayout](R.id.tags_container)
    collection.tags.asScala.foreach(tag => tagsContainer.addView(tag))
  }

  def check(v: View) {
    v match {
      case b: Button => {
        val mark = '✓'
        status=reverseMapping(b.getId)
        b.setText(b.getText + mark.toString)
        List(R.id.read, R.id.reading, R.id.wish).filter(_ != b.getId).foreach(id => {
          val button = getView.find[Button](id)
          button.setText(button.getText.toString.takeWhile(_ != mark))
        })
      }
    }
  }

  def submit(v:View){
    val layout=getView.find[LinearLayout](R.id.tags_container)
    val tags =(0 until layout.getChildCount).map(i=>layout.getChildAt(i).asInstanceOf[TextView].getText).toSet.mkString(" ")
    val p=CollectionPosted(status,tags,getView.find[EditText](R.id.comment).getText.toString.trim,getView.find[RatingBar](R.id.rating).getNumStars,privacy)
    future(Book.postCollection(getThisActivity.book.id,p)) onComplete{
      case Success(Some(c:Collection))=>{
        toast(R.string.collect_successfully)
//        getActivity.getIntent.putExtra(Constant.UPDATED,true)
        getThisActivity.getFragmentManager.beginTransaction().remove(this).commit()
      } case Failure(c)=>toast(R.string.collect_failed)
    }
  }

}

class TagFragment extends DoubanFragment[CollectionActivity]{
  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, b: Bundle): View =inflater.inflate(R.layout.add_tags,container,false)

  override def onViewCreated(view: View, savedInstanceState: Bundle) {
    super.onViewCreated(view, savedInstanceState)
    getThisActivity.book.tags
    Book.tagsOf(defaultSharedPreferences.getLong(Constant.userIdString,0))//TODO
  }
}
