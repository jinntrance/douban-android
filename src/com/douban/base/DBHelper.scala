package com.douban.base

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, ObjectInputStream, ObjectOutputStream}

import android.content.ContentValues
import android.database.sqlite.{SQLiteDatabase, SQLiteOpenHelper}

import scala.collection.mutable
import scala.reflect.ClassTag

/**
 * Copyright by <a href="http://www.josephjctang.com"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 4/28/13 11:57 AM
 * @version 1.0
 * @see http://developers.douban.com/wiki/?title=api_v2
 */
class DBHelper[T: ClassTag](c: android.content.Context, tableName: String, fields: Map[String, String] = Map()) extends SQLiteOpenHelper(c, "douban_book.db", null, 5) {
  @inline private val dataColumn = "_data"
  @inline private val idColumn = "_id"

  def fieldsDeclarations = Map(idColumn -> "int primary key", dataColumn -> "BLOB") ++ fields

  def onCreate(db: SQLiteDatabase) {
    if (0 < fieldsDeclarations.size) {
      val fields = fieldsDeclarations.map(e => s"${e._1} ${e._2}").mkString(",")
      db.execSQL(s"create table $tableName ($fields);")
    }
  }

  def onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    db.execSQL(s"DROP TABLE IF EXISTS $tableName ;")
    onCreate(db)
  }

  def insert(t: T) = {
    val c = new ContentValues()
    c.put(dataColumn, Serializer.serialize(t))
    getWritableDatabase.insert(tableName, null, c)
  }

  def insertAll(l: List[T]) = l.foreach(insert)

  def find(id: Int): T = {
    val c = getReadableDatabase.rawQuery(s"select $dataColumn from $tableName where $idColumn=$id", null)
    Serializer.deserialize(c.getBlob(1))
  }

  def findData(size: Int = 10, page: Int = 1) = {
    val c = getReadableDatabase.query(tableName, Array(dataColumn), null, null, null, null, s" $idColumn desc", s" ${size * (page - 1)},$size")
    if (0 == c.getCount) {
      Nil
    } else {
      val list = mutable.Buffer.newBuilder[T]
      c.moveToFirst()
      do {
        list += Serializer.deserialize(c.getBlob(0)) //get the first column's data
      } while (c.moveToNext())
      list.result().toList
    }
  }

  def deleteAll() = {
    getWritableDatabase.delete(tableName, null, null)
  }

  def remain(left: Int = 20) {
    getWritableDatabase.execSQL(s"delete from $tableName where $idColumn< any (select $idColumn from $tableName order by $idColumn desc limit $left);")
  }

  def delete(id: Int) = {
    0 < getWritableDatabase.delete(tableName, s"_id=$id ", null)
  }
}

object Serializer {
  def serialize[T <: Any](obj: T) = {
    val b = new ByteArrayOutputStream()
    val o = new ObjectOutputStream(b)
    o.writeObject(obj)
    b.toByteArray
  }

  def deserialize[T](bytes: Array[Byte]) = {
    val b = new ByteArrayInputStream(bytes)
    val o = new ObjectInputStream(b)
    o.readObject().asInstanceOf[T]
  }
}
