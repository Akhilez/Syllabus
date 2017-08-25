package com.homemade.akhilez.syllabus.frags

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import com.github.paolorotolo.appintro.AppIntro
import com.homemade.akhilez.syllabus.R
import com.homemade.akhilez.syllabus.SettingsActivity

/**
 * Created by Akhil on 8/25/2017.
 *
 */

class IntroActivity : AppIntro() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addSlide(IntroSlide.page(1))
        addSlide(IntroSlide.page(2))
        addSlide(IntroSlide.page(3))

        setBarColor(ContextCompat.getColor(applicationContext, R.color.colorAccent))
        setSeparatorColor(ContextCompat.getColor(applicationContext, R.color.colorAccent))

        showSkipButton(true)
        isProgressButtonEnabled = true

    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        done()
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        done()
    }

    private fun done(){
        val sharedPref = getSharedPreferences("common", Context.MODE_PRIVATE)
        if (sharedPref?.getString("syllabusId", null) == null) gotoSettings()
        else finish()
    }

    override fun onSlideChanged(oldFragment: Fragment?, newFragment: Fragment?) {
        super.onSlideChanged(oldFragment, newFragment)
        // Do something when the slide changes.
    }


    private fun gotoSettings(): Any?{
        val intent = Intent(this@IntroActivity, SettingsActivity::class.java)
        startActivity(intent)
        finish()
        return null
    }
}