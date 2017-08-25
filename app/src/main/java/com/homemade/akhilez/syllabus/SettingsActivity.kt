package com.homemade.akhilez.syllabus

import android.app.ProgressDialog
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import com.homemade.akhilez.syllabus.dataStructures.Syllabus
import com.homemade.akhilez.syllabus.db.DBHelper
import com.homemade.akhilez.syllabus.db.OpenDBHelper
import kotlinx.android.synthetic.main.activity_settings.*
import java.util.TreeSet
import kotlin.collections.ArrayList


class SettingsActivity : AppCompatActivity() {

    var selectedSyllabus: Syllabus? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        setUpSpinners()

        addAd()

    }

    private fun addAd() {
        /*
        MobileAds.initialize(applicationContext, resources.getString(R.string.dummyBanner))
        val mAdView = adView
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
        */
    }

    private fun setUpSpinners() {
        class SetUpSpinners : AsyncTask<Void, Void, Array<Syllabus>>(){
            var loading: ProgressDialog? = null
            override fun onPreExecute() {
                super.onPreExecute()
                loading = ProgressDialog.show(this@SettingsActivity, "Please wait...", "Fetching syllabus...", false, false)
            }

            override fun doInBackground(vararg p0: Void?): Array<Syllabus>?{
                val dbHelper = DBHelper()
                return if(dbHelper.isConnected(this@SettingsActivity))
                    dbHelper.getAllSyllabus()
                else null
            }

            override fun onPostExecute(result: Array<Syllabus>?) {
                super.onPostExecute(result)
                loading?.dismiss()
                if (result == null) {
                    Toast.makeText(this@SettingsActivity, "Please check your internet connection and try again", Toast.LENGTH_LONG).show()
                    return
                }
                addToSpinner(result.univArray(), collegeSpinner)
                collegeSpinner.performClick()
                collegeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

                    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                        val selectedUniv = parent.getItemAtPosition(position) as String
                        if (selectedUniv != "--select--"){
                            unhide(courseSpinner, branchLabel)
                            setCourseSpinner(result, selectedUniv)
                        } else hide(courseSpinner, regulationSpinner, yearSpinner, semesterSpinner,
                                branchLabel, regulationLabel, yearLabel, semLabel)
                    }
                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }
            }
        }
        SetUpSpinners().execute()
    }

    fun setCourseSpinner(allSyllabus: Array<Syllabus>, selectedUniv: String) {
        val result = allSyllabus.filterUniv(selectedUniv)//branches of selectedCollege
        addToSpinner(result.branchArray(), courseSpinner)
        courseSpinner.performClick()
        courseSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedBranch = parent.getItemAtPosition(position) as String
                if (selectedBranch != "--select--"){
                    unhide(regulationSpinner, regulationLabel)
                    setRegulationSpinner(result, selectedBranch)
                }else hide(regulationSpinner, yearSpinner, semesterSpinner, regulationLabel, yearLabel, semLabel)
            }
            override fun onNothingSelected(parent: AdapterView<*>?){}
        }
    }

    fun setRegulationSpinner(allSyllabus: Array<Syllabus>, selectedBranch: String) {
        val result = allSyllabus.filterBranches(selectedBranch)
        addToSpinner(result.regulationArray(), regulationSpinner)
        regulationSpinner.performClick()
        regulationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedRegulation = parent.getItemAtPosition(position) as String
                if (selectedRegulation != "--select--"){
                    unhide(yearSpinner, yearLabel)
                    setYearSpinner(result, selectedRegulation)
                }else hide(yearSpinner, semesterSpinner, yearLabel, semLabel)
            }
            override fun onNothingSelected(parent: AdapterView<*>?){}
        }
    }

    fun setYearSpinner(allSyllabus: Array<Syllabus>, selectedRegulation: String) {
        val result = allSyllabus.filterRegulations(selectedRegulation)
        addToSpinner(result.yearArray(), yearSpinner)
        yearSpinner.performClick()
        yearSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedYear = parent.getItemAtPosition(position) as String
                if (selectedYear != "--select--"){
                    unhide(semesterSpinner, semLabel)
                    setSemesterSpinner(result, selectedYear)
                }else hide(semesterSpinner, semLabel)
            }
            override fun onNothingSelected(parent: AdapterView<*>?){}
        }
    }

    fun setSemesterSpinner(allSyllabus: Array<Syllabus>, selectedYear: String) {
        val result = allSyllabus.filterYears(selectedYear)
        addToSpinner(result.semArray(), semesterSpinner)
        semesterSpinner.performClick()
        semesterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedSem = parent.getItemAtPosition(position) as String
                if (selectedSem != "--select--") {
                    selectedSyllabus = result.filterSems(selectedSem)[0]
                    unhide(changeButton)
                }
                else hide()
            }
            override fun onNothingSelected(parent: AdapterView<*>?){}
        }
    }

    fun changeButtonClick(view: View){
        class ChangeButton : AsyncTask<Void, Void, Void>(){
            var loading: ProgressDialog? = null
            override fun onPreExecute() {
                super.onPreExecute()
                loading = ProgressDialog.show(this@SettingsActivity, "Please wait...", "Updating syllabus...", false, false)
            }
            override fun doInBackground(vararg p0: Void?): Void? {
                if (selectedSyllabus == null)
                    Toast.makeText(this@SettingsActivity, "Syllabus not selected", Toast.LENGTH_LONG).show()
                else
                    OpenDBHelper(this@SettingsActivity).updateSyllabus(selectedSyllabus!!)
                return null
            }
            override fun onPostExecute(result: Void?) {
                super.onPostExecute(result)
                loading?.dismiss()
                val intent = Intent(applicationContext, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finish()
            }
        }
        ChangeButton().execute()
    }

    private fun addToSpinner(list: Array<String>?, spinner: Spinner){
        val adapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_item, list)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.setSelection(0)
    }

    private fun hide(vararg views: View){
        for (view in views)
            view.visibility = View.GONE
        changeButton.visibility = View.GONE
    }

    private fun unhide(vararg views: View){
        for(view in views){
            view.visibility = View.VISIBLE
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    fun refreshButton(view: View){
        refreshButtonAnimation(view)
        setUpSpinners()
    }

    private fun refreshButtonAnimation(view: View){
        val rotate = RotateAnimation(0f, 180f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        rotate.duration = 500
        rotate.interpolator = LinearInterpolator()
        view.startAnimation(rotate)
    }

}

//=============================EXTENSION FUNCTIONS=======================================//


private fun Array<Syllabus>.filterSems(selectedSem: String): Array<Syllabus> {
    val syllabusList = ArrayList<Syllabus>()
    this
            .filter { it.sem == selectedSem }
            .forEach { syllabusList.add(it) }
    return syllabusList.toTypedArray()
}

private fun Array<Syllabus>.filterYears(selectedYear: String): Array<Syllabus> {
    val syllabusList = ArrayList<Syllabus>()
    this
            .filter { it.year == selectedYear }
            .forEach { syllabusList.add(it) }
    return syllabusList.toTypedArray()
}

private fun Array<Syllabus>.filterRegulations(selectedRegulation: String): Array<Syllabus> {
    val syllabusList = ArrayList<Syllabus>()
    this
            .filter { it.regulation == selectedRegulation }
            .forEach { syllabusList.add(it) }
    return syllabusList.toTypedArray()
}

private fun Array<Syllabus>.filterBranches(selectedCourse: String): Array<Syllabus> {
    val syllabusList = ArrayList<Syllabus>()
    this
            .filter { it.branch == selectedCourse }
            .forEach { syllabusList.add(it) }
    return syllabusList.toTypedArray()
}

private fun Array<Syllabus>.filterUniv(selectedCollege: String): Array<Syllabus> {
    val syllabusList = ArrayList<Syllabus>()
    this
            .filter { it.univ == selectedCollege }
            .forEach { syllabusList.add(it) }
    return syllabusList.toTypedArray()
}

private fun Array<Syllabus>.univArray(): Array<String>? {
    val univList = TreeSet<String>()
    univList.add("--select--")
    this.mapTo(univList) { it.univ }
    return univList.toTypedArray()
}

private fun Array<Syllabus>.branchArray(): Array<String>? {
    val univList = TreeSet<String>()
    univList.add("--select--")
    this.mapTo(univList) { it.branch }
    return univList.toTypedArray()
}

private fun Array<Syllabus>.regulationArray(): Array<String>? {
    val univList = TreeSet<String>()
    univList.add("--select--")
    for (syllabus in this){
        syllabus.regulation?.let { univList.add(it) }
    }
    return univList.toTypedArray()
}

private fun Array<Syllabus>.yearArray(): Array<String>? {
    val univList = TreeSet<String>()
    univList.add("--select--")
    this.mapTo(univList) { it.year }
    return univList.toTypedArray()
}

private fun Array<Syllabus>.semArray(): Array<String>? {
    val univList = TreeSet<String>()
    univList.add("--select--")
    this.mapTo(univList) { it.sem }
    return univList.toTypedArray()
}
