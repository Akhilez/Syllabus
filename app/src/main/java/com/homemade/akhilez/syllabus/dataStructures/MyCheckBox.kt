package dataStructures

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.AsyncTask
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.widget.CheckBox
import com.homemade.akhilez.syllabus.R
import com.homemade.akhilez.syllabus.db.OpenDBHelper

/**
 * Created by Akhil on 8/18/2017.
 *
 */

class MyCheckBox(context: Context, attrSet: AttributeSet?) : CheckBox(context, attrSet) {

    companion object {
        const val UNCHECKED_UNIMP = 0
        const val CHECKED_UNIMP = 1
        const val UNCHECKED_IMP = 2
        const val CHECKED_IMP = 3
    }

    var unitId: String? = null
    var conceptId: Int? = null
    var subjectId: Int? = null

    var imp: Boolean = false
        set(status) {
            field = status
            buttonTintList = ColorStateList(
                    arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf(-android.R.attr.state_checked)),
                    intArrayOf(
                            if(!status && isChecked) ContextCompat.getColor(context, R.color.colorAccent)
                            else ContextCompat.getColor(context, R.color.tickSecondary),

                            if (status) ContextCompat.getColor(context, R.color.tickSecondary)
                            else ContextCompat.getColor(context, R.color.darkPrimary)
                    )
            )
            if(status) {
                setTextColor(Color.WHITE)
                setTypeface(null, Typeface.BOLD)

                if(isChecked) markConcept(CHECKED_IMP)
                else markConcept(UNCHECKED_IMP)

            } else {
                setTextColor(ContextCompat.getColor(context, R.color.myCheckBoxTextColor))
                setTypeface(null, Typeface.NORMAL)

                if(isChecked) markConcept(CHECKED_UNIMP)
                else markConcept(UNCHECKED_UNIMP)
            }
        }

    private fun dpToPx(dp: Int) = Math.round(dp * (context.resources.displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))

    constructor(context: Context) : this(context, attrSet = null) {
        setPadding(dpToPx(5), dpToPx(5), dpToPx(75), dpToPx(5))
        setTypeface(Typeface.SANS_SERIF, Typeface.NORMAL)
        textSize = 16f
    }

    override fun setChecked(status: Boolean) {
        super.setChecked(status)

        if (!imp) {
            buttonTintList = ColorStateList(
                    arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf(-android.R.attr.state_checked)),
                    intArrayOf(
                            ContextCompat.getColor(context, R.color.colorAccent),
                            ContextCompat.getColor(context, R.color.darkPrimary)
                    )
            )
            setTextColor(ContextCompat.getColor(context, R.color.myCheckBoxTextColor))
        }

        if(status) {
            paintFlags = Paint.STRIKE_THRU_TEXT_FLAG

            if (imp) markConcept(CHECKED_IMP)
            else markConcept(CHECKED_UNIMP)

        }else{
            paintFlags = Paint.LINEAR_TEXT_FLAG

            if (imp) markConcept(UNCHECKED_IMP)
            else markConcept(UNCHECKED_UNIMP)
        }
    }

    private fun markConcept(markValue: Int){
        class MarkConcept : AsyncTask<Void, Void, Void?>() {
            override fun doInBackground(vararg p0: Void?): Void? {
                OpenDBHelper(context).markConcept(subjectId!!, unitId!!, conceptId!!, markValue)
                return null
            }
        }
        if(subjectId != null && unitId != null && conceptId != null)
            MarkConcept().execute()
    }

    fun initialize(check: String) {
        try {
            when (check.toInt()) {
                CHECKED_UNIMP -> isChecked = true
                UNCHECKED_IMP -> imp = true
                CHECKED_IMP -> {
                    imp = true; isChecked = true
                }
            }
        } catch(e: NumberFormatException){
            e.printStackTrace()
        }
    }
}