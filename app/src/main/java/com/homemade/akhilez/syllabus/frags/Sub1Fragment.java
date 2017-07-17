package com.homemade.akhilez.syllabus.frags;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.TranslateAnimation;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.emilsjolander.components.StickyScrollViewItems.StickyScrollView;
import com.homemade.akhilez.syllabus.MainActivity;
import com.homemade.akhilez.syllabus.OpenDBHelper;
import com.homemade.akhilez.syllabus.R;
import com.homemade.akhilez.syllabus.SettingsActivity;

public class Sub1Fragment extends Fragment {

    public int position=-1, mScrollY=0, curLoc=0;
    public static int checkId;
    public TextView[] unitTextViews,unitBarTextViews;
    public RelativeLayout unitBar,highlighter, fullContainer;
    public StickyScrollView scrollView;
    public LinearLayout[] linearLayouts;
    public OpenDBHelper openDBHelper;
    DisplayMetrics displayMetrics;
    String[] units;
    RelativeLayout[] unitHeaders;
    TranslateAnimation anim;
    LinearLayout linearLayoutMain;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_subs, container, false);

        //FIRST TIME?
        SharedPreferences sharedPref = getActivity().getSharedPreferences("common", Context.MODE_PRIVATE);
        if(sharedPref.getString("firstTime","true").equals("true"))
            return rootView;

        //is position correct?
        if(position == -1) position = getContext().getSharedPreferences("common", Context.MODE_PRIVATE).getInt("lastTab",0);

        linearLayoutMain = (LinearLayout) rootView.findViewById(R.id.innerLinear);
        units = MainActivity.Companion.getAllUnits()[position];
        unitHeaders = new RelativeLayout[units.length];
        unitTextViews = new TextView[units.length];
        linearLayouts = new LinearLayout[units.length];
        displayMetrics = getContext().getResources().getDisplayMetrics();
        openDBHelper = new OpenDBHelper(getContext());
        initializeCheckId();


        //ASYNC
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                paintAsync();
            }
        });
        /*
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                paintAsync(); //Shows view
            }
        });*/
        //new Task1().execute();
        //paintAsync();


        fullContainer = (RelativeLayout) rootView.findViewById(R.id.fullContainer);
        scrollView = (StickyScrollView) rootView.findViewById(R.id.scrollView);
        if(units.length!=1) {
            buildUnitBar(units.length);
            fullContainer.addView(unitBar);

        }


        Display mdisp = getActivity().getWindowManager().getDefaultDisplay();
        Point mdispSize = new Point();
        mdisp.getSize(mdispSize);
        int maxY = mdispSize.y;
        final int halfY = maxY/3;


        //SCROLL LISTENER
        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                if(unitHeaders.length == 0)return;
                if(scrollView.getScrollY()!=0)mScrollY=scrollView.getScrollY();
                int i;
                for(i=0;i<unitHeaders.length;i++) {
                    if(unitHeaders[i]==null) return;
                    if ((unitHeaders[i].getTop() - mScrollY) > halfY) break;
                }
                if(i<=unitTextViews.length) animateHighlighter(i-1);
            }
        });

        return rootView;
    }



    void paintAsync(){
        for(int i=0;i<units.length;i++){
            addNewUnit(units[i],linearLayoutMain,i);
            String[] concepts = MainActivity.Companion.getAllConcepts()[position][i];
            //String[] checkFlags = MainActivity.allCheckFlags[position][i];
            String[] checkFlags = openDBHelper.getCheckFlags(MainActivity.Companion.getSubjects()[position],units[i]);
            linearLayouts[i] = new LinearLayout(getContext());
            LinearLayout.LayoutParams linearParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
            linearParams.leftMargin = dpToPx(5);
            linearParams.rightMargin = dpToPx(50);
            linearParams.bottomMargin = dpToPx(15);
            linearParams.topMargin = dpToPx(5);
            linearLayouts[i].setOrientation(LinearLayout.VERTICAL);
            linearLayouts[i].setLayoutParams(linearParams);
            linearLayouts[i].setFocusableInTouchMode(true);
            //linearLayouts[i].setFocusable(true);
            for(int j=0;j<concepts.length;j++){
                try {
                    addNewConcept(concepts[j], checkFlags[j], linearLayouts[i], checkId);
                    checkId++;
                }catch (ArrayIndexOutOfBoundsException excep){
                    OpenDBHelper openDBHelper = new OpenDBHelper(getContext());
                    openDBHelper.onUpgrade(openDBHelper.getWritableDatabase(),1,2);
                    Toast.makeText(getContext(), "Please reselect the class", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(getContext(), SettingsActivity.class);
                    getContext().startActivity(intent);
                    getActivity().finish();
                }
            }
            linearLayoutMain.addView(linearLayouts[i]);

        }

    }

    public void initializeCheckId(){
        checkId = 0;
        Integer[] number =  openDBHelper.getNoOfConcepts();
        if(number.length==0) return;
        for(int i=0;i<position;i++)
            checkId += number[i];
    }

    public void buildUnitBar(int noOfUnits){
        if(noOfUnits==1) return;
        //building background
        unitBar = new RelativeLayout(getContext());
        RelativeLayout.LayoutParams unitBarParams = new RelativeLayout.LayoutParams(dpToPx(40),noOfUnits*dpToPx(36));
        unitBarParams.topMargin=dpToPx(179);
        unitBarParams.rightMargin = dpToPx(17);
        unitBarParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        unitBar.setBackground(new ColorDrawable(getResources().getColor(R.color.darkPrimary)));
        //unitBar.setElevation(dpToPx(6));
        unitBar.setPadding(dpToPx(5),dpToPx(5),dpToPx(5),dpToPx(5));
        unitBar.setAlpha(0.8f);
        unitBar.setClickable(true);
        unitBar.setLayoutParams(unitBarParams);

        //Building highlighter
        highlighter = new RelativeLayout(getContext());
        RelativeLayout.LayoutParams highlighterParams = new RelativeLayout.LayoutParams(dpToPx(30),dpToPx(31));
        highlighter.setBackground(getResources().getDrawable(R.drawable.unit_highlighter));
        highlighter.setGravity(RelativeLayout.CENTER_HORIZONTAL);
        //highlighterParams.setMargins(0,dpToPx(0),0,0);
        highlighter.setLayoutParams(highlighterParams);
        unitBar.addView(highlighter);

        //building the textViews
        unitBarTextViews = new TextView[noOfUnits];
        for(int i=0; i<noOfUnits; i++){
            unitBarTextViews[i]= new TextView(getContext());
            unitBarTextViews[i].setText(Integer.toString(i+1));
            unitBarTextViews[i].setId(20+i);
            unitBarTextViews[i].setTextColor(Color.parseColor("#929292"));
            //unitBarTextViews[i].setPadding(0,dpToPx(1),0,0);
            RelativeLayout.LayoutParams textParams = new RelativeLayout.LayoutParams(dpToPx(30),dpToPx(30));
            textParams.topMargin=dpToPx(35*i);
            unitBarTextViews[i].setGravity(Gravity.CENTER);
            unitBarTextViews[i].setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            unitBarTextViews[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    unitPressed(v);
                }
            });
            unitBarTextViews[i].setLayoutParams(textParams);
            unitBar.addView(unitBarTextViews[i]);
        }


    }

    public void unitPressed(View view){
        int unitNumber = Integer.parseInt(((TextView)view).getText().toString())-1;
        if(linearLayouts[unitNumber]!=null) {
            int height =0;
            for(int i=0;i<unitNumber;i++){
                height += (unitTextViews[i].getHeight()+dpToPx(32));
                height += linearLayouts[i].getHeight();
            }
            scrollView.smoothScrollTo(0,height);
        }
        animateHighlighter(unitNumber);
    }

    public void animateHighlighter(final int unit){
        if(units.length==1) return;
        if(anim==null || anim.hasEnded()) {
            float startY = curLoc*dpToPx(35);
            final float finishY = (unit)*dpToPx(35);
            anim = new TranslateAnimation(0, 0, startY, finishY);
            anim.setDuration(200);
            anim.setFillAfter(true);

            highlighter.startAnimation(anim);

            curLoc = unit;
        }
    }

    public void addNewUnit(String unit, LinearLayout linearLayout,int count){
        unitTextViews[count] = new TextView(getContext());
        unitTextViews[count].setText(unit);
        unitTextViews[count].setGravity(Gravity.CENTER_VERTICAL);
        unitTextViews[count].setTextColor(Color.parseColor("#929292"));
        unitTextViews[count].setTextSize(16);
        unitTextViews[count].setAllCaps(true);
        unitTextViews[count].setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        unitTextViews[count].setFocusableInTouchMode(true);
        RelativeLayout relativeLayout = new RelativeLayout(getContext());
        RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        //relativeParams.topMargin=dpToPx(20);
        relativeLayout.setLayoutParams(relativeParams);
        relativeLayout.setBackgroundColor(Color.parseColor("#212121"));
        relativeLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        relativeLayout.setPadding(dpToPx(5),dpToPx(5),dpToPx(5),dpToPx(5));
        relativeLayout.setTag("sticky");
        //relativeLayout.setElevation(6);
        relativeLayout.addView(unitTextViews[count]);
        if(count!=0){
            RelativeLayout divider = new RelativeLayout(getContext());
            divider.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,dpToPx(2)));
            divider.setBackgroundColor(Color.parseColor("#aa0000"));
            linearLayout.addView(divider);
        }

        unitHeaders[count] = relativeLayout;
        linearLayout.addView(relativeLayout);
        unitTextViews[count]=unitTextViews[count];
    }

    public void addNewConcept(String concept, String checkFlag, LinearLayout linearLayout,int id){
        CheckBox checkBox = new CheckBox(getContext());
        checkBox.setTag(id);
        if(checkFlag.equals("true"))
            checkBox.setChecked(true);
        else checkBox.setChecked(false);
        checkBox.setText(concept);
        checkBox.setPadding(dpToPx(5),dpToPx(5),dpToPx(20),dpToPx(5));
        checkBox.setTextColor(Color.WHITE);
        checkBox.setOnClickListener(new CustomClickListener());
        linearLayout.addView(checkBox);
    }

    public String[] getDetails(){
        SharedPreferences sharedPref = getActivity().getSharedPreferences("common", Context.MODE_PRIVATE);
        String[] details = new String[] {
                sharedPref.getString("college","noCollege"),
                sharedPref.getString("course","noCourse"),
                sharedPref.getString("year","noYear"),
                sharedPref.getString("semester","noSemester")
        };
        return details;
    }

    public int dpToPx(int dp) {
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }


    public class CustomClickListener implements CheckBox.OnClickListener{
        @Override
        public void onClick(View view) {
            boolean checked = ((CheckBox) view).isChecked();
            OpenDBHelper openDBHelper = new OpenDBHelper(getContext());
            int id = (int)view.getTag();
            if (checked)
                openDBHelper.checkFlagAt(id);
            else
                openDBHelper.unCheckFlagAt(id);
        }
    }

}

