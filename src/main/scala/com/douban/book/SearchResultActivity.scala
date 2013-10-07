package com.douban.book

import com.douban.base._
import android.os.Bundle
import android.widget._
import android.view._
import com.douban.models.{Collection, Book, BookSearchResult}
import scala.concurrent._
import org.scaloid.common._
import ExecutionContext.Implicits.global
import java.lang.String
import Constant._
import scala.util.Failure
import scala.util.Success
import java.util
import scala.collection
import scala.collection.mutable
import com.google.zxing.client.android.Intents.SearchBookContents

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
  }

  def updateTitle() {
    setTitle(getString(R.string.search_result, searchText))
  }
}

class SearchResultList extends DoubanListFragment[SearchResultActivity] {
  var loading = false
  var footer: Option[View] = None
  private var currentPage = 0
  private var total = Long.MaxValue
  lazy val adapter: BookItemAdapter = new BookItemAdapter(R.layout.book_list_item, SearchResult.mapping, load = load())

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
    getThisActivity.updateTitle()
    updateFooter()
  }

  def updateFooter() = {
    getThisActivity.find[TextView](R.id.to_load) match {
      case footer: TextView => footer.setText(getString(R.string.swipe_up_to_load).format(adapter.count, total))
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
    getThisActivity.fragmentManager.findFragmentById(R.id.book_fragment) match {
      case bf: SearchResultDetail =>
        bf.updateBookView()
      case _ => {
        getThisActivity.startActivity(SIntent[BookActivity].putExtra(Constant.BOOK_KEY, Some(adapter.getBook(position))))
      }
    }
  }

  def load() = {
    if ((adapter.count < total) && !loading) future {
      currentPage += 1
      loading = true
      Book.search(getThisActivity.searchText, "", currentPage, countPerPage)
    } onSuccess {
      case b => {
        total = b.total
        adapter.addResult(b)
        runOnUiThread {
          adapter.notifyDataSetChanged()
          updateFooter()
        }
        if (adapter.count < total) toast(getString(R.string.more_books_loaded).format(adapter.count))
        else toast(R.string.more_loaded_finished)
        loading = false
      }
    }
  }
}

class BookItemAdapter(layoutId: Int, mapping: Map[Int, Any], list: collection.mutable.Buffer[Map[String, String]] = mutable.Buffer[Map[String, String]](), load: => Unit = {})(implicit activity: DoubanActivity) extends BaseAdapter {

  case class ViewHolder(image: ImageView, title: TextView, authors: TextView, publisher: TextView, stars: ImageView)

  override def getView(position: Int, view: View, parent: ViewGroup): View = {
    import CollectionActivity._
    val convertView = if (null != view) view else activity.getLayoutInflater.inflate(layoutId, null)
    if (null != convertView) {
      val b = bookList.get(position)
      activity.batchSetValues(mapping, list(position), convertView)
      val c: Collection = b.current_user_collection
      activity.displayWhen(R.id.favorite, null == c, convertView)
      if (null != c) {
        convertView.findViewById(R.id.currentState) match {
          case state: TextView => {
            state.setVisibility(View.VISIBLE)
            state.setText(SearchResult.stateMapping(c.status))
            state.setTextColor(colorMap(idsMap(c.status)))
          }
          case _ =>
        }
      } else convertView.findViewById(R.id.fav_layout) onClick (v => {
        activity.startActivity(SIntent[CollectionActivity].putExtra(Constant.BOOK_KEY, Some(b)))
      })
      activity.loadImageWithTitle(b.image, R.id.book_img, b.title, convertView)
      if (position + 2 >= count && count < total) load
    }
    convertView
  }

  var total = Long.MaxValue
  var count = 0
  val bookList: java.util.List[Book] = new java.util.ArrayList[Book]()

  def getCount: Int = count

  def getItem(index: Int): Map[String, String] = list(index)

  def getBook(index: Int): Book = bookList.get(index)

  def getItemId(position: Int): Long = position

  def addResult(r: BookSearchResult) = {
    import collection.JavaConverters._
    bookList.addAll(r.books)
    total = r.total
    list ++= r.books.asScala.map(activity.beanToMap(_))
    count += r.books.size()
  }
}

object SearchResult {
  val STATE_STRING = "current_user_collection.status"
  val stateMapping = Map("wish" -> "想读", "reading" -> "在读", "read" -> "读过")
  val mapping: Map[Int, Any] = Map(
    R.id.bookTitle -> "title", R.id.bookAuthor -> List("author", "translator"), R.id.bookPublisher -> List("publisher", "pubdate"),
    R.id.ratingNum ->("rating.numRaters", "(%s)"), R.id.ratedStars -> "rating.average",
    R.id.currentState -> STATE_STRING
  )
}