package com.homemade.akhilez.syllabus.frags

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.homemade.akhilez.syllabus.MainActivity

/**
 * Created by Akhil on 8/14/2017.
 *
 */

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = Intent(this@SplashActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}