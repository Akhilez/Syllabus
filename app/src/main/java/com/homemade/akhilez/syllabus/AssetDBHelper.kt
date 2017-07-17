package com.homemade.akhilez.syllabus

import android.content.Context
import android.database.Cursor
import android.util.Log
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper
import java.util.*


class AssetDBHelper(context: Context) : SQLiteAssetHelper(context, AssetDBHelper.DATABASE_NAME, null, AssetDBHelper.DATABASE_VERSION) {

    var collegess: Array<String> = arrayOf()

    init {
        collegess = getColleges()
    }

    fun getNamesOf(subs: Array<String>): Array<String> {
        val db = readableDatabase
        var query: String// = "select distinct unit from conceptsTable where subject = '"+sub+"'";
        val column = "ful"
        var c: Cursor// = db.rawQuery(query,null);
        val list = ArrayList<String>()
        for (sub in subs) {
            Log.d("yooo", sub)
            query = "select ful from subjectNames where smal = '$sub'"
            c = db.rawQuery(query, null)
            c.moveToFirst()
            list.add(c.getString(c.getColumnIndex(column)))
            c.close()
        }
        val listArray = list.toTypedArray<String>()
        db.close()
        return listArray
    }

    fun getAllUnits(subjects: Array<String>): Array<Array<String>> {
        val subLen = subjects.size
        val allUnits = Array<Array<String>>(subLen) { arrayOf() }
        for (i in 0..subLen - 1) {
            allUnits[i] = getUnits(subjects[i])
        }
        return allUnits
    }

    fun getAllConcepts(subjects: Array<String>, allUnits: Array<Array<String>>): Array<Array<Array<String>>> {
        val subLen = subjects.size
        var unitLen: Int
        val allConcepts = Array(subLen) { Array<Array<String>>(10) { arrayOf() } }
        for (i in 0..subLen - 1) {
            unitLen = allUnits[i].size
            for (j in 0..unitLen - 1) {
                allConcepts[i][j] = getConcepts(subjects[i], allUnits[i][j])
            }
        }
        return allConcepts
    }

    fun getColleges(): Array<String> {
        val db = readableDatabase
        val c = db.rawQuery("select distinct college from sTable order by college", null)
        val collegeList = ArrayList<String>()
        collegeList.add("--select--")
        c.moveToFirst()
        while (!c.isAfterLast) {
            collegeList.add(c.getString(c.getColumnIndex("college")))
            c.moveToNext()
        }
        val colleges = collegeList.toTypedArray<String>()
        c.close()
        db.close()
        return colleges
        //return new String[]{"--select--","JNTUH","OUEC","JNTUK","CBIT"};
    }

    fun getCourses(college: String): Array<String> {
        val db = readableDatabase
        val query = "select distinct course from sTable where college = '$college' order by course"
        val column = "course"
        val c = db.rawQuery(query, null)
        val list = ArrayList<String>()
        list.add("--select--")
        c.moveToFirst()
        while (!c.isAfterLast) {
            val crse = c.getString(c.getColumnIndex(column))
            if (crse.contains("/")) {
                return arrayOf("#")
            }
            list.add(crse)
            c.moveToNext()
        }
        val courses = list.toTypedArray<String>()
        c.close()
        db.close()
        return courses
        //return new String[] {"--select--","CSE","IT","ECE","EIE","MEC","CIV"};
    }

    fun getYears(college: String, course: String): Array<String> {
        val db = readableDatabase
        val query = "select distinct year from sTable where college = '$college' and course = '$course' order by year"
        val column = "year"
        val c = db.rawQuery(query, null)
        val list = ArrayList<String>()
        list.add("--select--")
        c.moveToFirst()
        while (!c.isAfterLast) {
            list.add(c.getString(c.getColumnIndex(column)))
            c.moveToNext()
        }
        val years = list.toTypedArray<String>()
        c.close()
        db.close()
        return years
        //return new String[] {"--select--","1","2","3","4"};
    }

    fun getSemesters(college: String, course: String, year: String): Array<String> {
        val db = readableDatabase
        val query = "select distinct sem from sTable where college = '$college' and course = '$course' and year = '$year' order by sem"
        val column = "sem"
        val c = db.rawQuery(query, null)
        val list = ArrayList<String>()
        list.add("--select--")
        c.moveToFirst()
        while (!c.isAfterLast) {
            list.add(c.getString(c.getColumnIndex(column)))
            c.moveToNext()
        }
        val semesters = list.toTypedArray<String>()
        c.close()
        db.close()
        return semesters
        //return new String[] {"--select--","1","2"};
    }

    fun getSubjects(details: Array<String>): Array<String> {
        val db = readableDatabase
        val query = "select subject from sTable where college = '" + details[0] + "' and course = '" + details[1] + "' and year = '" + details[2] + "' and sem = '" + details[3] + "'"
        Log.d("loggu", query)
        val c = db.rawQuery(query, null)
        if (c.columnCount == 0)
            Log.d("FAILED for: ", query)
        val subjects: Array<String>
        c.moveToFirst()
        subjects = c.getString(c.getColumnIndex("subject")).split(",".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
        c.close()
        db.close()
        return subjects
    }

    fun getUnits(sub: String): Array<String> {
        val db = readableDatabase
        val query = "select distinct unit from conceptsTable where subject = '$sub'"
        val column = "unit"
        val c = db.rawQuery(query, null)
        val list = ArrayList<String>()
        c.moveToFirst()
        while (!c.isAfterLast) {
            list.add(c.getString(c.getColumnIndex(column)))
            c.moveToNext()
        }
        val units = list.toTypedArray<String>()
        c.close()
        db.close()
        return units
    }

    fun getConcepts(sub: String, unit: String): Array<String> {
        val db = readableDatabase
        val query = "select concepts from conceptsTable where subject = '$sub' and unit = '$unit'"
        val c = db.rawQuery(query, null)
        c.moveToFirst()
        val concepts = c.getString(0).split(",".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
        c.close()
        db.close()
        return concepts
    }

    companion object {
        internal val DATABASE_NAME = "syllabusDefault.db"
        private val DATABASE_VERSION = 1
    }

}