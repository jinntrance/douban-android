package com.douban.book

import com.douban.base.{DoubanListFragment, DoubanActivity}
import android.view.{View, ViewGroup, LayoutInflater}
import android.os.Bundle
import scala.concurrent._
import com.douban.models.{CollectionSearchResult, CollectionSearch, Book}
import scala.util.Success
import  ExecutionContext.Implicits.global

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
  }
}


class FavoriteBooksListFragment extends DoubanListFragment[DoubanActivity]{

  var currentPage=1
  var status=""
  var bookTag=""
  var rating=0

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, b: Bundle): View = inflater.inflate(R.layout.fav_books_item,container,false)

  lazy val adapter=new BookItemAdapter(R.layout.fav_books_item,SearchResult.mapping)

  override def onActivityCreated(savedInstanceState: Bundle){
    setListAdapter(adapter)
  }
  def load(){
    future{
      getThisActivity.getAccessToken
      Book.collectionsOfUser(getThisActivity.currentUserId,new CollectionSearch(status,tag,rating))
    } onComplete{
      case Success(r:CollectionSearchResult)=>{
//        adapter.
      }
      case _=> //TODO
    }
  }
}