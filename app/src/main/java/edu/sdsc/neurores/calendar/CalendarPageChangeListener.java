package edu.sdsc.neurores.calendar;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.TextView;

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
    int previousMonthNum;
    int previousYearNum;

    CalendarPageChangeListener(Context context, EventCalendar eventCalendar){
        this.context = context;
        this.eventCalendar = eventCalendar;
        previousMonthNum = -1;
        previousYearNum = -1;
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

        Week selectedWeek = eventCalendar.getWeek(position);
        TextView textView = (TextView)((Activity)context).findViewById(R.id.calendar_title);
        Calendar today = Calendar.getInstance();

        // TODO Keep current month consistent when scrolling backwards (also grayed Days)

        if(selectedWeek.isWithinWeek(new CalendarBackedDay(today))){
            String monthName = today.getDisplayName(Calendar.MONTH,Calendar.LONG, Locale.getDefault());
            textView.setText(monthName + " " + today.get(Calendar.YEAR));
            previousMonthNum = today.get(Calendar.MONTH);
            previousYearNum = today.get(Calendar.YEAR);
        }else{
            textView.setText(selectedWeek.getMonthName() + " " + selectedWeek.getYear());
            previousMonthNum = selectedWeek.getMonth();
            previousYearNum = selectedWeek.getYear();
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
