package com.douban.book

import java.text.SimpleDateFormat
import java.util.{Calendar, GregorianCalendar}

import android.app.{Activity, DatePickerDialog, Dialog}
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.{Menu, MenuItem, View}
import android.widget
import android.widget._
import com.douban.base.{Constant, DoubanActivity}
import com.douban.models.{Book, CollectionSearch, CollectionSearchResult, TagsResult}
import org.scaloid.common._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class FavoriteBooksFilterActivity extends DoubanActivity {
  private var state = ""
  private var tags = collection.mutable.Set[String]()

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

  protected override def onCreate(b: Bundle): Unit = {
    super.onCreate(b)
    setContentView(R.layout.fav_books_filter)
    replaceActionBar(R.layout.header_edit, getString(R.string.filter_books))
    Future {
      Book.tagsOf(currentUserId)
    } onSuccess {
      case t: TagsResult => runOnUiThread({
        val container = find[LinearLayout](R.id.tags_container)
        container.addView(new SVerticalLayout {
          t.tags.foreach(tag => SCheckBox(tag.title.toString).onClick { db: (CheckBox) => {
            tags = if (db.isChecked) {
              tags + db.getText.toString
            }
            else tags - db.getText.toString
          }
          })
        })
      })
      case _ =>
    }
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

class FavoriteBooksFilterResultActivity extends DoubanActivity {
  lazy val adapter = new CollectionItemAdapter("", load)
  val REQUEST_CODE = 1
  var currentPage = 1
  var cs = CollectionSearch()
  private var hide = true

  def submitFilter(m: MenuItem) {
    startActivityForResult(SIntent[FavoriteBooksFilterResultActivity], REQUEST_CODE)
  }

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

  def updateHeader(s: CollectionSearch) {
    if (s.status.nonEmpty)
      setViewValue(R.id.currentState, SearchResult.stateMapping.getOrElse(s.status, ""))
    if (s.rating > 0)
      setViewValue(R.id.ratedStars, s.rating + getString(R.string.stars))
    if (s.from.nonEmpty || s.to.nonEmpty)
      setViewValue(R.id.date_duration, s.from.substring(0, Math.min(s.from.length, 10)) + getString(R.string.to) + s.to.substring(0, Math.min(s.to.length, 10)))
    if (s.tag.nonEmpty)
      setViewValue(R.id.tags_txt, s.tag)
  }

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
            toast(R.string.books_not_found)
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

  override def onCreateOptionsMenu(menu: Menu) = {
    getMenuInflater.inflate(R.menu.filter, menu)
    super.onCreateOptionsMenu(menu)
  }

  protected override def onCreate(b: Bundle): Unit = {
    super.onCreate(b)
    setContentView(R.layout.fav_books_result)
    startActivityForResult(SIntent[FavoriteBooksFilterActivity], REQUEST_CODE)
  }
}
