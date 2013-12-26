package com.douban.book

import com.douban.base.{DoubanFragment, Constant, DoubanActivity}
import android.os.Bundle
import com.douban.models.{ReviewRating, Book, CollectionPosted, Collection}
import android.widget._
import android.view.{ViewGroup, LayoutInflater, View}
import scala.concurrent._
import ExecutionContext.Implicits.global
import org.scaloid.common._
import scala.Some
import scala.util.Success
import android.content.Context
import android.widget.MultiAutoCompleteTextView.CommaTokenizer
import android.text.{Editable, TextWatcher}

/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 9/29/13 2:47 AM
 * @version 1.0
 */
class CollectionActivity extends DoubanActivity {
  def getTags={
    getIntent.getExtras.getString(Constant.TAGS,"")
  }

  def setTags(tags:String)={
    getIntent.putExtra(Constant.TAGS,tags)
    find[TextView](R.id.tags_txt).setText(tags)
  }


  override def onBackPressed(){
    //restore the action bar of CollectionActivity
    replaceActionBar(R.layout.header_edit_collection, getString(R.string.add_collection))
    super.onBackPressed()
  }

  lazy val  collectionFrag: Option[CollectionFragment] = Some(findFragment[CollectionFragment](R.id.collectionFragment))
  lazy val book: Option[Book] = getIntent.getSerializableExtra(Constant.BOOK_KEY) match{
    case Some(bk:Book)=>Some(bk)
    case _=>None
  }


  protected override def onCreate(b: Bundle) {
    super.onCreate(b)
    currentUserId
    setContentView(R.layout.collection_container)
  }

  def check(v: View) = collectionFrag match {
    case Some(cf) => cf.check(v)
    case None =>
  }

  def submit(v: View) =  fragmentManager.findFragmentByTag(Constant.ACTIVITY_TAG) match{
      case t:TagFragment=>t.tagsAdded()
      case _=> collectionFrag match {
        case Some(cf) => cf.submit(v)
        case _ =>
    }
  }

  def checkPrivacy(v: View) = collectionFrag match {
    case Some(cf) => cf.checkPrivacy(v)
    case None =>
  }

  var fragment: TagFragment = null

  def addTag(v: View) {
    fragment=new TagFragment()
    fragmentManager.beginTransaction().add(R.id.collection_container, fragment,Constant.ACTIVITY_TAG).addToBackStack(null).commit()
  }
}

class CollectionFragment extends DoubanFragment[CollectionActivity] {
  var status = "wish"
  var public = true
  val reverseMapping = SearchResult.idsMap.map(_.swap)
  var updateable=false


  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, b: Bundle): View = inflater.inflate(R.layout.collection, container, false)

  override def onActivityCreated(b: Bundle) {
    super.onActivityCreated(b)
    val counter=getView.find[TextView](R.id.chars_count)
    if(activity.getIntent.getBooleanExtra(Constant.PUBLIC,false))
      checkPrivacy(rootView.findViewById(R.id.privacy))
    activity.book match {
      case Some(bk:Book) => bk.current_user_collection match {
        case c: Collection =>
          updateable=true
          updateCollection(c)
          counter.setText(c.comment.length.toString)
        case _ =>
          val id = getActivity.getIntent.getExtras.getInt(Constant.STATE_ID)
          check(getView.find[Button](if (0 == id) R.id.wish else id))
          future {
            activity.getAccessToken
            updateCollection(activity.book.getOrElse(bk).updateExistCollection(Book.collectionOf(bk.id)))
          }
      }
      case None =>
    }
    getView.find[EditText](R.id.comment).addTextChangedListener(new  TextWatcher() {
      def beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int): Unit = { }
      def afterTextChanged(s: Editable): Unit = {
        counter.setText(s.toString.length.toString)
      }
      def onTextChanged(s: CharSequence, start: Int, before: Int, count: Int): Unit = {}
    })
  }


  override def onResume(): Unit = {
    super.onResume()
    activity.replaceActionBar(R.layout.header_edit_collection, getString(R.string.add_collection))
  }

  def updateCollection(collection: Collection) {
    val currentStatus = getView.find[Button](SearchResult.idsMap(collection.status))
    check(currentStatus)
    getView.find[EditText](R.id.comment).setText(collection.comment)
    collection.rating match {
      case rat: ReviewRating => getView.find[RatingBar](R.id.rating).setRating(rat.value.toInt)
      case _ =>
    }
    activity.setTags(activity.getTags match{
      case tags:String if tags.nonEmpty=>tags
      case _=> collection.tags.mkString(" ")
    })

  }

  def check(v: View) {

    v match {
      case b: Button =>
        val mark = '✓'
        val txt: String = b.getText.toString
        if (!txt.contains(mark)) {
          status = reverseMapping(b.getId)
          activity.getIntent.putExtra(Constant.STATE_ID,status)
          b.setText(txt + mark.toString)
          b.setBackgroundResource(R.drawable.button_gray)
          List(R.id.read, R.id.reading, R.id.wish).filter(_ != b.getId).foreach(id => {
            getView.find[Button](id) match {
              case b: Button =>
                b.setText(b.getText.toString.takeWhile(_ != mark))
                b.setBackgroundResource(SearchResult.drawableMap(b.getId))
              case _ =>
            }
          }
          )
        }
        rootView.findViewById(R.id.rating).setVisibility(if (v.getId == R.id.wish) View.GONE else View.VISIBLE)
    }
  }

  def checkPrivacy(v: View) {
      public = toggleBackGround(public, v, (R.drawable.private_icon, R.drawable.public_icon))
      activity.getIntent.putExtra(Constant.PUBLIC,public)
  }

  def submit(v: View) {
    val p = CollectionPosted(status, activity.getTags, getView.find[EditText](R.id.comment).getText.toString.trim, getView.find[RatingBar](R.id.rating).getRating.toInt, privacy = if (public) "public" else "private")
    future{
      if(updateable)  Book.updateCollection(activity.book.get.id,p)
      else Book.postCollection(activity.book.get.id, p)
    } onComplete {
      case Success(Some(c: Collection)) =>
        activity.getIntent.putExtra(Constant.COLLECTION,c)
        toast(getString(R.string.collect_successfully))
        activity.finish()
      case _ => toast(getString(R.string.collect_failed))
    }
  }
}

