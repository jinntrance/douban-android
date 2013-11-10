package com.douban.book

import com.douban.base._
import android.view._
import android.os.Bundle
import scala.concurrent._
import com.douban.models._
import  ExecutionContext.Implicits.global
import org.scaloid.common._
import android.widget._
import com.douban.models.CollectionSearchResult
import scala.Some
import com.douban.models.CollectionSearch
import scala.util.Success
import com.douban.models.Collection
import android.content.Intent
import android.app.Activity
import java.text.SimpleDateFormat
import java.sql.Timestamp
import java.util.Date

/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 10/7/13 1:25 AM
 * @version 1.0
 */
class FavoriteBooksActivity extends DoubanActivity{
  lazy val waiting=waitToLoad()
  lazy val th=find[TabHost](R.id.tabHost)
  var currentTab=1
  override def onCreate(b: Bundle){
    super.onCreate(b)
    setContentView(R.layout.fav_books)
    th.setup()
    th.addTab(th.newTabSpec("wish").setIndicator("想读").setContent(R.id.wish_container))
    th.addTab(th.newTabSpec("reading").setIndicator("在读").setContent(R.id.reading_container))
    th.addTab(th.newTabSpec("read").setIndicator("读过").setContent(R.id.read_container))
    th.setCurrentTab(currentTab)
    val  listener= (parent: AdapterView[_], view: View, position: Int, id: Long)=> {
      parent.getAdapter.asInstanceOf[CollectionItemAdapter].getBean(position) match {
        case c:Collection=>{
          val book=c.book
          c.updateBook(null)
          book.updateExistCollection(c)
          startActivity(SIntent[BookActivity].putExtra(Constant.BOOK_KEY,Some(book)))
        }
      }
    }
    val readingAdapter = new CollectionItemAdapter("reading", load)
    find[ListView](R.id.reading) onItemClick listener setAdapter readingAdapter
    load("reading",readingAdapter)
    val wishAdapter = new CollectionItemAdapter("wish", load)
    find[ListView](R.id.wish) onItemClick listener  setAdapter wishAdapter
    load("wish",wishAdapter)
    val readAdapter = new CollectionItemAdapter("read", load)
    find[ListView](R.id.read) onItemClick listener  setAdapter readAdapter
    load("read",readAdapter)
    waiting
  }

  def submitFilter(m:MenuItem){
    startActivity(SIntent[FavoriteBooksListActivity])
  }


  def load(status:String,adapter:CollectionItemAdapter)={
    future{
      val cs=CollectionSearch(status,start=adapter.count,count=countPerPage)
      Book.collectionsOfUser(currentUserId,cs)
    } onComplete{
      case Success(r:CollectionSearchResult)=>runOnUiThread{
        adapter.addResult(r.total,r.collections.size,r.collections)
        adapter.notifyDataSetChanged()
        if(null!=waiting) waiting.cancel()
      }
      case _=>
    }
  }

  override def onCreateOptionsMenu(menu: Menu) = {
    getMenuInflater.inflate(R.menu.filter, menu)
    super.onCreateOptionsMenu(menu)
  }

  def showNext(): Unit = {
    currentTab=(currentTab+1)%3
    th.setCurrentTab(currentTab)
  }

  def showPre(): Unit = {
    currentTab=(currentTab-1)%3
    th.setCurrentTab(currentTab)
  }
}


class FavoriteBooksListFragment extends DoubanListFragment[DoubanActivity]{
  var currentPage=0
  var status="reading"
  var bookTag=""
  var rating=0
  var collectionSearch=CollectionSearch()
  lazy val adapter=new CollectionItemAdapter("reading",load)

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    super.onCreateView(inflater, container, savedInstanceState)
  }

  override def onActivityCreated(b: Bundle){
    super.onActivityCreated(b)
    getListView.setAdapter(adapter)
  }
  def load(status:String,adapter:CollectionItemAdapter){
    future{
      val cs=CollectionSearch(status,bookTag,rating,start=adapter.count,count=countPerPage)
      Book.collectionsOfUser(activity.currentUserId,cs)
    } onComplete{
      case Success(r:CollectionSearchResult)=>{
          currentPage+=1
          if(1==currentPage) adapter.notifyDataSetInvalidated()
          else adapter.notifyDataSetChanged()
      }
      case _=>
    }
  }

  override def onListItemClick(l: ListView, v: View, position: Int, id: Long) {
    l.setItemChecked(position,true)
    startActivity(SIntent[BookActivity].putExtra(Constant.BOOK_KEY,Some(adapter.getBean(position))))
  }
}

