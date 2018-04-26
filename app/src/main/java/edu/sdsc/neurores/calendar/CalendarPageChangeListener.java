package edu.sdsc.neurores.calendar;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.widget.TextView;

import java.text.DateFormatSymbols;
import java.util.Calendar;

import edu.sdsc.neurores.R;
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

        Calendar calendar = getWeek(position);
        calendar.add(Calendar.DAY_OF_MONTH, 6);
        //String month = intMonthToString(calendar.get(Calendar.MONTH));
        //String year = String.valueOf(calendar.get(Calendar.YEAR));

        TextView textView = (TextView)((Activity)context).findViewById(R.id.calendar_title);
        textView.setText(selectedWeek.getMonthName() + " " + selectedWeek.getYear());
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
