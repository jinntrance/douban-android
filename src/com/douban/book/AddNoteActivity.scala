package com.douban.book

import com.douban.base.{Constant, DoubanFragment, DoubanActivity}
import android.view.{View, ViewGroup, LayoutInflater}
import android.os.Bundle
import android.widget.{TextView, EditText}
import com.google.gson.Gson

import org.scaloid.common._
import scala.concurrent._
import com.douban.models.{Annotation, Book, AnnotationPosted}
import scala.util.Success
import ExecutionContext.Implicits.global
import android.content.pm.PackageManager
import android.content.Intent
import android.provider.MediaStore
import android.app.{ProgressDialog, Activity}
import java.text.SimpleDateFormat
import java.io.File
import java.util.Date
import android.net.Uri
import android.text.{Editable, TextWatcher}

/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 9/30/13 11:51 AM
 * @version 1.0
 */
class AddNoteActivity extends DoubanActivity {
  var bookPage = ""
  var chapter = ""
  var noteConent = ""
  var public = true
  lazy val annt:Option[Annotation]= new Gson().fromJson(getIntent.getStringExtra(Constant.ANNOTATION),Annotation.getClass) match {
    case a:Annotation=>Some(a)
    case _=> None
  }

  override def onCreate(b: Bundle) {
    super.onCreate(b)
    currentUserId
    setContentView(R.layout.add_note_container)
    val bundle: Bundle = getIntent.getExtras
    bookPage = bundle.getString(Constant.BOOK_PAGE, bookPage)
    annt foreach  {a=>{
        bookPage=a.page_no.toString
        chapter=a.chapter
        noteConent=a.content
    }
    }
    b match{
      case savedInstance:Bundle=>
      case _=>
        fragmentManager.beginTransaction().replace(R.id.add_note_container,
          new AddNoteFragment().addArguments(bundle),
          Constant.ACTIVITY_NOTE_ADDITION).commit()
    }

  }

  def submit(v: View) {
    findViewById(R.id.bookPage) match {
      case bp: EditText =>
        bookPage = bp.getText.toString.trim
        chapter = find[EditText](R.id.chapter_name).getText.toString.trim
        if (bookPage.nonEmpty || chapter.nonEmpty) fragmentManager.popBackStack()
        else toast(R.string.add_chapter_hint)
      case _ =>
        val content = find[EditText](R.id.note_input).text.toString
        var proc:ProgressDialog=null
        if (content.length <= 15) toast(R.string.note_length)
        else Future {
          val page:Int = bookPage match {
            case p:String if p.nonEmpty && p.forall(_.isDigit)=>p.toInt
            case _ => 1
          }
          val a = new AnnotationPosted(content, page, chapter, if (public) "public" else "private")
          a.files = Range(1, notesImage.size+1).map(_.toString).zip(notesImage).toMap
          proc=waitToLoad(msg=R.string.saving)
          getIntent.getLongExtra(Constant.BOOK_ID, 0) match {
            case bookId: Long if bookId > 0 =>
              Book.postAnnotation(bookId, a).isDefined
            case _ =>
              annt.foreach(at=>Book.updateAnnotation(at.id, a))
          }
        } onComplete {
          case Success(true) =>
            runOnUiThread(onBackPressed())
            toast(R.string.annotation_added)
          case _ =>
            stopWaiting(proc)
            toast(R.string.annotation_fails_to_add)
        }
    }
  }

  def editChapter(v: View) {
    findViewById(R.id.note_input) match {
      case ed: EditText => noteConent = ed.text.toString
      case _ =>
    }
    fragmentManager.beginTransaction().replace(R.id.add_note_container, new AddChapterFragment()).addToBackStack(null).commit()
  }


  def checkPrivacy(v: View) {
    public = toggleBackGround(public, v, (R.drawable.private_icon, R.drawable.public_icon))
  }

  def addQuote(v: View) {
    val text = find[EditText](R.id.note_input)
    val start: Int = text.getSelectionStart
    val end: Int = text.getSelectionEnd
    val (newString: String, newEnd: Int) = text.getText.toString.substring(start, end).trim match {
      case r"<原文开始>([\s\S]*)${selection}</原文结束>" => (selection, end - 13)
      case s: String => (s"<原文开始>$s</原文结束>", end + 13)
    }
    text.getText.replace(start, end, newString)
    text.setSelection(start, newEnd)
  }

