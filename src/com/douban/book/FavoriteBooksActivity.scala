package com.douban.book

import com.douban.base._
import android.view._
import android.os.Bundle
import scala.concurrent._
import com.douban.models._
import ExecutionContext.Implicits.global
import org.scaloid.common._
import android.widget._
import com.douban.models.CollectionSearchResult
import scala.Some
import com.douban.models.CollectionSearch
import scala.util.Success
import com.douban.models.Collection
import android.content.Intent
import android.app.{Dialog, DatePickerDialog, Activity}
import java.text.SimpleDateFormat
import java.util.{GregorianCalendar, Calendar}
import android.support.v4.app.DialogFragment
import android.widget
import scala.concurrent._

/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 10/7/13 1:25 AM
 * @version 1.0
 */

class FavoriteBooksActivity extends DoubanActivity {
  lazy val waiting = waitToLoad()
  lazy val th = find[TabHost](R.id.tabHost)
  var currentTab = 1

  override def onCreate(b: Bundle) {
    super.onCreate(b)
    setContentView(R.layout.fav_books)
    th.setup()
    th.addTab(th.newTabSpec("wish").setIndicator("想读").setContent(R.id.wish_container))
    th.addTab(th.newTabSpec("reading").setIndicator("在读").setContent(R.id.reading_container))
    th.addTab(th.newTabSpec("read").setIndicator("读过").setContent(R.id.read_container))
    th.setCurrentTab(currentTab)
    val readingAdapter = new CollectionItemAdapter("reading", load)
    find[ListView](R.id.reading) onItemClick readingAdapter.listener setAdapter readingAdapter
    load("reading", readingAdapter)
    val wishAdapter = new CollectionItemAdapter("wish", load)
    find[ListView](R.id.wish) onItemClick wishAdapter.listener setAdapter wishAdapter
    load("wish", wishAdapter)
    val readAdapter = new CollectionItemAdapter("read", load)
    find[ListView](R.id.read) onItemClick readAdapter.listener setAdapter readAdapter
    load("read", readAdapter)
    future{
      Book.collectionsOfUser(currentUserId).total
    }onSuccess{
      case (total:Int)=>
        put(Constant.COLLE_NUM,total)
        setViewValue(R.id.menu_favbooks, s"${getString(R.string.favorite)}($total)", slidingMenu)
    }
    waiting
  }

  def submitFilter(m: MenuItem) {
    startActivity(SIntent[FavoriteBooksListActivity])
  }

  def load(status: String, adapter: CollectionItemAdapter) = {
    future {
      val cs = CollectionSearch(status, start = adapter.count, count = countPerPage)
      Book.collectionsOfUser(currentUserId, cs)
    } onComplete {
      case Success(r: CollectionSearchResult) => runOnUiThread {
        adapter.addResult(r.total, r.collections.size, r.collections)
        adapter.notifyDataSetChanged()
        if (null != waiting) waiting.cancel()
      }
      case _ =>
    }
  }

  override def onCreateOptionsMenu(menu: Menu) = {
    getMenuInflater.inflate(R.menu.filter, menu)
    super.onCreateOptionsMenu(menu)
  }

  def showNext(): Unit = {
    currentTab = (currentTab + 1) % 3
    th.setCurrentTab(currentTab)
  }

  def showPre(): Unit = {
    currentTab = (currentTab - 1) % 3
    th.setCurrentTab(currentTab)
  }
}

class CollectionItemAdapter(status: String, loader: (String, CollectionItemAdapter) => Unit,
                            mapping: Map[Int, Any] = CollectionItemAdapter.map)(implicit activity: DoubanActivity)
  extends ItemAdapter[Collection](R.layout.fav_books_item, mapping) {
  var currentPage = 0

  override def getView(position: Int, view: View, parent: ViewGroup): View = {
    super.getView(position, view, parent) match {
      case v: View =>
        val c: Collection = getBean(position)
        activity.loadImageWithTitle(c.book.image, R.id.book_img, c.book.title, v)
        activity.setViewValue(R.id.recommend, SearchResult.getStar(c.rating), v)
        activity.setViewValue(R.id.tags_txt, c.tags.mkString(" "), v, hideEmpty = false)
        v
      case _ => null
    }
  }

  override protected def selfLoad(): Unit = loader(status, this)

  lazy val listener = (parent: AdapterView[_], view: View, position: Int, id: Long) => {
    parent.getAdapter.asInstanceOf[CollectionItemAdapter].getBean(position) match {
      case c: Collection =>
        val book = c.book.copy()
        val col = c.copy()
        col.updateBook(null)
        book.updateExistCollection(col)
        activity.startActivity(SIntent[BookActivity].putExtra(Constant.BOOK_KEY, Some(book)))
    }
  }
}

object CollectionItemAdapter {
  val map = Map(R.id.time -> "updated", R.id.bookTitle -> "book.title", R.id.bookAuthor -> List("book.author",
    "book.translator"), R.id.bookPublisher -> "book.publisher")
}

class FavoriteBooksListActivity extends DoubanActivity {
  val REQUEST_CODE = 1

  protected override def onCreate(b: Bundle): Unit = {
    super.onCreate(b)
    setContentView(R.layout.fav_books_result)
    startActivityForResult(SIntent[FavoriteBooksFilterActivity], REQUEST_CODE)
  }

