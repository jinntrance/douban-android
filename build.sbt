import sbt._

import Keys._

import AndroidKeys._

organization := "com.douban"

name := "douban-book"

version := "1.0"

scalaVersion := "2.10.1"

resolvers += "oss repo" at "https://oss.sonatype.org/content/repositories/releases/"

javacOptions ++= Seq("-source", "1.6", "-target", "1.6")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-Xcheckinit","-optimise")

//autoScalaLibrary := false

//unmanagedBase <<= baseDirectory { base => base / "libs" }

libraryDependencies ++= Seq(
			"org.scaloid" % "scaloid" % "1.1_8_2.10",
			"com.douban" %% "scala-api" % "2.1" ,
			"com.google.zxing" % "core" % "2.1",
			"com.google.android" % "support-v4" % "r7")

proguardOption in Android :="""
-verbose
-dontobfuscate
-dontoptimize
-printseeds target/keep.log
-printmapping target/obf.log
-keepparameternames
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod
-keepclassmembers class * { ** MODULE$; }
-keepdirectories
-keep class scala.runtime.ObjectRef
-keep class scala.runtime.VolatileObjectRef
-keep class scala.reflect.Manifest
-keep class scala.reflect.ClassTag
-keep class scala.reflect.ClassManifestDeprecatedApis*
-keep class scala.collection.mutable.ArrayBuffer
-keep class scala.math.Ordering
-keep class org.scaloid.common.SActivity
-keep class org.scaloid.common.SContext
-keep class org.scaloid.common.Registerable
-keep class org.scaloid.common.LoggerTag
-keep class android.support.v4.app.Fragment
-keep public class scala.Option
-keep public class scala.PartialFunction
-keep public class scala.Function0
-keep public class scala.Function1
-keep public class scala.Function2
-keep public class scala.Product
-keep public class scala.Tuple2
-keep public class scala.collection.Seq
-keep public class scala.collection.GenSeq
-keep public class scala.collection.immutable.List
-keep public class scala.collection.immutable.Map
-keep public class scala.collection.SeqLike {public protected *;}
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.appwidget.AppWidgetProvider
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.preference.Preference
-keep public class * extends android.view.View
-keepclasseswithmembernames class * {native <methods>;}
-keepclasseswithmembers class * {public <init>(android.content.Context, android.util.AttributeSet);}
-keepclasseswithmembers class * {public <init>(android.content.Context, android.util.AttributeSet, int);}
-keepclassmembers class * extends android.app.Activity {   public void *(android.view.View);}
-keepclassmembers enum * {public static **[] values();public static ** valueOf(java.lang.String);}
-keep class * implements android.os.Parcelable {  public static final android.os.Parcelable$Creator *;}
"""

proguardOptimizations in Android ++= Seq()
