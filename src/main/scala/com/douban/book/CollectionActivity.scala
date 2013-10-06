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

/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 9/29/13 2:47 AM
 * @version 1.0
 */
class CollectionActivity extends DoubanActivity {
  var collectionFrag: Option[CollectionFragment] = None
  lazy val book: Option[Book] = getIntent.getSerializableExtra(Constant.BOOK_KEY) match{
    case bk:Book=>Some(bk)
    case _=>None
  }
  var tags=""

  protected override def onCreate(b: Bundle) {
    super.onCreate(b)
    setContentView(R.layout.collection_container)
    collectionFrag = Some(new CollectionFragment())
    getFragmentManager.beginTransaction().replace(R.id.collection_container, collectionFrag.get).commit()
  }

  def check(v: View) = collectionFrag match {
    case Some(cf) => cf.check(v)
    case None =>
  }

  def submit(v: View) = collectionFrag match {
    case Some(cf) => cf.submit(v)
    case None =>
  }

  def checkPrivacy(v: View) = collectionFrag match {
    case Some(cf) => cf.checkPrivacy(v)
    case None =>
  }

  var fragment: TagFragment = null

  def addTag(v: View) {
    fragment=new TagFragment()
    getFragmentManager.beginTransaction().replace(R.id.collection_container, fragment).addToBackStack("").commit()
  }

  def tagsAdded(v:View){
     fragment.tagsAdded
  }
}

class CollectionFragment extends DoubanFragment[CollectionActivity] {
  var status = "wish"
  var public = true
  val reverseMapping = CollectionActivity.idsMap.map(_.swap)

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, b: Bundle): View = inflater.inflate(R.layout.collection, container, false)

  override def onActivityCreated(b: Bundle) {
    super.onActivityCreated(b)
    getThisActivity.replaceActionBar(R.layout.header_edit, getString(R.string.add_collection))
    getThisActivity.book match {
      case Some(bk:Book) => bk.current_user_collection match {
        case c: Collection => updateCollection(c)
        case _ => {
          val id = getActivity.getIntent.getExtras.getInt(Constant.STATE_ID)
          check(getView.find[Button](if (0 == id) R.id.wish else id))
          future {
            getThisActivity.getAccessToken
            updateCollection(getThisActivity.book.getOrElse(bk).updateCollection(Book.collectionOf(bk.id)))
          }
        }
      }
      case None =>
    }
  }

  lazy val tagsContainer: LinearLayout = getView.find[LinearLayout](R.id.tags_container)

  def updateCollection(collection: Collection) {
    val currentStatus = getView.find[Button](CollectionActivity.idsMap(collection.status))
    check(currentStatus)
    getView.find[EditText](R.id.comment).setText(collection.comment)
    collection.rating match {
      case rat: ReviewRating => getView.find[RatingBar](R.id.rating).setRating(rat.value.toInt)
      case _ =>
    }
    getThisActivity.tags=collection.tags.mkString(" ")
    collection.tags.foreach(tagsContainer addView string2Button(_))
  }

  def check(v: View) {

    v match {
      case b: Button => {
        val mark = '✓'
        val txt: String = b.getText.toString
        if (!txt.contains(mark)) {
          status = reverseMapping(b.getId)
          b.setText(txt + mark.toString)
          b.setBackgroundResource(R.drawable.button_gray)
          List(R.id.read, R.id.reading, R.id.wish).filter(_ != b.getId).foreach(id => {
            getView.find[Button](id) match {
              case b: Button => {
                b.setText(b.getText.toString.takeWhile(_ != mark))
                b.setBackgroundResource(CollectionActivity.colorMap(b.getId))
              }
              case _ =>
            }
          }
          )
        }
        rootView.findViewById(R.id.rating).setVisibility(if (v.getId == R.id.wish) View.GONE else View.VISIBLE)
      }
    }
  }

  def checkPrivacy(v: View) {
    runOnUiThread(public = toggleBackGround(public, v, (R.drawable.private_icon, R.drawable.public_icon)))
  }

  def submit(v: View) {
    val layout = getView.find[LinearLayout](R.id.tags_container)
    val tags = (0 until layout.getChildCount).map(i => layout.getChildAt(i).asInstanceOf[TextView].getText).toSet.mkString(" ")
    val p = CollectionPosted(status, tags, getView.find[EditText](R.id.comment).getText.toString.trim, getView.find[RatingBar](R.id.rating).getNumStars, privacy = if (public) "public" else "private")
    future(Book.postCollection(getThisActivity.book.get.id, p)) onComplete {
      case Success(Some(c: Collection)) => {
        toast(getString(R.string.collect_successfully))
        getThisActivity.getFragmentManager.beginTransaction().remove(this).commit()
      }
      case _ => toast(getString(R.string.collect_failed))
    }
  }

  def updateTags(tags:String)={
    tagsContainer.removeAllViews()
    tags.split(' ').foreach(tagsContainer addView string2Button(_))
  }
}

