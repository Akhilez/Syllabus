package com.homemade.akhilez.syllabus

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner

class SettingsActivity : AppCompatActivity() {

    var spinners: Array<Spinner> = arrayOf()
    var selectedCollege: String = ""
    var selectedCourse: String = ""
    var selectedYear: String = ""
    var selectedSemester: String = ""

    var assetHelper: AssetDBHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        spinners = arrayOf(
                findViewById(R.id.collegeSpinner) as Spinner,
                findViewById(R.id.courseSpinner) as Spinner,
                findViewById(R.id.yearSpinner) as Spinner,
                findViewById(R.id.semesterSpinner) as Spinner
        )

        assetHelper = AssetDBHelper(this)
        setUpSpinners()

        //ADS
        /*
        MobileAds.initialize(applicationContext, resources.getString(R.string.banner_ad_unit_id2))
        val mAdView = findViewById(R.id.adView) as AdView
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
        */

    }

    fun setUpSpinners() {
        val colleges = assetHelper?.collegess
        val collegeAdapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_item, colleges)
        collegeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinners[0].adapter = collegeAdapter
        spinners[0].setSelection(0)
        spinners[0].onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                selectedCollege = parent.getItemAtPosition(position) as String
                if (selectedCollege != "--select--") {
                    setCourseSpinner()
                    spinners[1].performClick()
                } else {
                    spinners[1].adapter = null
                    spinners[2].adapter = null
                    spinners[3].adapter = null
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
    }

    fun setCourseSpinner() {
        val courses = assetHelper?.getCourses(selectedCollege)
        val courseAdapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_item, courses)
        courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinners[1].adapter = courseAdapter
        spinners[1].setSelection(0)
        spinners[1].onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                selectedCourse = parent.getItemAtPosition(position) as String
                if (selectedCourse != "--select--") {
                    setYearSpinner()
                    spinners[2].performClick()
                } else {
                    spinners[2].adapter = null
                    spinners[3].adapter = null
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
    }

    fun setYearSpinner() {
        val years = assetHelper?.getYears(selectedCollege, selectedCourse)
        val yearAdapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_item, years)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinners[2].adapter = yearAdapter
        spinners[2].setSelection(0)
        spinners[2].onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                selectedYear = parent.getItemAtPosition(position) as String
                if (selectedYear != "--select--") {
                    setSemesterSpinner()
                    spinners[3].performClick()
                } else {
                    spinners[3].adapter = null
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
    }

    fun setSemesterSpinner() {
        val sems = assetHelper?.getSemesters(selectedCollege, selectedCourse, selectedYear)
        val semAdapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_item, sems)
        semAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinners[3].adapter = semAdapter
        spinners[3].setSelection(0)
        spinners[3].onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                selectedSemester = parent.getItemAtPosition(position) as String
                if (selectedSemester != "--select--") {
                    //setup loading message
                    ProgressDialog.show(this@SettingsActivity, "", "Loading. Please wait...", true)

                    val sharedPref = baseContext.getSharedPreferences("common", Context.MODE_PRIVATE)
                    val editor = sharedPref.edit()
                    val inDetails = arrayOf(sharedPref.getString("college", "noCollege"),
                            sharedPref.getString("course", "noCourse"),
                            sharedPref.getString("year", "noYear"),
                            sharedPref.getString("semester", "noSemester")
                    )
                    if (!(inDetails[0] == selectedCollege && inDetails[1] == selectedCourse && inDetails[2] == selectedYear && inDetails[3] == selectedSemester)) {

                        editor.putString("college", selectedCollege)
                        editor.putString("course", selectedCourse)
                        editor.putString("year", selectedYear)
                        editor.putString("semester", selectedSemester)
                        editor.putInt("lastTab", 0)
                        editor.apply()

                        val openHelper = OpenDBHelper(applicationContext)
                        openHelper.myUpgrade()
                        val details = arrayOf(selectedCollege, selectedCourse, selectedYear, selectedSemester)
                        val subjects = assetHelper?.getSubjects(details)
                        var i = 0
                        if (subjects != null) {
                            for (subject in subjects) {
                                val units = assetHelper?.getUnits(subject)
                                for (unit in units!!) {
                                    val concepts = assetHelper?.getConcepts(subject, unit)
                                    for (concept in concepts!!) {
                                        openHelper.insertNewConcept(i, subject, unit, concept)
                                        i++
                                    }
                                }
                            }
                        }

                    }
                    if (sharedPref.getString("firstTime", "true") == "true")
                        editor.putString("firstTime", "false")
                    editor.commit()
                    //onBackPressed();
                    val intentHome = Intent(baseContext, MainActivity::class.java)
                    intentHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intentHome)
                    finish()
                    //progressDialog.dismiss();
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
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

}
