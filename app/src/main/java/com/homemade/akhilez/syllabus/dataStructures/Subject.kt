package com.homemade.akhilez.syllabus.dataStructures

/**
 * Created by Akhil on 8/9/2017.
 *
 */

class Subject(idParam: String, nameParam: String = "", unitsParam: Array<SyllabusUnit>? = null){
    val id = idParam
    var name = nameParam
    var units = unitsParam

}