class TagFragment extends DoubanFragment[CollectionActivity] {
  lazy val tags_input = rootView.find[MultiAutoCompleteTextView](R.id.tags_multi_text)

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, b: Bundle): View = inflater.inflate(R.layout.add_tags, container, false)



  override def onActivityCreated(bundle: Bundle) {
    super.onActivityCreated(bundle)
    getThisActivity.replaceActionBar(R.layout.header_tag, getString(R.string.add_tags))
      future {
        val r = Book.tagsOf(defaultSharedPreferences.getString(Constant.userIdString, "0").toLong)
        rootView.find[ListView](R.id.my_tags_list).setAdapter(new TagAdapter(r.tags.map(_.title)))
    }
    val popTagsAdapter = new TagAdapter(getThisActivity.book.get.tags.map(_.title))
//    val popTagsAdapter = new ArrayAdapter[String](getThisActivity,android.R.layout.simple_dropdown_item_1line,getThisActivity.book.get.tags.map(_.title))
    rootView.find[ListView](R.id.pop_tags_list).setAdapter(popTagsAdapter)
    tags_input.setTokenizer(new CommaTokenizer())

    val th=rootView.find[TabHost](R.id.tabHost)
    th.setup()
    th.addTab(th.newTabSpec("tab1").setIndicator("热门标签").setContent(R.id.pop_tags))
    th.addTab(th.newTabSpec("tab2").setIndicator("我的标签").setContent(R.id.my_tags))
  }

  class TagAdapter(tags: java.util.List[String]) extends BaseAdapter {
    lazy val inflater = getThisActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE).asInstanceOf[LayoutInflater]

    override def getView(position: Int, view: View, parent: ViewGroup): View = {
      val convertView = view match {
        case v: View => view
        case _ => inflater.inflate(R.layout.add_tags_item, parent, false)
      }
      convertView.findViewById(R.id.tag_container).onClick(v => {
        val txt = tags_input.getText.toString
        val tag = getItem(position).toString
        val view = v.findViewById(R.id.checker)
        if (txt.indexOf(tag)>=0) {
          view.setVisibility(View.GONE)
          tags_input.setText(txt.replaceAll(tags.get(position)+" ",""))
        } else {
          view.setVisibility(View.VISIBLE)
          tags_input.append(s"$tag ")
        }
      })
      convertView.find[TextView](R.id.tag).setText(tags.get(position))

      convertView
    }

    def getItem(p1: Int): AnyRef = tags.get(p1)

    def getItemId(p1: Int): Long = p1

    def getCount: Int = tags.size()
  }

  def tagsAdded={
    getThisActivity.tags=rootView.find[MultiAutoCompleteTextView](R.id.tags_multi_text).getText.toString
    getFragmentManager.beginTransaction().remove(this).commit()
  }
}
object CollectionActivity{
  val idsMap = Map("read" -> R.id.read, "reading" -> R.id.reading, "wish" -> R.id.wish)
  val colorMap=Map(R.id.wish->R.drawable.button_pink,R.id.reading->R.drawable.button_green,R.id.read->R.drawable.button_brown)
}
