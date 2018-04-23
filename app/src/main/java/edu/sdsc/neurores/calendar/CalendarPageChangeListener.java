package edu.sdsc.neurores.calendar;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.widget.TextView;

import java.text.DateFormatSymbols;
import java.util.Calendar;

import edu.sdsc.neurores.R;

/**
 * Created by trevor on 4/21/18.
 */

public class CalendarPageChangeListener implements ViewPager.OnPageChangeListener {
    Context context;
    Calendar start, end;

    CalendarPageChangeListener(Context context, Calendar start, Calendar end){
        this.context = context;
        this.start = start;
        this.end = end;
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        Calendar calendar = getWeek(position);
        calendar.add(Calendar.DAY_OF_MONTH, 6);
        String month = intMonthToString(calendar.get(Calendar.MONTH));
        String year = String.valueOf(calendar.get(Calendar.YEAR));

        TextView textView = (TextView)((Activity)context).findViewById(R.id.calendar_title);
        textView.setText(month + " " + year);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private Calendar getWeek(int position){
        Calendar current = Calendar.getInstance();
        current.setTimeInMillis((long)((1000L * 60L * 60L * 24L * 7L) * position) + start.getTimeInMillis());
        return current;
    }

    private String intMonthToString(int month){
        return new DateFormatSymbols().getMonths()[month-1];
    }
}
