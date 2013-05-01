package com.douban.base

import android.database.sqlite.{SQLiteDatabase, SQLiteOpenHelper}
import android.content.ContentValues
import scala.reflect.ClassTag
import scala.reflect.classTag
import scala.collection.mutable
import com.douban.common.Req

/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 4/28/13 11:57 AM
 * @version 1.0
 * @see http://developers.douban.com/wiki/?title=api_v2
 */

class DBHelper[T:ClassTag](c:android.content.Context,tableName:String,fields:Map[String,String]=Map()) extends SQLiteOpenHelper(c, "douban_book.db", null, 5){
  private val  dataColumn = "_data"
  def fieldsDeclarations:Map[String,String]=Map("_id" -> "int primary key", dataColumn -> "text")++fields
  def onCreate(db: SQLiteDatabase) {
    if(0<fieldsDeclarations.size){
     val fields=fieldsDeclarations.map(e=>s"${e._1} ${e._2}").mkString(",")
     db.execSQL(s"create table $tableName ($fields);")
    }
  }

  def onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    db.execSQL(s"DROP TABLE IF EXISTS $tableName ;")
    onCreate(db)
  }
  def insert(t:T)={
    val c=new ContentValues()
    c.put(dataColumn,Req.g.toJson(t))
    getWritableDatabase.insert(tableName,null,c)
  }
  def find(id:Int)={
    val c=getReadableDatabase.rawQuery(s"select _data from $tableName where _id=$id",null)
    Req.g.fromJson[T](c.getString(1),classTag[T].runtimeClass)
  }
  def findData(size:Int=10,page:Int=1)={
    val c=getReadableDatabase.query(tableName,Array(dataColumn),null,null,null,null,s" _id desc",s" ${size*(page-1)},$size")
    val list=mutable.Buffer.newBuilder[T]
    do {
      list += (Req.g.fromJson[T](c.getString(1),classTag[T].runtimeClass))
    }while(c.moveToNext())
    list.result().toList
  }
  def remain(left:Int=20){
    getWritableDatabase.execSQL(s"delete from $tableName where _id < any (select _id from $tableName order by _id desc limit $left);")
  }
  def delete(id:Int)={
    0<getWritableDatabase.delete(tableName,s"_id=$id ",null)
  }
}
