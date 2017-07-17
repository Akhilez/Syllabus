package com.homemade.akhilez.syllabus

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.*

class OpenDBHelper(context: Context) : SQLiteOpenHelper(context, OpenDBHelper.DATABASE_NAME, null, OpenDBHelper.DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        //db.execSQL(SQL_CREATE_ENTRIES);
        db.execSQL("create table checkedBoxes(id integer, subject text, unit text, concepts text, checkFlags text)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        //db.execSQL(SQL_DELETE_ENTRIES);
        db.execSQL("drop table if exists checkedBoxes")
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }


    //================= MY METHODS ==================


    fun getAllCheckFlags(subjects: Array<String>, allUnits: Array<Array<String>>): Array<Array<Array<String>>> {
        val subLen = subjects.size
        var unitLen: Int
        val allCheckFlags = Array(subLen) { Array<Array<String>>(10) { arrayOf() } }

        for (i in 0..subLen - 1) {
            unitLen = allUnits[i].size
            for (j in 0..unitLen - 1) {
                allCheckFlags[i][j] = getCheckFlags(subjects[i], allUnits[i][j])
            }
        }
        return allCheckFlags
    }

    fun getCheckFlags(sub: String, unit: String): Array<String> {
        val db = readableDatabase
        val query = "select checkFlags from checkedBoxes where subject = '$sub' and unit = '$unit'"
        val column = "checkFlags"
        val c = db.rawQuery(query, null)
        val list = ArrayList<String>()
        c.moveToFirst()
        while (!c.isAfterLast) {
            list.add(c.getString(c.getColumnIndex(column)))
            c.moveToNext()
        }
        val listArray = list.toTypedArray<String>()
        //String[] listArray = c.getString(c.getColumnIndex(column)).split(",");
        c.close()
        db.close()
        return listArray
        //return new boolean[] {true,false,true,false,true};
    }

    //Log.d("sql message : ",Integer.toString(c.getInt(0))+c.getString(1));
    val noOfConcepts: Array<Int>
        get() {
            val db = readableDatabase
            val query = "select count(id) as c,subject from checkedBoxes group by subject order by id"
            val column = "c"
            val c = db.rawQuery(query, null)
            val list = ArrayList<Int>()
            c.moveToFirst()
            while (!c.isAfterLast) {
                list.add(c.getInt(c.getColumnIndex(column)))
                c.moveToNext()
            }
            val listArray = list.toTypedArray<Int>()
            c.close()
            db.close()
            return listArray
        }

    fun myUpgrade() {
        val db = writableDatabase
        var sql = "drop table checkedBoxes"
        db.execSQL(sql)
        sql = "create table checkedBoxes(id integer, subject text, unit text, concepts text, checkFlags text)"
        db.execSQL(sql)
        db.close()
    }

    fun insertNewConcept(i: Int, subject: String, unit: String, concept: String) {
        val db = writableDatabase
        val sql = "insert into checkedBoxes values ($i, '$subject','$unit','$concept','false')"
        db.execSQL(sql)
        db.close()
    }

    fun checkFlagAt(id: Int) {
        val db = writableDatabase
        val sql = "update checkedBoxes set checkFlags = 'true' where id = " + id
        db.execSQL(sql)
        db.close()
    }

    fun unCheckFlagAt(id: Int) {
        val db = writableDatabase
        val sql = "update checkedBoxes set checkFlags = 'false' where id = " + id
        db.execSQL(sql)
        db.close()
    }

    companion object {
        // If you change the database schema, you must increment the database version.
        val DATABASE_VERSION = 2
        val DATABASE_NAME = "syllabusLocal.db"
    }


}