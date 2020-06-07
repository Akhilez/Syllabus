package com.homemade.akhilez.syllabus.db

/**
 * Created by Akhil on 8/6/2017.
 *
 */

object Config{

    //private val ip = "http://" + "192.168.0.101" + "/syllabus/"
    private val ip = "https://akhilez.com/syllabus/requests/"

    val URL_GET_ALL_SYLLABUS = ip + "getAllSyllabus"
    val URL_GET_UNITS = ip + "getUnits"
    val URL_GET_SUBJECTS = ip + "getSubjects"
    val URL_IS_CONNECTED = ip + "isConnected"


    val KEY_UNIV = "univ"
    val KEY_BRANCH = "branch"
    val KEY_REGULATION = "regulation"
    val KEY_SEM = "sem"
    val KEY_AC_YEAR = "acYear"
    val KEY_SYLLABUS_ID = "syllabus_id"
    val KEY_ID = "id"
    val KEY_NAME = "name"
    val KEY_SUBJECT_ID = "subject_id"
    val KEY_UNIT = "unit"
    val KEY_CONCEPTS = "concepts"

}
