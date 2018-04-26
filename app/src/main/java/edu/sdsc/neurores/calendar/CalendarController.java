package edu.sdsc.neurores.calendar;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import java.util.Calendar;

import edu.sdsc.neurores.calendar.abstraction.CalendarBackedEventCalendar;
import edu.sdsc.neurores.calendar.abstraction.Day;
import edu.sdsc.neurores.calendar.abstraction.EventCalendar;
import edu.sdsc.neurores.calendar.adapter.CalendarAdapter;

/**
 * Created by trevor on 4/21/18.
 */

public class CalendarController {
    private Context context;
    private Calendar start, end;
    private CalendarAdapter pagerAdapter;
    private View.OnClickListener onClickListener;
    private AdapterView.OnItemClickListener onItemClickListener;
    private ViewPager.OnPageChangeListener onPageChangeListener;

    public CalendarController(Context context, Calendar start, Calendar end, DayClickListener dayClickListener){
        start = (Calendar) start.clone();
        end = (Calendar) end.clone();

        start.set(Calendar.DAY_OF_WEEK, 0);
        end.set(Calendar.DAY_OF_WEEK, 0);

        start.set(Calendar.HOUR_OF_DAY, 0);
        start.clear(Calendar.MINUTE);
        start.clear(Calendar.SECOND);
        start.clear(Calendar.MILLISECOND);

        start.set(Calendar.DAY_OF_WEEK, start.getFirstDayOfWeek());

        this.context = context;
        this.start = start;
        this.end = end;

        EventCalendar eventCalendar = new CalendarBackedEventCalendar(start,end);

        CalendarClickListener calendarClickListener = new CalendarClickListener();
        this.onClickListener = calendarClickListener;
        this.onItemClickListener = calendarClickListener;
        pagerAdapter = new CalendarAdapter(context, start,end, dayClickListener);
        calendarClickListener.setCalendarAdapter(pagerAdapter);
        onPageChangeListener = new CalendarPageChangeListener(context, eventCalendar);
    }

    public int getWeekPosition(Calendar calendar){
        calendar = (Calendar) start.clone();

        calendar.set(Calendar.DAY_OF_WEEK, 0);

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.clear(Calendar.MINUTE);
        calendar.clear(Calendar.SECOND);
        calendar.clear(Calendar.MILLISECOND);

        calendar.set(Calendar.DAY_OF_WEEK, start.getFirstDayOfWeek());

        return 0;
    }

    public PagerAdapter getPagerAdapter(){
        return pagerAdapter;
    }

    public View.OnClickListener getOnClickListener(){
        return onClickListener;
    }

    public AdapterView.OnItemClickListener getOnItemClickListener(){
        return onItemClickListener;
    }

    public ViewPager.OnPageChangeListener getOnPageChangeListener(){
        return onPageChangeListener;
    }
}