  @inline def wrap(txt: String, wrapper: String) = {
    s"<$wrapper>$txt</$wrapper>"
  }

  private def createImageFile(prefix: String = "") = {
    val timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date())
    val imageFileName = s"${prefix}_${timeStamp}_"
    val folder: File = new File(getExternalCacheDir.getAbsolutePath + "/notes")
    folder.mkdirs()
    File.createTempFile(imageFileName, ".jpg", folder)
  }

  private val takingPhotos = 10
  private val choosingPhotos = takingPhotos + 1
  private var currentPic: Uri = null
  var notesImage = collection.mutable.ListBuffer[String]()

  def takePhotos(v: View) {
    currentPic = Uri.fromFile(createImageFile())
    startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT, currentPic), takingPhotos)
  }

  def choosePhotos(v: View) {
    val photoPickerIntent = new Intent(Intent.ACTION_PICK)
    photoPickerIntent.setType("image/*")
    startActivityForResult(photoPickerIntent, choosingPhotos)
  }

  def addPicture(path: String) {
    notesImage += path
    fragmentManager.findFragmentByTag(Constant.ACTIVITY_NOTE_ADDITION) match {
      case frg: AddNoteFragment => frg.appendPicture(notesImage.size)
      case _ =>
    }
  }


  def contentUriToFilePath(uri: Uri): String = {
    val cursor = getContentResolver.query(uri, Array(MediaStore.MediaColumns.DATA), null, null, null)
    val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
    cursor.moveToFirst()
    val t = cursor.getString(columnIndex)
    t
  }

  override def onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
    super.onActivityResult(requestCode, resultCode, data)
    if (resultCode == Activity.RESULT_OK) requestCode match {
      case `takingPhotos` => addPicture(currentPic.getPath)
      case `choosingPhotos` => addPicture(contentUriToFilePath(data.getData))
      case _ =>
    }
  }
}

class AddChapterFragment extends DoubanFragment[AddNoteActivity] {
  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, b: Bundle): View = inflater.inflate(R.layout.note_add_chapter, container, false)

  override def onActivityCreated(b: Bundle) {
    super.onActivityCreated(b)
    activity.replaceActionBar(R.layout.header_edit, getString(R.string.add_chapter))
    setViewValue(R.id.bookPage, activity.bookPage, hideEmpty = false)
    setViewValue(R.id.chapter_name, activity.chapter, hideEmpty = false)
  }
}

class AddNoteFragment extends DoubanFragment[AddNoteActivity] {
  private var numOfPics = 0
  private var resuming = false

  def appendPicture(i: Int) = {
    getView.find[EditText](R.id.note_input).append(s"<图片${i + numOfPics}>")
  }

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, b: Bundle): View = inflater.inflate(R.layout.note_add, container, false)

  override def onActivityCreated(bd: Bundle) {
    super.onActivityCreated(bd)
    lazy val counter = getView.find[TextView](R.id.chars_count)
    getArguments match {
      case b: Bundle =>
        activity.annt.foreach(a=> numOfPics=a.last_photo)
        activity.replaceActionBar(R.layout.header_edit_note, if (activity.bookPage.isEmpty) activity.chapter else "P" + activity.bookPage)
        setViewValue(R.id.note_input, activity.noteConent, hideEmpty = false)
        activity.noteConent match {
          case s: String if s.nonEmpty => counter.setText(s.length.toString)
          case _ =>
        }

      case _ => activity.replaceActionBar(R.layout.header_edit_note, if (activity.bookPage.isEmpty) activity.chapter else "P" + activity.bookPage)
    }
    hideWhen(R.id.note_camera, isIntentUnavailable(MediaStore.ACTION_IMAGE_CAPTURE))
    hideWhen(R.id.note_album, isIntentUnavailable(Intent.ACTION_PICK))
    getView.find[EditText](R.id.note_input).addTextChangedListener(new TextWatcher() {
      def beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int): Unit = {}

      def afterTextChanged(s: Editable): Unit = {
        counter.setText(s.length.toString)
      }

      def onTextChanged(s: CharSequence, start: Int, before: Int, count: Int): Unit = {}
    })
    if (activity.bookPage.isEmpty && activity.chapter.isEmpty)
      if (!resuming) getThisActivity.editChapter(null)
      else activity.finish()
    resuming = true
  }
}

