package com.homemade.akhilez.syllabus.db

import android.content.Context
import android.net.ConnectivityManager
import com.homemade.akhilez.syllabus.dataStructures.Subject
import com.homemade.akhilez.syllabus.dataStructures.Syllabus
import com.homemade.akhilez.syllabus.dataStructures.SyllabusUnit


/**
 * Created by Akhil on 8/6/2017.
 *
 */

class DBHelper : RequestHandler(){

    fun isConnected(context: Context): Boolean {
        if (!isNetworkAvailable(context)) return false
        return sendGetRequest(Config.URL_IS_CONNECTED).trim().toBoolean()
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null
    }

    fun getAllSyllabus(): Array<Syllabus>{
        val jsonMapList = jsonStringToMapList(sendGetRequest(Config.URL_GET_ALL_SYLLABUS), Config.KEY_ID, Config.KEY_UNIV, Config.KEY_BRANCH, Config.KEY_REGULATION, Config.KEY_AC_YEAR, Config.KEY_SEM)
        val syllabusList = ArrayList<Syllabus>()

        for (i in 0 until jsonMapList.size) {
            val jsonMap = jsonMapList[i]
            val syllabus_id = jsonMap[Config.KEY_ID]
            val univ = jsonMap[Config.KEY_UNIV]
            val branch = jsonMap[Config.KEY_BRANCH]
            val regulation = jsonMap[Config.KEY_REGULATION]
            val year = jsonMap[Config.KEY_AC_YEAR]
            val sem = jsonMap[Config.KEY_SEM]
            val syllabus = Syllabus(syllabus_id!!, univ!!, branch!!, regulation, year!!, sem!!)
            syllabusList.add(syllabus)
        }
        return syllabusList.toTypedArray()
    }

    fun getSubjects(syllabus_id: String): Array<Subject> {
        //1. Get json string from post request
        val map = HashMap<String, String>()
        map.put(Config.KEY_SYLLABUS_ID, syllabus_id)
        val jsonString = sendPostRequest(Config.URL_GET_SUBJECTS, map)

        //2. Parse json string into list<hash maps>
        val jsonMapList = jsonStringToMapList(jsonString, Config.KEY_ID, Config.KEY_NAME)
        val subjectsList = ArrayList<Subject>()

        for (i in 0 until jsonMapList.size) {
            val jsonMap = jsonMapList[i]
            val id = jsonMap[Config.KEY_ID]
            val name = jsonMap[Config.KEY_NAME]
            val units = getUnits(id!!)
            val subject = Subject(id, name!!, units)
            subjectsList.add(subject)
        }

        return subjectsList.toTypedArray()
    }

    private fun getUnits(subjectId: String): Array<SyllabusUnit> {
        val map = HashMap<String, String>()
        map.put(Config.KEY_SUBJECT_ID, subjectId)
        val jsonString = sendPostRequest(Config.URL_GET_UNITS, map)
        val jsonMapList = jsonStringToMapList(jsonString, Config.KEY_UNIT, Config.KEY_CONCEPTS)
        val unitsList = ArrayList<SyllabusUnit>()

        for (i in 0 until jsonMapList.size) {
            val jsonMap = jsonMapList[i]
            val unit = jsonMap[Config.KEY_UNIT]
            val concepts = jsonMap[Config.KEY_CONCEPTS]!!
            val syllabusUnit = SyllabusUnit(unit!!, concepts)
            unitsList.add(syllabusUnit)
        }

        return unitsList.toTypedArray()

    }

}