package com.homemade.akhilez.syllabus.db

import android.content.Context
import android.content.SharedPreferences
import android.database.CursorIndexOutOfBoundsException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.homemade.akhilez.syllabus.dataStructures.Subject
import com.homemade.akhilez.syllabus.dataStructures.Syllabus
import com.homemade.akhilez.syllabus.dataStructures.SyllabusUnit
import java.util.*

class OpenDBHelper(contextParam: Context) : SQLiteOpenHelper(contextParam, DATABASE_NAME, null, DATABASE_VERSION) {

    private var context = contextParam

    companion object {
        // If you change the database schema, you must increment the database version.
        val DATABASE_VERSION = 3
        val DATABASE_NAME = "syllabus_local.db"
    }

    override fun onCreate(db: SQLiteDatabase) {
        //db.execSQL(SQL_CREATE_ENTRIES);
        db.execSQL("create table subjects(id int primary key, name varchar(64) not null unique)")
        db.execSQL("create table units( subject_id int, unit varchar(64), concepts longtext null, checks text null, primary key (subject_id, unit))")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        //db.execSQL(SQL_DELETE_ENTRIES);
        db.execSQL("drop table if exists subjects")
        db.execSQL("drop table if exists units")
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    //================= MY METHODS ==================

    fun syncDb() {
        val dbHelper = DBHelper()
        if (!dbHelper.isConnected(context)) return
        val syllabusId = getDetails()["syllabusId"]

        if(syllabusId != null) syncSubjects(dbHelper, syllabusId)
    }

    private fun syncSubjects(dbHelper: DBHelper, syllabusId: String) {
        /*
        1. create table subjects_temp
        2. copy data from mysql subjects to subjects_temp
        3. drop table subjects
        4. rename subjects_temp to subjects
         */

        //1. create table subjects_temp
        val db = writableDatabase
        db.execSQL("drop table if exists subjects_temp")
        db.execSQL("create table subjects_temp(id int primary key, name varchar(64) not null unique)")
        db.execSQL("drop table if exists units_temp")
        db.execSQL("create table units_temp( subject_id int, unit varchar(64), concepts longtext null, checks text null, primary key (subject_id, unit))")

        //2. copy data from mysql subjects to subjects_temp
        val subjects = dbHelper.getSubjects(syllabusId)
        for (subject in subjects) {
            db.execSQL("insert into subjects_temp values( ${subject.id}, '${subject.name}' )")
            if (subject.units != null)
                enterUnits(subject, db)
        }

        //3. drop table subjects
        db.execSQL("drop table subjects")
        db.execSQL("drop table units")

        //4. rename subjects_temp to subjects
        db.execSQL("alter table subjects_temp rename to subjects")
        db.execSQL("alter table units_temp rename to units")
        db.close()
    }

    private fun enterUnits(subject: Subject, db: SQLiteDatabase) {
        for (unit in subject.units!!) {
            val sql = "insert into units_temp values ( '${subject.id}', '${unit.unit}', '${unit.concepts}', '${getChecks(subject.id, unit.unit, db)}' )"
            db.execSQL(sql)
        }
    }

    private fun getChecks(subjectId: String, unitId: String, db: SQLiteDatabase): String {
        val query = "select checks from units where subject_id=$subjectId and unit='$unitId'"
        val result = db.rawQuery(query, null)

        result.moveToFirst()
        val concepts = try {
            result.getString(result.getColumnIndex("checks"))
        } catch (e: CursorIndexOutOfBoundsException) {
            ""
        }
        result.close()
        return concepts
    }

    fun updateSyllabus(syllabus: Syllabus) {
        /*
        0. check if the given details match with the current syllabus
        1. get syllabus_id
        2. get subjects from syllabus_id
        3. add subjects to local DB
        4. get units from syllabus_id
        5. add units to local DB
        6. insert the syllabus_id and other details into sharedPref
         */

        //0. check if the given details match with the current syllabus
        //if (syllabusMatch(syllabus)) return

        //remove anchor
        context.getSharedPreferences("common", Context.MODE_PRIVATE).edit().putInt("lastTab", 0).apply()

        //2. get subjects from syllabus_id
        val subjects = DBHelper().getSubjects(syllabus.id)

        //3. add subjects to local DB
        addSubjects(subjects)

        //6. insert the syllabus_id and other details into sharedPref
        insertDetails(syllabus)
    }

    private fun syllabusMatch(syllabus: Syllabus): Boolean {
        val details = getDetails()
        if (details["univ"] == syllabus.univ
                && details["branch"] == syllabus.branch
                && details["regulation"] == syllabus.regulation
                && details["year"] == syllabus.year
                && details["sem"] == syllabus.sem)
            return true
        return false
    }

    private fun insertDetails(syllabus: Syllabus) {
        val sharedPref: SharedPreferences = context.getSharedPreferences("common", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString("syllabusId", syllabus.id)
        editor.putString("univ", syllabus.univ)
        editor.putString("branch", syllabus.branch)
        editor.putString("regulation", syllabus.regulation)
        editor.putString("year", syllabus.year)
        editor.putString("sem", syllabus.sem)
        editor.apply()
    }

    private fun addSubjects(subjects: Array<Subject>) {
        val db = writableDatabase
        db.execSQL("delete from subjects")
        db.execSQL("delete from units")
        for (subject in subjects) {
            db.execSQL("insert into subjects values( ${subject.id}, '${subject.name}' )")
            if (subject.units != null)
                for (unit in subject.units!!)
                    db.execSQL("insert into units values ( '${subject.id}', '${unit.unit}', '${unit.concepts}', '${getBlankChecks(unit.concepts.split(",").size)}' )")
        }
        db.close()
    }

    private fun getBlankChecks(len: Int): String {
        if (len == 0) return ""
        val string = StringBuilder()
        for (i in 0..len - 2)
            string.append("0,")
        string.append("0")
        return string.toString()
    }

    fun getSubjects(): Array<Subject> {
        val db = readableDatabase
        val query = "select id, name from subjects"
        val result = db.rawQuery(query, null)
        val list = ArrayList<Subject>()
        result.moveToFirst()
        while (!result.isAfterLast) {
            val subject = getSubjectHierarchy(result.getInt(result.getColumnIndex("id")).toString())
            subject.name = result.getString(result.getColumnIndex("name"))
            list.add(subject)
            result.moveToNext()
        }
        val resultArray = list.toTypedArray()
        result.close()
        db.close()
        return resultArray
    }

    fun getSubject(position: Int): Subject? {
        val query = "SELECT id, name FROM subjects ORDER BY ID LIMIT $position,1"
        val db = readableDatabase
        val result = db.rawQuery(query, null)
        var subject: Subject? = null
        result.moveToFirst()
        if (!result.isAfterLast) {
            subject = getSubjectHierarchy(result.getInt(result.getColumnIndex("id")).toString())
            subject.name = result.getString(result.getColumnIndex("name"))
        }
        result.close()
        db.close()
        return subject
    }

    private fun getSubjectHierarchy(subject_id: String): Subject {
        val db = readableDatabase
        val query = "select unit, concepts, checks from units where subject_id=$subject_id"
        val subject = Subject(subject_id)
        val result = db.rawQuery(query, null)
        val unitsMap = ArrayList<SyllabusUnit>()
        result.moveToFirst()
        while (!result.isAfterLast) {
            val unit_id = result.getString(result.getColumnIndex("unit"))
            val concepts = result.getString(result.getColumnIndex("concepts"))
            val checks = result.getString(result.getColumnIndex("checks"))
            unitsMap.add(SyllabusUnit(unit_id, concepts, checks))
            result.moveToNext()
        }
        subject.units = unitsMap.toTypedArray()
        result.close()
        db.close()
        return subject
    }

    fun markConcept(subject_id: Int, unitId: String, conceptIndex: Int, markValue: Int) {
        /*
        1. get the concept marks array
        2. update the concept mark value in the array
        3. convert array to string
        4. update the concepts
         */

        val conceptMarks = getConceptMarks(subject_id, unitId)

        conceptMarks[conceptIndex] = markValue.toString()

        updateConceptMarks(subject_id, unitId, arrayToString(conceptMarks))

    }

    private fun updateConceptMarks(subject_id: Int, unitId: String, concepts: String) {
        val db = writableDatabase
        db.execSQL("update units set checks='$concepts' where subject_id=$subject_id and unit='$unitId'")
        db.close()
    }

    private fun arrayToString(array: Array<String>): String {
        val string = StringBuilder()
        for (i in 0..array.size - 2)
            string.append("${array[i]},")
        string.append(array[array.size - 1])
        return string.toString()
    }

    private fun getConceptMarks(subject_id: Int, unitId: String): Array<String> {
        val db = readableDatabase
        val query = "select checks from units where subject_id=$subject_id and unit='$unitId'"
        val result = db.rawQuery(query, null)

        result.moveToFirst()
        val concepts = result.getString(result.getColumnIndex("checks"))
        result.close()
        db.close()
        return concepts.split(",").toTypedArray()
    }

    private fun getDetails(): HashMap<String, String> {
        val map = HashMap<String, String>()
        val sharedPref = context.getSharedPreferences("common", Context.MODE_PRIVATE)

        map.put("syllabusId", sharedPref!!.getString("syllabusId", "null"))
        map.put("univ", sharedPref.getString("univ", "null"))
        map.put("branch", sharedPref.getString("branch", "null"))
        map.put("regulation", sharedPref.getString("regulation", "null"))
        map.put("year", sharedPref.getString("year", "null"))
        map.put("sem", sharedPref.getString("sem", "null"))

        return map
    }

}