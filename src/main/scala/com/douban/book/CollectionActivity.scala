package com.douban.book

import com.douban.base.{DoubanFragment, Constant, DoubanActivity}
import android.os.Bundle
import com.douban.models.{ReviewRating, Book, CollectionPosted, Collection}
import android.widget._
import android.view.{ViewGroup, LayoutInflater, View}
import collection.JavaConverters._
import scala.concurrent._
import ExecutionContext.Implicits.global
import org.scaloid.common._
import scala.Some
import scala.util.Success
import com.douban.book.db.MyTagsHelper
import android.content.Context

/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 9/29/13 2:47 AM
 * @version 1.0
 */
class CollectionActivity extends DoubanActivity {
  var collectionFrag:Option[CollectionFragment]=None
  lazy val book:Option[Book]=SearchResult.selectedBook
  protected override def onCreate(b: Bundle) {
    super.onCreate(b)
    setContentView(R.layout.collection_container)
    collectionFrag=Some(new CollectionFragment())
    getFragmentManager.beginTransaction().replace(R.id.collection_container,collectionFrag.get).commit()
  }
  def check(v: View) = collectionFrag match {
    case Some(cf) =>cf.check(v)
    case None =>
  }


  def submit(v: View) = collectionFrag match {
    case Some(cf) => cf.submit(v)
    case None =>
  }
  def checkPrivacy(v:View)=collectionFrag match {
    case Some(cf) =>cf.checkPrivacy(v)
    case None =>
  }

  def addTag(v:View){
    getFragmentManager.beginTransaction().replace(R.id.collection_container,new TagFragment()).addToBackStack("").commit()
  }
}

class CollectionFragment extends DoubanFragment[CollectionActivity]{
  var status="wish"
  var public=true
  val mapping=Map("read"-> R.id.read, "reading"-> R.id.reading,  "wish" -> R.id.wish)
  val reverseMapping=mapping.map(_.swap)

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, b: Bundle): View =inflater.inflate(R.layout.collection,container,false)

  override def onActivityCreated(b: Bundle) {
    super.onActivityCreated(b)
    getThisActivity.replaceActionBar(R.layout.header_edit,getString(R.string.add_collection))
    getThisActivity.book match {
     case Some(book) =>book.current_user_collection match {
       case c:Collection =>updateCollection(c)
       case _ =>  {
         val id = getActivity.getIntent.getExtras.getInt(Constant.STATE_ID)
         check(getView.find[Button](if (0 == id) R.id.wish else id))
         future {
           getThisActivity.getAccessToken
           updateCollection(book.updateCollection(Book.collectionOf(book.id)))
         }
       }
     }
     case None =>
   }
  }

  def updateCollection(collection: Collection) {
    val currentStatus = getView.find[Button](mapping(collection.status))
    check(currentStatus)
    getView.find[EditText](R.id.comment).setText(collection.comment)
    collection.rating match {
      case rat: ReviewRating =>getView.find[RatingBar](R.id.rating).setRating(rat.value.toInt)
      case _ =>
    }
    getView.find[LinearLayout](R.id.tags_container) addView string2TextView(collection.tags.mkString(" "))
  }

  def check(v: View) {
    v match {
      case b: Button => {
      val mark = '✓'
      val txt: String = b.getText.toString
      if(!txt.contains(mark)) {
          status = reverseMapping(b.getId)
          b.setText(txt + mark.toString)
          List(R.id.read, R.id.reading, R.id.wish).filter(_ != b.getId).foreach(id => {
           getView.find[Button](id) match {
             case b: Button =>b.setText(b.getText.toString.takeWhile(_ != mark))
             case _ =>
           }
          }
        )
      }
      rootView.findViewById(R.id.rating).setVisibility(if(v.getId==R.id.wish) View.GONE else View.VISIBLE)
    }
  }
  }

  def checkPrivacy(v:View) {
    runOnUiThread(public=toggleBackGround(public,v,(R.drawable.private_icon,R.drawable.public_icon)))
  }

  def submit(v:View){
    val layout=getView.find[LinearLayout](R.id.tags_container)
    val tags =(0 until layout.getChildCount).map(i=>layout.getChildAt(i).asInstanceOf[TextView].getText).toSet.mkString(" ")
    val p=CollectionPosted(status,tags,getView.find[EditText](R.id.comment).getText.toString.trim,getView.find[RatingBar](R.id.rating).getNumStars,privacy=if(public) "public" else "private")
    future(Book.postCollection(getThisActivity.book.get.id,p)) onComplete{
      case Success(Some(c:Collection))=>runOnUiThread{
        toast(R.string.collect_successfully)
//        getActivity.getIntent.putExtra(Constant.UPDATED,true)
        getThisActivity.getFragmentManager.beginTransaction().remove(this).commit()
      }
      case _=>toast(R.string.collect_failed)
    }
  }

}

class TagFragment extends DoubanFragment[CollectionActivity]{

  lazy val tags_input=rootView.find[MultiAutoCompleteTextView](R.id.tags_multi_text)

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, b: Bundle): View =inflater.inflate(R.layout.add_tags,container,false)

  override def onViewCreated(view: View, savedInstanceState: Bundle) {
    super.onViewCreated(view, savedInstanceState)
    getThisActivity.replaceActionBar(R.layout.header_tag,getString(R.string.add_tags))
    val helper = MyTagsHelper()
//    val myTags: util.List[String] = helper.findData(15).map(_.title)
//    val myTagsAdapter=new TagAdapter(myTags)

    if(getThisActivity.usingWIfi|| !getThisActivity.using2G) {
      future{
        val r=Book.tagsOf(defaultSharedPreferences.getLong(Constant.userIdString,0))
        runOnUiThread(rootView.find[ListView](R.id.my_tags_list).setAdapter(new TagAdapter(r.tags.map(_.title))))
//        if(myTags.size()==0) myTags.addAll(r.tags.map(_.title))
//        myTagsAdapter.notifyDataSetChanged()
//        r.tags.foreach(helper.insert)
      }
    }
//    rootView.find[ListView](R.id.pop_tags_list).setAdapter(new TagAdapter(getThisActivity.book.tags.map(_.title)))
    tags_input.setAdapter(new ArrayAdapter[String](getThisActivity,android.R.layout.simple_list_item_checked,getThisActivity.book.get.tags.map(_.title)))
    tags_input.setThreshold(0)

  }

  class TagAdapter(tags:java.util.List[String]) extends BaseAdapter {
    lazy val inflater=getThisActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE).asInstanceOf[LayoutInflater]
    override def getView(position: Int, view: View, parent: ViewGroup): View = {
      val convertView = view match {
        case v: View =>  view
        case _ =>  inflater.inflate(R.layout.add_tags_item,parent,false)
      }
      convertView.findViewById(R.id.tag_container).onClick(v=>{
           val txt=tags_input.getText
           val tag=getItem(position)
           val view=v.findViewById(R.id.checker)
           if(txt.toString .contains(tag)) {
             view.setVisibility(View.GONE)
//             txt.toString (tags.get(position),"")
           }  else{
             view.setVisibility(View.VISIBLE)
             tags_input.append(s" $tag")
           }})
       convertView.find[TextView](R.id.tag).setText(tags.get(position))

      convertView
    }

    def getItem(p1: Int): AnyRef = tags.get(p1)

    def getItemId(p1: Int): Long = p1

    def getCount: Int = tags.size()
  }
}
