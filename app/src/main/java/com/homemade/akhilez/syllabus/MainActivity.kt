package com.homemade.akhilez.syllabus


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewPager
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.homemade.akhilez.syllabus.frags.Sub1Fragment

class MainActivity : AppCompatActivity() {

    private var toolbar: Toolbar? = null

    var viewPager: ViewPager? = null

    private var drawerToggle: ActionBarDrawerToggle? = null
    private var mDrawer: DrawerLayout? = null
    private var nvMenu: Menu? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //ADS RELATED
        addAd()

        //First time?
        val sharedPref: SharedPreferences = getSharedPreferences("common", Context.MODE_PRIVATE)
        if (sharedPref.getString("firstTime", "true") == "true") {
            val intent = Intent(this@MainActivity, SettingsActivity::class.java)
            startActivity(intent)
            //finish();
            return
        }

        val assetHelper = AssetDBHelper(this)

        toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        val tabLayout = findViewById(R.id.tab_layout) as TabLayout
        tabLayout.setSelectedTabIndicatorHeight(10)

        //setting up subjects
        details = getDetails()
        subjects = assetHelper.getSubjects(details)
        val subjectNames = assetHelper.getNamesOf(subjects)
        for (i in subjectNames)
            tabLayout.addTab(tabLayout.newTab().setText(i))
        tabLayout.tabGravity = TabLayout.GRAVITY_FILL

        //setting up variables for the data
        allUnits = assetHelper.getAllUnits(subjects)
        allConcepts = assetHelper.getAllConcepts(subjects, allUnits)

        //setting up viewpager fragments
        viewPager = findViewById(R.id.container) as ViewPager
        val adapter = PagerAdapter(supportFragmentManager, tabLayout.tabCount)
        (viewPager as ViewPager).adapter = adapter


        ///*
        (viewPager as ViewPager).addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        tabLayout.setOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (nvMenu != null) nvMenu!!.getItem(tab.position).isChecked = true
                goToSubject(tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        //*/

        //anchoring the tab
        val lastTab = sharedPref.getInt("lastTab", 0)
        viewPager!!.currentItem = lastTab

        //setting up the navigation drawer--------------
        setDrawer(subjectNames)

        //setting up the details header
        val detailsHeader = findViewById(R.id.detailsHeader) as TextView
        detailsHeader.text = (details[1] + " " + yearSemFormat())

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        if (drawerToggle!!.onOptionsItemSelected(item)) return true

        when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                this.startActivity(intent)
                return true
            }
            R.id.home -> {
                mDrawer!!.openDrawer(GravityCompat.START)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        // Sync the toggle state after onRestoreInstanceState has occurred.
        if (drawerToggle != null) drawerToggle!!.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Pass any configuration change to the drawer toggles
        drawerToggle!!.onConfigurationChanged(newConfig)
    }

    private fun setDrawer(subjectNames: Array<String>) {
        mDrawer = findViewById(R.id.main_content) as DrawerLayout
        val nvDrawer = findViewById(R.id.nvView) as NavigationView

        nvMenu = nvDrawer.menu
        nvMenu!!.clear()
        subjectNames.indices
                .map { nvMenu!!.add(0, Menu.FIRST + it, Menu.NONE, subjectNames[it]) }
                .forEach { it.isCheckable = true }
        nvDrawer.itemTextColor = myColorStateList
        val header = nvDrawer.getHeaderView(0)
        (header.findViewById(R.id.university) as TextView).text = details[0]
        (header.findViewById(R.id.branch) as TextView).text = details[1]
        (header.findViewById(R.id.year_sem) as TextView).text = yearSemFormat()

        setupDrawerContent(nvDrawer)
        drawerToggle = ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.drawer_open, R.string.drawer_close)
        mDrawer!!.addDrawerListener(drawerToggle!!)
    }

    private fun setupDrawerContent(navigationView: NavigationView) {
        navigationView.setNavigationItemSelectedListener { menuItem ->
            menuItem.isChecked = true
            mDrawer!!.closeDrawers()
            goToSubject(menuItem.itemId - Menu.FIRST)
            true
        }
    }

    fun startSettingsIntent(view: View) {
        val intent = Intent(this, SettingsActivity::class.java)
        this.startActivity(intent)
    }

    private fun yearSemFormat(): String {
        return details[2] + "-" + details[3]
    }

    private fun goToSubject(position: Int) {
        viewPager?.setCurrentItem(position, true)
        val sharedPref: SharedPreferences = getSharedPreferences("common", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putInt("lastTab", position)
        editor.apply()
    }

    private fun addAd() {
        /*
        MobileAds.initialize(getApplicationContext(), getResources().getString(R.string.banner_ad_unit_id));
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        */
    }

    private val myColorStateList: ColorStateList
        get() {
            val state = arrayOf(intArrayOf(-android.R.attr.state_enabled), intArrayOf(android.R.attr.state_enabled), intArrayOf(-android.R.attr.state_checked), intArrayOf(android.R.attr.state_pressed))
            val color = intArrayOf(resources.getColor(R.color.darkPrimary), Color.WHITE, Color.BLUE, Color.WHITE)
            return ColorStateList(state, color)
        }

    inner class PagerAdapter internal constructor(fm: FragmentManager, internal var mNumOfTabs: Int) : FragmentStatePagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            //This is for dynamic tabs
            val tab1 = Sub1Fragment()
            tab1.position = position
            return tab1
        }

        override fun getCount(): Int {
            return mNumOfTabs
        }
    }

    fun getDetails(): Array<String> {
        val sharedPref = baseContext.getSharedPreferences("common", Context.MODE_PRIVATE)
        return arrayOf(sharedPref.getString("college", "noCollege"), sharedPref.getString("course", "noCourse"), sharedPref.getString("year", "noYear"), sharedPref.getString("semester", "noSem"))
    }

    companion object {
        var subjects: Array<String> = arrayOf()
        var details: Array<String> = arrayOf()
        var allUnits: Array<Array<String>> = arrayOf()
        var allConcepts: Array<Array<Array<String>>> = arrayOf()
    }


}