  def updateHeader(s: CollectionSearch) {
    if (s.status.nonEmpty)
      setViewValue(R.id.currentState, SearchResult.stateMapping.getOrElse(s.status, ""))
    if (s.rating > 0)
      setViewValue(R.id.ratedStars, s.rating + "星")
    if (s.from.nonEmpty || s.to.nonEmpty)
      setViewValue(R.id.date_duration, s.from.substring(0, Math.min(s.from.length, 10)) + "到" + s.to.substring(0, Math.min(s.to.length, 10)))
    if (s.tag.nonEmpty)
      setViewValue(R.id.tags_txt, s.tag)
  }

  def submitFilter(m: MenuItem) {
    startActivityForResult(SIntent[FavoriteBooksListActivity], REQUEST_CODE)
  }

  private var hide = true

  def toggleHeader(v: View) {
    toggleDisplayWhen(R.id.rating_container, hide)
    toggleDisplayWhen(R.id.tags_layout, hide)
    toggleDisplayWhen(R.id.duration_container, hide)
    hide = toggleBackGround(hide, R.id.filter_indicator, (R.drawable.filter_result_hide, R.drawable.filter_result_display))
  }

  override def onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
      data.getSerializableExtra(Constant.COLLECTION_SEARCH) match {
        case s: CollectionSearch =>
          updateHeader(s)
          cs = s
          currentPage = 1
          find[ListView](R.id.fav_books_result) onItemClick adapter.listener setAdapter adapter
          reload()
        case _ =>
      }
    } else finish()
  }

  override def onCreateOptionsMenu(menu: Menu) = {
    getMenuInflater.inflate(R.menu.filter, menu)
    super.onCreateOptionsMenu(menu)
  }

  var currentPage = 1
  var cs = CollectionSearch()
  lazy val adapter = new CollectionItemAdapter("", load)


  def reload() = load("", adapter)

  def load(status: String, adapter: CollectionItemAdapter) {
    listLoader(
      toLoad = 1 == currentPage || adapter.getCount < adapter.getTotal,
      result = {
        val search = CollectionSearch(cs.status, cs.tag, cs.rating, cs.from, cs.to, start = adapter.count, count = countPerPage)
        Book.collectionsOfUser(currentUserId, search)
      },
      success =
        (r: CollectionSearchResult) =>
          if (r.total == 0)
            toast("没有找到对应书籍")
          else runOnUiThread {
            if (1 == currentPage) {
              adapter.replaceResult(r.total, r.collections.size(), r.collections)
              adapter.notifyDataSetInvalidated()
            }
            else {
              adapter.addResult(r.total, r.collections.size(), r.collections)
              adapter.notifyDataSetChanged()
            }
            currentPage += 1
            setTitle(getString(R.string.favorite) + s"(${adapter.getCount}/${r.total})")
          }
    )
  }
}

class FavoriteBooksFilterActivity extends DoubanActivity {
  private var state = ""
  private var tags = collection.mutable.Set[String]()

  protected override def onCreate(b: Bundle): Unit = {
    super.onCreate(b)
    setContentView(R.layout.fav_books_filter)
    replaceActionBar(R.layout.header_edit, getString(R.string.filter_books))
    future {
      Book.tagsOf(currentUserId)
    } onSuccess {
      case t: TagsResult => runOnUiThread({
        val container = find[LinearLayout](R.id.tags_container)
        container.addView(new SVerticalLayout {
          t.tags.foreach(tag => SCheckBox(tag.title.toString).onClick(_ match {
            case db: CheckBox =>
              tags = if (db.isChecked) {
                tags + db.getText.toString
              }
              else tags - db.getText.toString
            case _ =>
          }))
        })
      })
      case _ =>
    }
  }

  def submit(v: View) {
    val sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ")
    val textFormat = new SimpleDateFormat("yy-MM-dd")

    val from = find[EditText](R.id.from_date).getText.toString match {
      case f: String if f.nonEmpty => sdf.format(textFormat.parse(f))
      case _ => ""
    }
    val to = find[EditText](R.id.to_date).getText.toString match {
      case f: String if f.nonEmpty => sdf.format(textFormat.parse(f))
      case _ => ""
    }
    val s = CollectionSearch(state, tags.mkString(" "), find[RatingBar](R.id.rating).getRating.toInt, from, to)
    setResult(Activity.RESULT_OK, getIntent.putExtra(Constant.COLLECTION_SEARCH, s))
    finish()
  }

  def showDatePickerDialog(v: View) {
    val newFragment: DialogFragment = new DatePickerFragment(v: View)
    newFragment.show(getSupportFragmentManager, "datePicker")
  }

  def checkSate(v: View) {
    state = SearchResult.str2ids.getOrElse(v.getId, "")
  }

  class DatePickerFragment(anchor: View) extends DialogFragment with DatePickerDialog.OnDateSetListener {
    override def onCreateDialog(savedInstanceState: Bundle): Dialog = {
      val c: Calendar = Calendar.getInstance
      val year: Int = c.get(Calendar.YEAR)
      val month: Int = c.get(Calendar.MONTH)
      val day: Int = c.get(Calendar.DAY_OF_MONTH)
      new DatePickerDialog(getActivity, this, year, month, day)
    }

    def onDateSet(view: widget.DatePicker, year: Int, month: Int, day: Int): Unit = {
      val textFormat = new SimpleDateFormat("yy-MM-dd")
      val dateString = textFormat.format(new GregorianCalendar(year, month, day).getTime)
      setViewValueByView(anchor, dateString)
    }
  }

}