class TagFragment extends DoubanFragment[CollectionActivity] {
  lazy val tags_input = rootView.find[MultiAutoCompleteTextView](R.id.tags_multi_text)

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, b: Bundle): View = inflater.inflate(R.layout.add_tags, container, false)



  override def onActivityCreated(bundle: Bundle) {
    super.onActivityCreated(bundle)
    activity.replaceActionBar(R.layout.header_edit, getString(R.string.add_tags))
      future {
        val r = Book.tagsOf(activity.currentUserId)
        rootView.find[ListView](R.id.my_tags_list).setAdapter(new TagAdapter(r.tags.map(_.title)))
    }
    val popTagsAdapter = new TagAdapter(activity.book.get.tags.map(_.title))
    rootView.find[ListView](R.id.pop_tags_list).setAdapter(popTagsAdapter)
    tags_input.setTokenizer(new CommaTokenizer())

    val th=rootView.find[TabHost](R.id.tabHost)
    th.setup()
    th.addTab(th.newTabSpec("tab1").setIndicator("热门标签").setContent(R.id.pop_tags))
    th.addTab(th.newTabSpec("tab2").setIndicator("我的标签").setContent(R.id.my_tags))

    tags_input.append(activity.getTags)
  }

  class TagAdapter(tags: java.util.List[String]) extends BaseAdapter {
    lazy val inflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE).asInstanceOf[LayoutInflater]

    override def getView(position: Int, view: View, parent: ViewGroup): View = {
      val convertView = view match {
        case v: View =>
          toggleDisplayWhen(R.id.checker,tags_input.getText.toString.contains(getItem(position)),view)
          view
        case _ => inflater.inflate(R.layout.add_tags_item, parent, false)
      }
      convertView.findViewById(R.id.tag_container).onClick(v => {
        val txt = tags_input.getText.toString
        val tag = getItem(position).toString
        val view = v.findViewById(R.id.checker)
        if (txt.indexOf(tag)>=0) {
          view.setVisibility(View.GONE)
          tags_input.setText(txt.replaceAll(tags.get(position),"").replaceAll("  "," "))
        } else {
          view.setVisibility(View.VISIBLE)
          tags_input.append(s" $tag")
        }
      })
      convertView.find[TextView](R.id.tag).setText(tags.get(position))

      convertView
    }

    def getItem(p1: Int): String = tags.get(p1)

    def getItemId(p1: Int): Long = p1

    def getCount: Int = tags.size()
  }

  def tagsAdded()={
    activity.setTags(tags_input.getText.toString)
    activity.onBackPressed()
  }
}
