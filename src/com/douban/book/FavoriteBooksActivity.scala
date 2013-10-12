package com.douban.book

import com.douban.base._
import android.view.{View, ViewGroup, LayoutInflater}
import android.os.Bundle
import scala.concurrent._
import com.douban.models.{Collection, CollectionSearchResult, CollectionSearch, Book}
import scala.util.Success
import  ExecutionContext.Implicits.global
import org.scaloid.common._
import android.widget.{TabHost, ListView, TextView}
import com.douban.base.DBundle
import com.douban.models.CollectionSearchResult
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
  override def onCreate(b: Bundle){
    super.onCreate(b)
    setContentView(R.layout.fav_books)
    val th=find[TabHost](R.id.tabHost)
    th.setup()
    th.addTab(th.newTabSpec("wish").setIndicator("想读").setContent(R.id.wish_container))
    th.addTab(th.newTabSpec("reading").setIndicator("在读").setContent(R.id.reading_container))
    th.addTab(th.newTabSpec("read").setIndicator("已读").setContent(R.id.read_container))
    th.setCurrentTab(1)
    val readingAdapter: CollectionItemAdapter = new CollectionItemAdapter("reading", load)
    find[ListView](R.id.reading).setAdapter(readingAdapter)
    load("reading",readingAdapter)
    val wishAdapter: CollectionItemAdapter = new CollectionItemAdapter("wish", load)
    find[ListView](R.id.wish).setAdapter(wishAdapter)
    load("wish",wishAdapter)
    val readAdapter: CollectionItemAdapter = new CollectionItemAdapter("read", load)
    find[ListView](R.id.read).setAdapter(readAdapter)
    load("read",readAdapter)
  }

  def filter(v:View){
    fragmentManager.beginTransaction().replace(R.id.fav_books_container,new FavoriteBooksListFragment,Constant.FRAGMENT_FAV_BOOKS).addToBackStack(null).commit()
  }

  def submitFilter(v:View){
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
      }
      case _=>
    }
  }

  def viewBook(v:View){
    startActivity(SIntent[BookActivity].putExtra(Constant.BOOK_ID,v.find[TextView](R.id.book_id).getText.toString))
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
      Book.collectionsOfUser(getThisActivity.currentUserId,cs)
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

class CollectionItemAdapter(status:String,loader: (String,CollectionItemAdapter)=> Unit,mapping:Map[Int,Any]=Map( R.id.time->"updated",R.id.book_id->"book.id",
  R.id.bookTitle -> "book.title", R.id.bookAuthor -> List("book.author", "book.translator"),R.id.bookPublisher->"book.publisher"))(implicit activity: DoubanActivity) extends ItemAdapter[Collection](R.layout.fav_books_item,mapping) {
  var currentPage=0
  override def getView(position: Int, view: View, parent: ViewGroup): View = {
    super.getView(position, view, parent) match{
      case  v:View=>{
        val c: Collection = getBean(position)
        activity.loadImageWithTitle(c.book.image, R.id.book_img, c.book.title, v)
        activity.setViewValue(R.id.recommend,SearchResult.getStar(c.rating))
        activity.setViewValue(R.id.tags_txt,c.tags.mkString(" "))
        v
      }
      case _=>null
    }
  }



  override protected def selfLoad(): Unit = loader(status,this)
}


