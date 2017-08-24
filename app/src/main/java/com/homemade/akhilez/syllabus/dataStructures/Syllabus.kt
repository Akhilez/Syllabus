package com.homemade.akhilez.syllabus.dataStructures

/**
 * Created by Akhil on 8/21/2017.
 *
 */

class Syllabus (idParam: String, univParam: String, branchParam: String, regulationParam: String? = null, yearParam: String, semParam: String, subjectsParam: Array<Subject>? = null) {
    val id = idParam
    val univ = univParam
    val branch = branchParam
    val regulation = regulationParam
    val year = yearParam
    val sem = semParam
}
