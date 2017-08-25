package com.homemade.akhilez.syllabus


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.homemade.akhilez.syllabus.dataStructures.Subject
import com.homemade.akhilez.syllabus.db.OpenDBHelper
import com.homemade.akhilez.syllabus.frags.IntroActivity
import com.homemade.akhilez.syllabus.frags.Sub1Fragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.nav_header.view.*



class MainActivity : AppCompatActivity() {

    private var drawerToggle: ActionBarDrawerToggle? = null
    private var subjects: Array<Subject>? = null
    private var sharedPref: SharedPreferences? = null
    private var details = HashMap<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        /*
        1. if no syllabusId, go to settings else get details
        2. set up tab layout
        3. set up viewPager
        4. set up navigation drawer
        5. set class header
        6. background db sync
        7. add ad
         */
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        //1. if no syllabusId, go to settings else get details
        sharedPref = getSharedPreferences("common", Context.MODE_PRIVATE)
        if (sharedPref?.getString("syllabusId", null) == null) gotoIntro() ?: return

        details = getDetails()
        val openHelper = OpenDBHelper(this@MainActivity)
        subjects = openHelper.getSubjects()

        //2. set up tab layout
        setUpTabLayout()

        //3. set up viewPager
        setUpViewPager()

        //4. set up navigation drawer
        setDrawer(subjects)

        //5. set class header
        detailsHeader.text = (details["branch"] + " " + yearSemFormat())

        //6. background db sync
        //backgroundSync(openHelper)

        //7. add ad
        addAd()

    }

    private fun backgroundSync(openDBHelper: OpenDBHelper){
        class BGSync : AsyncTask<Void, Void, Void?>() {
            override fun doInBackground(vararg p0: Void?): Void? {
                try{
                    openDBHelper.syncDb()
                }catch(e: Exception){
                    e.printStackTrace()
                }
                return null
            }
        }
        BGSync().execute()
    }

    private fun setUpTabLayout(){
        tab_layout.setSelectedTabIndicatorHeight(10)

        for (subject in subjects!!)
            tab_layout.addTab(tab_layout.newTab().setText(subject.name))

        tab_layout.tabGravity = TabLayout.GRAVITY_FILL

        tab_layout.setOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val nvMenu = nvView.menu
                if (nvMenu != null && nvMenu.size() > tab.position) nvMenu.getItem(tab.position).isChecked = true
                goToSubject(tab.position)
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun setUpViewPager(){
        (viewPager as ViewPager).adapter = ListPagerAdapter(supportFragmentManager)

        (viewPager as ViewPager).addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tab_layout))

        //anchoring the tab
        viewPager.currentItem = sharedPref!!.getInt("lastTab", 0)
        //TODO: set drawer item to current item
    }

    private fun gotoIntro(): Any?{
        val intent = Intent(this@MainActivity, IntroActivity::class.java)
        startActivity(intent)
        finish()
        return null
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
                main_content!!.openDrawer(GravityCompat.START)
                return true
            }
            R.id.help -> {
                val intent = Intent(this, IntroActivity::class.java)
                this.startActivity(intent)
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

    private fun setDrawer(subjectNames: Array<Subject>?) {
        val nvMenu = nvView.menu
        nvMenu.clear()
        subjectNames?.indices?.map { nvMenu.add(0, Menu.FIRST + it, Menu.NONE, subjectNames[it].name) }?.forEach { it.isCheckable = true }
        nvView.itemTextColor = myColorStateList
        val header = nvView.getHeaderView(0)
        header.university.text = details["univ"]
        header.branch.text = details["branch"]
        header.year_sem.text = yearSemFormat()

        setupDrawerContent(nvView)
        drawerToggle = ActionBarDrawerToggle(this, main_content, toolbar, R.string.drawer_open, R.string.drawer_close)
        main_content.addDrawerListener(drawerToggle!!)
    }

    private fun setupDrawerContent(navigationView: NavigationView) {
        navigationView.setNavigationItemSelectedListener { menuItem ->
            menuItem.isChecked = true
            main_content!!.closeDrawers()
            goToSubject(menuItem.itemId - Menu.FIRST)
            true
        }
    }

    fun startSettingsIntent(view: View) {
        val intent = Intent(this, SettingsActivity::class.java)
        this.startActivity(intent)
    }

    private fun yearSemFormat(): String {
        return details["year"] + "-" + details["sem"]
    }

    private fun goToSubject(position: Int) {
        viewPager.setCurrentItem(position, true)

        val editor = sharedPref?.edit()
        editor?.putInt("lastTab", position)
        editor?.apply()
    }

    private fun addAd() {
        /*
        MobileAds.initialize(applicationContext, resources.getString(R.string.dummyBanner))
        val mAdView = adView
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
        */
    }

    private val myColorStateList: ColorStateList
        get() {
            val state = arrayOf(intArrayOf(-android.R.attr.state_enabled), intArrayOf(android.R.attr.state_enabled), intArrayOf(-android.R.attr.state_checked), intArrayOf(android.R.attr.state_pressed))
            val color = intArrayOf(ContextCompat.getColor(this@MainActivity, R.color.darkPrimary), Color.WHITE, Color.BLUE, Color.WHITE)
            return ColorStateList(state, color)
        }

    private fun getDetails(): HashMap<String, String> {
        val map = HashMap<String, String>()

        map.put("univ", sharedPref!!.getString("univ", "null"))
        map.put("branch",sharedPref!!.getString("branch", "null"))
        map.put("regulation",sharedPref!!.getString("regulation", "null"))
        map.put("year", sharedPref!!.getString("year", "null"))
        map.put("sem", sharedPref!!.getString("sem", "null"))

        return map
    }

    inner class MyPagerAdapter internal constructor(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            //This is for dynamic tabs
            val tab1 = Sub1Fragment()
            tab1.subject = subjects!![position]
            return tab1
        }

        override fun getCount(): Int {
            return subjects!!.size
        }
    }

    inner class ListPagerAdapter(private var fragmentManager: FragmentManager) : PagerAdapter() {
        private var fragments: Array<Fragment?> = arrayOfNulls(subjects!!.size)

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            assert(0 <= position && position < fragments.size)
            val trans = fragmentManager.beginTransaction()
            trans.remove(fragments[position])
            trans.commit()
            fragments[position] = null
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Fragment {
            val fragment = getItem(position)
            val trans = fragmentManager.beginTransaction()
            trans.add(container.id, fragment, "fragment:" + position)
            trans.commit()
            return fragment
        }

        override fun getCount(): Int {
            return fragments.size
        }

        override fun isViewFromObject(view: View, fragment: Any): Boolean {
            return (fragment as Fragment).view === view
        }

        private fun getItem(position: Int): Fragment {
            assert(0 <= position && position < fragments.size)
            if (fragments[position] == null) {
                val tab1 = Sub1Fragment()
                tab1.subject = subjects!![position]
                fragments[position] = tab1 //make your fragment here
            }
            return fragments[position]!!
        }
    }

}
