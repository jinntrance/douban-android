package com.douban.book

import com.douban.base._
import android.os.Bundle
import android.widget._
import android.view._
import com.douban.models._
import org.scaloid.common._
import java.lang.String
import Constant._
import com.douban.models.BookSearchResult
import scala.Some
import com.douban.models.Collection

/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 5/3/13 5:25 PM
 * @version 1.0
 * @see http://developers.douban.com/wiki/?title=api_v2
 */
class SearchResultActivity extends DoubanActivity {
  var searchText = ""

  protected override def onCreate(b: Bundle) = {
    super.onCreate(b)
    setContentView(R.layout.book_list)
    searchText = getIntent.getStringExtra(SEARCH_TEXT_KEY)
    slidingMenu
  }

  def updateTitle(append:String="") {
    setTitle(getString(R.string.search_result, searchText))
  }
}

class SearchResultList extends DoubanListFragment[SearchResultActivity] {
  var loading = false
  var footer: Option[View] = None
  private var currentPage = 1
  private var total = Long.MaxValue
  lazy val adapter: BookItemAdapter = new BookItemAdapter(SearchResult.mapping, load = load())

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, bundle: Bundle) = {
    footer = Some(inflater.inflate(R.layout.book_list_loader, null))
    super.onCreateView(inflater, container, bundle)
  }

  override def onActivityCreated(bundle: Bundle) {
    super.onActivityCreated(bundle)
    getListView.setDivider(null)
    getListView.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS)
    getListView.addFooterView(footer.get)
    setListAdapter(adapter)
    load()
    activity.updateTitle()
  }

  def updateFooter() = {
    activity.find[TextView](R.id.to_load) match {
      case footer: TextView => {
        footer.setText(getString(R.string.swipe_up_to_load).format(adapter.count, total))
        activity.updateTitle(s"(${adapter.count}/$total)")
        footer.setVisibility(View.VISIBLE)
      }
      case _ =>
    }
  }

  override def onStart() {
    super.onStart()
    if (getFragmentManager.findFragmentById(R.id.book_fragment) != null) {
      getListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE)
    }
  }

  override def onListItemClick(l: ListView, v: View, position: Int, id: Long) {
    getListView.setItemChecked(position, true)
    activity.fragmentManager.findFragmentById(R.id.book_fragment) match {
      case bf: SearchResultDetail =>
        bf.updateBookView()
      case _ => {
        activity.startActivity(SIntent[BookActivity].putExtra(Constant.BOOK_KEY, Some(adapter.getBean(position))))
      }
    }
  }

  def load() = {
    activity.listLoader(
      toLoad = (adapter.count < total) && !loading,
      result = {
        loading = true
        Book.search(activity.searchText, "", currentPage, countPerPage)
      },
      success = (b: BookSearchResult) => {
        currentPage += 1
        total = b.total
        adapter.addResult(b.total, b.books.size(), b.books)
        runOnUiThread {
          adapter.notifyDataSetChanged()
          updateFooter()
        }
        if (adapter.count < total) toast(getString(R.string.more_books_loaded).format(adapter.count))
        else toast(R.string.more_loaded_finished)
        loading = false
      })
  }
}

class BookItemAdapter(mapping: Map[Int, Any], load: => Unit = {})(implicit activity: DoubanActivity) extends
    ItemAdapter[Book](R.layout.book_list_item,mapping,load=load) {

  override def getView(position: Int, view: View, parent: ViewGroup): View = {
    val convertView = super.getView(position,view,parent)
    if (null != convertView) {
      val b = getBean(position)
      val c: Collection = b.current_user_collection
      activity.displayWhen(R.id.favorite, null == c, convertView)
      if (null != c) {
        convertView.findViewById(R.id.currentState) match {
          case state: TextView => {
            state.setVisibility(View.VISIBLE)
            state.setText(SearchResult.stateMapping(c.status))
            state.setTextColor(SearchResult.colorMap(SearchResult.idsMap(c.status)))
          }
          case _ =>
        }
      } else convertView.findViewById(R.id.fav_layout) onClick (v => {
        activity.startActivity(SIntent[CollectionActivity].putExtra(Constant.BOOK_KEY, Some(b)))
      })
      activity.loadImageWithTitle(b.image, R.id.book_img, b.title, convertView)

    }
    convertView
  }
}

object SearchResult {
  val STATE_STRING = "current_user_collection.status"
  val stateMapping = Map("wish" -> "想读", "reading" -> "在读", "read" -> "读过")
  val idsMap = Map("read" -> R.id.read, "reading" -> R.id.reading, "wish" -> R.id.wish)
  val str2ids=idsMap.map(_.swap)
  val colorMap=Map(R.id.wish->R.drawable.button_pink,R.id.reading->R.drawable.button_green,R.id.read->R.drawable.button_brown)

  val mapping: Map[Int, Any] = Map(
    R.id.bookTitle -> "title", R.id.bookAuthor -> List("author", "translator"), R.id.bookPublisher -> List("publisher", "pubdate"),
    R.id.ratingNum ->("rating.numRaters", "(%s)"), R.id.ratedStars -> "rating.average"
  )
  def getStar(rat: ReviewRating):String= {
    val r = Array("很差", "较差", "还行", "推荐", "力荐")
    rat match {
      case rat: ReviewRating => {
        val rating = rat.value.toInt
        if (rating > 0 && rating <= 5) rating + "星" + r(rating - 1) else ""
      }
      case _ =>""
    }
  }
}
