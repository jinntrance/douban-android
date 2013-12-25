package com.douban.book

import com.douban.base.{Constant, DoubanFragment, DoubanActivity}
import android.view.{View, ViewGroup, LayoutInflater}
import android.os.Bundle
import android.widget.EditText

import org.scaloid.common._
import scala.concurrent._
import com.douban.models.Book
import scala.util.{Random, Success}
import com.douban.models.AnnotationPosted
import ExecutionContext.Implicits.global
import android.content.pm.PackageManager
import android.content.Intent
import android.provider.MediaStore
import android.app.Activity
import java.text.SimpleDateFormat
import java.io.File
import java.util.Date
import android.net.Uri

/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 9/30/13 11:51 AM
 * @version 1.0
 */
class AddNoteActivity extends DoubanActivity {
  var bookPage=""
  var chapter=""
  var noteConent=""
  var public=true
  override def onCreate(b: Bundle){
    super.onCreate(b)
    currentUserId
    setContentView(R.layout.add_note_container)
    val bundle: Bundle = getIntent.getExtras
    bookPage=bundle.getString(Constant.BOOK_PAGE,bookPage)
    chapter=bundle.getString(Constant.ANNOTATION_CHAPTER,chapter)
    fragmentManager.beginTransaction().replace(R.id.add_note_container, new AddNoteFragment().addArguments(bundle),Constant.ACTIVITY_NOTE_ADDITION).commit()

  }

  def submit(v:View){
    findViewById(R.id.bookPage) match{
      case bp:EditText=>
        bookPage=bp.getText.toString.trim
        chapter=find[EditText](R.id.chapter_name).getText.toString.trim
        if(bookPage.nonEmpty||chapter.nonEmpty) fragmentManager.popBackStack()
      case _=> future {
        val a=new AnnotationPosted(find[EditText](R.id.note_input).text.toString,bookPage.toInt,chapter,if(public) "public" else "private")
        a.files=Range(1,notesImage.size).map(_.toString).zip(notesImage).toMap
        toast("正在保存到豆瓣帐号...")
        getIntent.getLongExtra(Constant.BOOK_ID,0) match {
          case bookId:Long if bookId>0 =>
            Book.postAnnotation(bookId,a).isDefined
          case _=>
            val id=getIntent.getExtras.getString(Constant.ANNOTATION_ID,"0").toLong
            id>0&&Book.updateAnnotation(id,a).isDefined
        }
      }onComplete{
        case Success(true)=>
          toast(R.string.annotation_added)
          runOnUiThread(onBackPressed())
        case _=> toast(R.string.annotation_fails_to_add)
      }
    }
  }
  def editChapter(v:View){
    findViewById(R.id.note_input) match{
      case ed:EditText=>noteConent=ed.text.toString
      case _=>
    }
    fragmentManager.beginTransaction().replace(R.id.add_note_container,new AddChapterFragment()).addToBackStack(null).commit()
  }


  def checkPrivacy(v: View) {
    public = toggleBackGround(public, v, (R.drawable.private_icon, R.drawable.public_icon))
  }

  def addQuote(v:View){
    val text=find[EditText](R.id.note_input)
    val start: Int = text.getSelectionStart
    val end: Int = text.getSelectionEnd
    val (newString:String,newEnd:Int)=text.getText.toString.substring(start,end).trim match{
      case r"<原文开始>([\s\S]*)${selection}</原文结束>"=>(selection,end-13)
      case s:String=>(s"<原文开始>$s</原文结束>",end+13)
    }
    text.getText.replace(start,end,newString)
    text.setSelection(start,newEnd)
  }

  @inline def wrap(txt:String,wrapper:String)={
    s"<$wrapper>$txt</$wrapper>"
  }

  private def createImageFile(prefix:String="")={
    val timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date())
    val imageFileName = s"${prefix}_${timeStamp}_"
    val folder: File = new File(getExternalCacheDir.getAbsolutePath + "/notes")
    folder.mkdirs()
    File.createTempFile(imageFileName,".jpg", folder)
  }

  private val takingPhotos=10
  private val choosingPhotos=takingPhotos+1
  private var currentPic: Uri=null
  var notesImage=collection.mutable.ListBuffer[String]()
  def takePhotos(v:View){
    currentPic = Uri.fromFile(createImageFile())
    startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT,currentPic), takingPhotos)
  }

  def choosePhotos(v:View){
    val photoPickerIntent = new Intent(Intent.ACTION_PICK)
    photoPickerIntent.setType("image/*")
    startActivityForResult(photoPickerIntent, choosingPhotos)
  }

  def addPicture(path:String){
    notesImage+=path
    fragmentManager.findFragmentByTag(Constant.ACTIVITY_NOTE_ADDITION) match{
      case frg:AddNoteFragment=> frg.appendPicture(notesImage.size)
      case _=>
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
    super.onActivityResult(requestCode,resultCode,data)
     if(resultCode==Activity.RESULT_OK) requestCode match{
       case `takingPhotos` => addPicture(currentPic.getPath)
       case `choosingPhotos` => addPicture(contentUriToFilePath(data.getData))
       case _=>
     }
  }
}

class AddChapterFragment extends DoubanFragment[AddNoteActivity]{
  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, b: Bundle): View = inflater.inflate(R.layout.note_add_chapter,container,false)

  override def onActivityCreated(b: Bundle) {
    super.onActivityCreated(b)
    activity.replaceActionBar(R.layout.header_edit,"添加页码/章节名")
    setViewValue(R.id.bookPage,activity.bookPage,hideEmpty = false)
    setViewValue(R.id.chapter_name,activity.chapter,hideEmpty = false)
  }
}

class AddNoteFragment extends DoubanFragment[AddNoteActivity]{
  private var numOfPics=0
  def appendPicture(i: Int)={
    getView.find[EditText](R.id.note_input).append(s"<图片${i+numOfPics}>")
  }

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, b: Bundle): View = inflater.inflate(R.layout.note_add,container,false)

  override def onActivityCreated(bd: Bundle) {
    super.onActivityCreated(bd)
    getArguments match {
      case b: Bundle =>
        val page = b.getString(Constant.BOOK_PAGE, activity.bookPage)
        val chapter = b.getString(Constant.ANNOTATION_CHAPTER, activity.chapter)
        val content = b.getString(Constant.ANNOTATION_CONTENT, activity.noteConent)
        numOfPics = b.getString(Constant.ANNOTATION_IMAGES_NUMBER, "0").toInt
        activity.replaceActionBar(R.layout.header_edit_note, if (page.isEmpty) chapter else "P" + page)
        setViewValue(R.id.note_input, content, hideEmpty = false)
      case _ => activity.replaceActionBar(R.layout.header_edit_note,if(activity.bookPage.isEmpty) activity.chapter else "P"+activity.bookPage)
    }
    hideWhen(R.id.note_camera,isIntentUnavailable(MediaStore.ACTION_IMAGE_CAPTURE))
    hideWhen(R.id.note_album,isIntentUnavailable(Intent.ACTION_PICK))
    if(activity.bookPage.isEmpty && activity.chapter.isEmpty) getThisActivity.editChapter(null)
  }
  private def isIntentUnavailable(action:String) :Boolean= {
    val packageManager = activity.getPackageManager
    val intent = new Intent(action)
    packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).isEmpty
  }

}

