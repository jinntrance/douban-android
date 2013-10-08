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
    fragmentManager.beginTransaction().replace(R.id.reading_container,new FavoriteBooksListFragment).
    replace(R.id.wish_container,new FavoriteBooksListFragment().addArguments(DBundle().put(Constant.READING_STATUS,"wish"))).
    replace(R.id.read_container,new FavoriteBooksListFragment() .addArguments(DBundle().put(Constant.READING_STATUS,"read"))).commit()
  }

  def filter(v:View){
//    findFragment[FavoriteBooksListFragment](R.id.list)
  }
}


class FavoriteBooksListFragment extends DoubanListFragment[DoubanActivity]{

  var currentPage=0
  var status="reading"
  var bookTag=""
  var rating=0
  lazy val adapters=List(R.id.wish,R.id.reading,R.id.read).map(e=> e -> {
    val a = new CollectionItemAdapter(mapping, load())
    getView.find[ListView](e).setAdapter(a)
    a
  }).toMap

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, b: Bundle): View = inflater.inflate(R.layout.fav_books_item,container,false)
  val mapping=Map(R.id.time->"updated")++SearchResult.mapping.map{case (k,v)=>(k,"book."+v)}

  override def onActivityCreated(b: Bundle){
    super.onActivityCreated(b)
    adapters
    load()
  }
  def load(page:Int=currentPage){
    future{
      getThisActivity.getAccessToken
      Book.collectionsOfUser(getThisActivity.currentUserId,new CollectionSearch(getArguments.getString(Constant.READING_STATUS,status),tag,rating))
    } onComplete{
      case Success(r:CollectionSearchResult)=>{
          currentPage+=1
          val adapter=adapters(SearchResult.idsMap.getOrElse(status,R.id.reading))
          adapter.addResult(r.total,r.collections.size,r.collections)
          if(1==currentPage) adapter.notifyDataSetInvalidated()
          else adapter.notifyDataSetChanged()
      }
      case _=>
    }
  }
  }
class CollectionItemAdapter(mapping:Map[Int,Any],load: => Unit)(implicit activity: DoubanActivity) extends ItemAdapter[Collection](R.layout.fav_books_item,mapping,load=load) {
  override def getView(position: Int, view: View, parent: ViewGroup): View = {
    super.getView(position, view, parent) match{
      case  v:View=>{
        val c: Collection = getBean(position)
        v.find[TextView](R.id.recommend).setText(SearchResult.getStar(c.rating))
        v.find[TextView](R.id.tags_txt).setText(c.tags.mkString(" "))
        v
      }
      case _=>null
    }
  }
}
