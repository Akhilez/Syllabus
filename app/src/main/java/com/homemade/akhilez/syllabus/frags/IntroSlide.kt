package com.homemade.akhilez.syllabus.frags

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.homemade.akhilez.syllabus.R

/**
 * Created by Akhil on 8/25/2017.
 *
 */

class IntroSlide : Fragment() {

    private var rootView: View? = null
    var pageNo: Int? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater!!.inflate(R.layout.first_intro_slide, container, false)

        val image = rootView!!.findViewById(R.id.imageView) as ImageView
        Glide.with(this).load(getIntroImage(pageNo)).into(image)

        return rootView
    }

    private fun getIntroImage(pageNo: Int?): Int =
        when (pageNo){
            1 -> R.drawable.intro1
            2 -> R.drawable.intro2
            else -> R.drawable.intro3
        }


    companion object {
        fun page(pgNo: Int): IntroSlide {
            val introPage = IntroSlide()
            introPage.pageNo = pgNo
            return introPage
        }
    }

}
