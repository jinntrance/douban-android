package com.douban.book

import java.util

import android.app
import android.app.ActionBar
import android.app.ActionBar.Tab
import android.os.Bundle
import android.support.v4.app._
import android.support.v4.view.ViewPager
import android.view._
import android.widget._
import com.douban.base._
import com.douban.models.{Collection, CollectionSearch, CollectionSearchResult, _}
import org.scaloid.common._
import org.scaloid.support.v4.SListFragment

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.util.{Failure, Success}

/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 10/7/13 1:25 AM
 * @version 1.0
 */

class FavoriteBooksActivity extends DoubanActivity {
  lazy val waiting = waitToLoad()
  val status = Array("wish", "reading", "read")
  val statusMap = Map(1 -> R.string.wish, 2 -> R.string.reading, 3 -> R.string.read)
  var collectionMap = collection.mutable.HashMap.empty[Int, (Long, util.ArrayList[Collection])]
  var currentTab = 1

  override def onCreate(b: Bundle) {
    super.onCreate(b)
    setContentView(R.layout.fav_books)
    val viewPager = find[ViewPager](R.id.fav_pager)
    val pageAdapter = new FavoritePagerAdapter(fragmentManager, this)
    viewPager.setAdapter(pageAdapter)
    val actionBar = getActionBar
    actionBar.setHomeButtonEnabled(false)
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS)

    val tabListener = new ActionBar.TabListener() {

      override def onTabSelected(tab: Tab, ft: app.FragmentTransaction): Unit = {
        viewPager.setCurrentItem(tab.getPosition)
        fragOf(tab.getPosition).firstLoad
      }

      def fragOf(index: Int): FavoriteBooksListFragment = {
        pageAdapter.getItem(index).asInstanceOf[FavoriteBooksListFragment]
      }

      override def onTabReselected(tab: Tab, ft: app.FragmentTransaction): Unit = {
        val index = tab.getPosition
        collectionMap.get(index).map {
          case (total, list) =>
            fragOf(index).addData(total, list).notifyDataSetChanged()
        }
      }

      override def onTabUnselected(tab: Tab, ft: app.FragmentTransaction): Unit = {
        val index = tab.getPosition
        val frag = fragOf(index)
        collectionMap += (index ->(frag.adapter.getTotal, frag.adapter.getItems))
        frag.adapter.resetData
      }
    }
    statusMap.values.map(actionBar.newTab().setText(_).setTabListener(tabListener)).foreach(actionBar.addTab)

    viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
      override def onPageSelected(position: Int): Unit = {
        actionBar.setSelectedNavigationItem(position)
      }
    })

    Future {
      Book.collectionsOfUser(currentUserId).total
    } onSuccess {
      case (total: Int) =>
        put(Constant.COLLE_NUM, total)
      //setViewValue(R.id.menu_favbooks, s"${getString(R.string.favorite)}($total)", slidingMenu)
    }

  }

  def submitFilter(m: MenuItem) {
    startActivity(SIntent[FavoriteBooksFilterResultActivity])
  }

  override def onCreateOptionsMenu(menu: Menu) = {
    getMenuInflater.inflate(R.menu.filter, menu)
    super.onCreateOptionsMenu(menu)
  }
}

class CollectionItemAdapter(status: String, loader: (String, CollectionItemAdapter) => Unit,
                            mapping: Map[Int, Any] = CollectionItemAdapter.map)(implicit activity: DoubanActivity)
  extends ItemAdapter[Collection](R.layout.fav_books_item, mapping) {
  lazy val listener = (parent: AdapterView[_], view: View, position: Int, id: Long) => {
    parent.getAdapter.asInstanceOf[CollectionItemAdapter].getItem(position) match {
      case c: Collection =>
        val book = c.book.copy()
        val col = c.copy()
        col.updateBook(null)
        book.updateExistCollection(col)
        activity.startActivity(SIntent[BookActivity].putExtra(Constant.BOOK_KEY, Some(book)))
    }
  }
  var currentPage = 0

  override def getView(position: Int, view: View, parent: ViewGroup): View = {
    super.getView(position, view, parent) match {
      case v: View =>
        val c: Collection = getItem(position)
        activity.loadImageWithTitle(c.book.image, R.id.book_img, c.book.title, v)
        activity.setViewValue(R.id.recommend, SearchResult.getStar(c.rating), v)
        activity.setViewValue(R.id.tags_txt, c.tags.mkString(" "), v, hideEmpty = false)
        v
      case _ => null
    }
  }

  override protected def selfLoad(): Unit = loader(status, this)
}

object CollectionItemAdapter {
  val map = Map(R.id.time -> "updated", R.id.bookTitle -> "book.title", R.id.bookAuthor -> List("book.author",
    "book.translator"), R.id.bookPublisher -> "book.publisher")
}

class FavoritePagerAdapter(fm: FragmentManager, ctx: FavoriteBooksActivity) extends FragmentPagerAdapter(fm) {

  override def getItem(index: Int): Fragment = {
    val frag = new FavoriteBooksListFragment
    val b=DBundle().put(Constant.READING_STATUS, ctx.status(index))
    b.putLong(Constant.USER_ID,ctx.currentUserId)
    b.putInt(Constant.COUNT_PER_PAGE,ctx.countPerPage)
    frag.setArguments(b)
    frag
  }

  override def getCount: Int = 3

  override def getPageTitle(position: Int): CharSequence = {
    ctx.getResources.getString(ctx.statusMap.getOrElse(position, R.string.reading))
  }
}

class FavoriteBooksListFragment extends SListFragment {

  implicit def thisActivity = getActivity.asInstanceOf[FavoriteBooksActivity]

  lazy val adapter = new CollectionItemAdapter(getArguments.getString(Constant.READING_STATUS, "reading"), load)(thisActivity)
  lazy val thisStatus = getArguments.getString(Constant.READING_STATUS, "reading")

  override def onActivityCreated(b: Bundle): Unit = {
    super.onActivityCreated(b)
    getListView.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS)
    setListAdapter(adapter)
  }

  def firstLoad = {
    load(thisStatus, adapter)
    this
  }

  def addData(total: Long, list: util.ArrayList[Collection]) = {
    adapter.addResult(total, list.size(), list)
    adapter
  }

  def load(status: String, adapter: CollectionItemAdapter) = {
    Future {
      val cs = CollectionSearch(status, start = adapter.count, count = getArguments.getInt(Constant.COUNT_PER_PAGE,12).toInt)
      Book.collectionsOfUser(getArguments.getLong(Constant.USER_ID,0), cs)
    } onComplete {
      case Success(r: CollectionSearchResult) => runOnUiThread {
        adapter.addResult(r.total, r.collections.size, r.collections)
        adapter.notifyDataSetChanged()
      }
      case Failure(m) =>
      println(m.getMessage)
    }
  }

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, b: Bundle): View = {
    super.onCreateView(inflater, container, b)
  }
}





