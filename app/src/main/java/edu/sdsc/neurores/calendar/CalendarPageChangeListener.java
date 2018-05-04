package edu.sdsc.neurores.calendar;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.TextView;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import edu.sdsc.neurores.R;
import edu.sdsc.neurores.calendar.abstraction.CalendarBackedDay;
import edu.sdsc.neurores.calendar.abstraction.EventCalendar;
import edu.sdsc.neurores.calendar.abstraction.Week;

/**
 * Created by trevor on 4/21/18.
 */

public class CalendarPageChangeListener implements ViewPager.OnPageChangeListener {
    Context context;
    EventCalendar eventCalendar;

    CalendarPageChangeListener(Context context, EventCalendar eventCalendar){
        this.context = context;
        this.eventCalendar = eventCalendar;
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        Week selectedWeek = eventCalendar.getWeek(position);
        TextView textView = (TextView)((Activity)context).findViewById(R.id.calendar_title);
        Calendar today = Calendar.getInstance();

        if(selectedWeek.isWithinWeek(new CalendarBackedDay(today))){
            String monthName = today.getDisplayName(Calendar.MONTH,Calendar.LONG, Locale.getDefault());
            textView.setText(monthName + " " + today.get(Calendar.YEAR));
        }else{
            Log.v("taggy", "Getting selected " + selectedWeek.getMonthName());
            textView.setText(selectedWeek.getMonthName() + " " + selectedWeek.getYear());
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private Calendar getWeek(int position){
        Calendar current = Calendar.getInstance();
        current.setTimeInMillis((long)((1000L * 60L * 60L * 24L * 7L) * position) + eventCalendar.getStartTimeMillis());
        return current;
    }

}