class CollectionItemAdapter(status:String,loader: (String,CollectionItemAdapter)=> Unit,mapping:Map[Int,Any]=Map(R.id.time->"updated", R.id.bookTitle -> "book.title", R.id.bookAuthor -> List("book.author", "book.translator"),R.id.bookPublisher->"book.publisher"))(implicit activity: DoubanActivity)
  extends ItemAdapter[Collection](R.layout.fav_books_item,mapping) {
  var currentPage=0
  override def getView(position: Int, view: View, parent: ViewGroup): View = {
    super.getView(position, view, parent) match{
      case  v:View=>{
        val c: Collection = getBean(position)
        activity.loadImageWithTitle(c.book.image, R.id.book_img, c.book.title, v)
        activity.setViewValue(R.id.recommend,SearchResult.getStar(c.rating),v)
        activity.setViewValue(R.id.tags_txt,c.tags.mkString(" "),v,hideEmpty = false)
        v
      }
      case _=>null
    }
  }



  override protected def selfLoad(): Unit = loader(status,this)
}

class FavoriteBooksListActivity extends DoubanActivity{
  val REQUEST_CODE=1


  protected override def onCreate(b: Bundle): Unit = {
    super.onCreate(b)
    setContentView(R.layout.fav_books_result)
    startActivityForResult(SIntent[FavoriteBooksFilterActivity],REQUEST_CODE)
  }

  def updateHeader(b:Bundle){
    b.keySet().map(key=> key->b.getString(key,"")).toMap
  }

  override def onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
     if(requestCode==REQUEST_CODE&& resultCode== Activity.RESULT_OK){
         updateHeader(data.getExtras)
         fragmentManager.beginTransaction().replace(R.id.fav_books_fragment,
           new FavoriteBooksListFragment addArguments data.getExtras,Constant.FRAGMENT_FAV_BOOKS).addToBackStack(null).commit()
       }
  }
  override def onCreateOptionsMenu(menu: Menu) = {
    getMenuInflater.inflate(R.menu.filter, menu)
    super.onCreateOptionsMenu(menu)
  }
}

class FavoriteBooksFilterActivity extends DoubanActivity{
  private var state=""
  private var tags=collection.mutable.Set[String]()
  protected override def onCreate(b: Bundle): Unit = {
    super.onCreate(b)
    setContentView(R.layout.fav_books_filter)
    replaceActionBar(R.layout.header_edit,getString(R.string.filter_books))
    future{
      Book.tagsOf(currentUserId)
    }onSuccess{
      case t:TagsResult=>runOnUiThread({
        val container=find[LinearLayout](R.id.tags_container)
        container.addView(new SLinearLayout{
          t.tags.foreach(tag=>SCheckBox(tag.toString).onClick(_ match{
            case db:CheckBox=>
              tags=if(db.isChecked) {tags + db.getText.toString}
              else tags - db.getText.toString
            case _=>
          }))
        })
      })
      case _=>
    }
  }

  def submit(v:View){
    val sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ")

    val from=sdf.format(new Date(find[DatePicker](R.id.from_date).getCalendarView.getDate))
    val to=sdf.format(new Date(find[DatePicker](R.id.to_date).getCalendarView.getDate))
    //TODO handle the default min and max date problem
    CollectionSearch(state,tags.mkString(" "),find[RatingBar](R.id.rating).getRating.toInt,from,to)
//    getIntent.putExtras(???)
  }

  def checkSate(v:View){
    state=SearchResult.str2ids.getOrElse(v.getId,"")
  }


}


