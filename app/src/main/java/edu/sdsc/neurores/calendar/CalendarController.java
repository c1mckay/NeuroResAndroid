package edu.sdsc.neurores.calendar;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import edu.sdsc.neurores.calendar.abstraction.CalendarBackedEventCalendar;
import edu.sdsc.neurores.calendar.abstraction.Day;
import edu.sdsc.neurores.calendar.abstraction.Event;
import edu.sdsc.neurores.calendar.abstraction.EventCalendar;
import edu.sdsc.neurores.calendar.abstraction.Week;
import edu.sdsc.neurores.calendar.adapter.CalendarAdapter;

/**
 * Created by trevor on 4/21/18.
 */

public class CalendarController {
    private Context context;
    private Calendar start, end;
    private CalendarAdapter pagerAdapter;
    private ViewPager.OnPageChangeListener onPageChangeListener;
    private EventCalendar eventCalendar;
    private DayClickHandler dayClickHandler;
    private List<Event> events;

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
        this.events = new ArrayList<>();

        eventCalendar = new CalendarBackedEventCalendar(start,end, events);
        dayClickHandler = new DayClickHandler();
        dayClickHandler.registerDayClickListener(dayClickListener);
        pagerAdapter = new CalendarAdapter(context, start,end, dayClickHandler, events);
        onPageChangeListener = new CalendarPageChangeListener(context, eventCalendar);
    }

    public int getPositionOfWeek(Week week){
        return eventCalendar.getWeekPosition(week);
    }

    public CalendarAdapter getPagerAdapter(){
        return pagerAdapter;
    }

    public ViewPager.OnPageChangeListener getOnPageChangeListener(){
        return onPageChangeListener;
    }

    public Week getWeekAtPosition(int position){
        return eventCalendar.getWeek(position);
    }

    public void setEvents(List<Event> events) {
        this.events = events;
        pagerAdapter = new CalendarAdapter(context, start,end, dayClickHandler, events, );
    }
}
