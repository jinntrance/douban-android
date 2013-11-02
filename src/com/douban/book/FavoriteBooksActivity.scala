package com.douban.book

import com.douban.base._
import android.view._
import android.os.Bundle
import scala.concurrent._
import com.douban.models._
import  ExecutionContext.Implicits.global
import org.scaloid.common._
import android.widget._
import scala.Some
import scala.util.Success
import com.douban.models.CollectionSearchResult
import scala.Some
import com.douban.models.CollectionSearch
import scala.util.Success
import com.douban.models.Collection

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
    th.addTab(th.newTabSpec("read").setIndicator("已读").setContent(R.id.read_container))
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

  def filter(v:View){
    fragmentManager.beginTransaction().replace(R.id.fav_books_container,new FavoriteBooksListFragment,Constant.FRAGMENT_FAV_BOOKS).addToBackStack(null).commit()
  }

  def submitFilter(m:MenuItem){
    fragmentManager.findFragmentByTag(Constant.FRAGMENT_FAV_BOOKS) match{
      case f:FavoriteBooksListFragment=>f.submitFilter()
      case _=>
    }
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
  var header:View=null

  def submitFilter(){
     //TODO
  }

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    header=inflater.inflate(R.layout.fav_books_result,null)
    super.onCreateView(inflater, container, savedInstanceState)
  }

  override def onActivityCreated(b: Bundle){
    super.onActivityCreated(b)
    getListView.addHeaderView(header)
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

class FavoriteBooksFilterActivity extends DoubanActivity{
  protected override def onCreate(b: Bundle): Unit = {
    super.onCreate(b)
    setContentView(R.layout.fav_books_filter)
    future{
      Book.tagsOf(currentUserId)
    }onSuccess{
      case t:TagsResult=>runOnUiThread({
        val container=find[LinearLayout](R.id.tags_container)
        t.tags.foreach(tag=>container.addView(new SLinearLayout{
          SCheckBox(tag.toString)
        }))
      })
      case _=>
    }
  }
}


