package com.homemade.akhilez.syllabus.frags

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.TranslateAnimation
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.homemade.akhilez.syllabus.R
import com.homemade.akhilez.syllabus.dataStructures.Subject
import com.homemade.akhilez.syllabus.dataStructures.SyllabusUnit
import com.homemade.akhilez.syllabus.db.OpenDBHelper
import dataStructures.MyCheckBox
import kotlinx.android.synthetic.main.fragment_subs.*
import kotlinx.android.synthetic.main.fragment_subs.view.*
import kotlinx.android.synthetic.main.list_item_unit.view.*
import kotlinx.android.synthetic.main.unit_nav_item.view.*

class Sub1Fragment : Fragment() {

    private var curLoc = 0
    private var mScrollY = 0
    private var sharedPref: SharedPreferences? = null
    var subject: Subject? = null
    private var rootView: View? = null
    private var anim: TranslateAnimation? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        /*
        1. if no syllabus selected, return rootView
        3. create list view of the subject hierarchy
        4. create unit navigator
        5. add listeners
        6. initialize checkboxes
         */
        rootView = inflater!!.inflate(R.layout.fragment_subs, container, false)

        //1. if no syllabus selected, return rootView
        sharedPref = context.getSharedPreferences("common", Context.MODE_PRIVATE)
        if (sharedPref?.getString("syllabusId", null) == null) return rootView


        if (subject == null) {
            val position = context.getSharedPreferences("common", Context.MODE_PRIVATE).getInt("lastTab", 0)
            subject = OpenDBHelper(context).getSubject(position-1)
        }
        activity.runOnUiThread{
            //3. create list view of the subject hierarchy
            setUpListView(inflater, rootView!!, subject!!)

            //4. create unit navigator
            setUpUnitNavigator(inflater, subject!!)

            //5. add listeners
            addListeners()
        }

        return rootView
    }

    private fun addListeners(){
        val mdisp = activity.windowManager.defaultDisplay
        val mdispSize = Point()
        mdisp.getSize(mdispSize)
        val maxY = mdispSize.y
        val halfY = maxY / 3
        val numUnits = subject!!.units!!.size

        //SCROLL LISTENER
        rootView!!.scrollView.viewTreeObserver.addOnScrollChangedListener(ViewTreeObserver.OnScrollChangedListener {
            if (numUnits == 0) return@OnScrollChangedListener
            if (rootView!!.scrollView.scrollY != 0) mScrollY = rootView!!.scrollView.scrollY
            var i = 0
            while (i < numUnits) {
                if (rootView!!.unitsLayout.getChildAt(i).top - mScrollY > halfY) break
                i++
            }
            if (i <= numUnits && context != null) animateHighlighter(i - 1)
        })


    }

    private fun unitPressed(unitNumber: Int) {
        if (unitsLayout.getChildAt(unitNumber) != null) {
            val height = (0 until unitNumber).sumBy { unitsLayout.getChildAt(it).height }
            rootView!!.scrollView.smoothScrollTo(0, height)
        }
        animateHighlighter(unitNumber)
    }

    private fun animateHighlighter(unit: Int){
        if (subject!!.units!!.size == 1) return
        if (anim == null || anim!!.hasEnded()) {
            val startY = (curLoc * dpToPx(60)).toFloat()
            val finishY = (unit * dpToPx(60)).toFloat()
            anim = TranslateAnimation(0f, 0f, startY, finishY)
            anim!!.duration = 200
            anim!!.fillAfter = true

            unitHighlighter.startAnimation(anim)

            curLoc = unit
        }
    }

    private fun dpToPx(dp: Int) = Math.round(dp * (activity.resources.displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))

    private fun setUpListView(inflater: LayoutInflater, rootView: View, subject: Subject){
        var bookView: View? = null
        for (i in 0 until subject.units!!.size){
            val unitView = inflater.inflate(R.layout.list_item_unit, null, false)
            val currentUnit = subject.units!![i]

            unitView.unitText.text = currentUnit.unit
            setUpConcepts(unitView.conceptsContainer, currentUnit)

            if (currentUnit.unit == "Books") bookView = unitView
            else rootView.unitsLayout.addView(unitView)
        }
        if (bookView != null) rootView.unitsLayout.addView(bookView)

    }

    private fun setUpConcepts(container: LinearLayout, unit: SyllabusUnit){
        val concepts = unit.concepts.split(",")
        val checks = unit.checks.split(",")
        for (i in 0 until concepts.size) {
            val conceptView = MyCheckBox(context)

            conceptView.subjectId = subject!!.id.toInt()
            conceptView.unitId = unit.unit
            conceptView.conceptId = i

            var concept = concepts[i]
            if (concept.contains(":")) {
                val conceptArr = concept.split(":")
                if (conceptArr.size == 2){
                    concept = conceptArr[1]
                    container.addView(getChapterHeader(conceptArr[0]))
                }
            }
            conceptView.text = concept

            if (checks[i] != "0") conceptView.initialize(checks[i])

            conceptView.setOnLongClickListener({ conceptView.imp = !conceptView.imp; true })

            container.addView(conceptView)
        }
    }

    private fun getChapterHeader(heading: String): View{
        val layout = TextView(context)
        layout.text = heading
        layout.setTextColor(Color.WHITE)
        layout.setPadding(dpToPx(45), dpToPx(10), dpToPx(65), dpToPx(5))
        return layout
    }

    private fun setUpUnitNavigator(inflater: LayoutInflater, subject: Subject){
        /*
        1. if no of units <= 1, then hide units nav and return
        2. build nav text views
         */

        val numUnits = subject.units!!.size

        if (numUnits <= 1) {
            rootView?.unitNavContainer?.visibility = View.GONE
            return
        }

        var hasBooks = false
        for (i in 0 until subject.units!!.size) {
            if (subject.units!![i].unit == "Books") hasBooks = true
            rootView?.unitNavLinear?.addView(getNavUnitTextView(inflater, (i + 1).toString()))
        }

        if (hasBooks) {
            rootView!!.unitNavLinear.getChildAt(subject.units!!.size-1).unitNumber.text = "B"
        }


        //UNIT CHANGE LISTENER
        for (i in 0 until numUnits)
            rootView!!.unitNavLinear.getChildAt(i).setOnClickListener({ unitPressed(i) })

    }

    private fun getNavUnitTextView(inflater: LayoutInflater, text: String): RelativeLayout{
        val newUnit = inflater.inflate(R.layout.unit_nav_item, null, false) as RelativeLayout
        newUnit.unitNumber.text = text
        return newUnit
    }

}
