package edu.sdsc.neurores.calendar.abstraction;

import android.util.Log;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import edu.sdsc.neurores.helper.FormatHelper;

/**
 * Created by trevor on 4/25/18.
 */

public class CalendarBackedWeek implements Week {
    private Calendar calendar;
    private Day[] days;
    private List<Event> events;

    public CalendarBackedWeek(Calendar calendar, List<Event> events){
        this.events = events;

        if(events == null){
            events = new ArrayList<>();
        }
        this.calendar = (Calendar) calendar.clone();

        this.calendar.set(Calendar.DAY_OF_WEEK, 0);

        this.calendar.set(Calendar.HOUR_OF_DAY, 0);
        this.calendar.clear(Calendar.MINUTE);
        this.calendar.clear(Calendar.SECOND);
        this.calendar.clear(Calendar.MILLISECOND);

        this.calendar.set(Calendar.DAY_OF_WEEK, this.calendar.getFirstDayOfWeek());

        days = new Day[7];
        for(int i = 0; i < 7; i++){
            Calendar temp = (Calendar) calendar.clone();
            temp.add(Calendar.DAY_OF_MONTH, i);

            List<Event> eventsForDay = new ArrayList<>();
            for(Event event : events){
                if(event.timeOverlaps(temp)){
                    Event eventClone = (Event) event.clone();
                    eventsForDay.add(eventClone);
                }
            }
            days[i] =  new CalendarBackedDay(temp, eventsForDay);
            for(Event event : days[i].getEvents()){
                event.setDay(days[i]);
            }
        }
    }

    @Override
    public Day getDay(int position) {
        return days[position];
    }

    @Override
    public String getMonthName() {
        return intMonthToString(calendar.get(Calendar.MONTH));
    }

    @Override
    public int getYear() {
        return calendar.get(Calendar.YEAR);
    }

    @Override
    public int getMonth() {
        return calendar.get(Calendar.MONTH);
    }

    @Override
    public long getStartOfWeek() {
        return calendar.getTimeInMillis();
    }

    @Override
    public int getNumWeekInYear() {
        return calendar.get(Calendar.WEEK_OF_YEAR);
    }

    @Override
    public boolean isWithinWeek(Day day) {
        Calendar endOfWeek = (Calendar) calendar.clone();
        endOfWeek.add(Calendar.DAY_OF_YEAR, 7);

        int yearOfDay = day.getYear();
        int monthOfDay = day.getMonth();
        int dayOfMonthOfDay = day.getDayInMonth();

        Calendar dayCal = Calendar.getInstance();
        dayCal.set(Calendar.YEAR, yearOfDay);
        dayCal.set(Calendar.MONTH, monthOfDay);
        dayCal.set(Calendar.DAY_OF_MONTH, dayOfMonthOfDay);

        return calendar.before(dayCal) && endOfWeek.after(dayCal);
    }

    private String intMonthToString(int month){
        return new DateFormatSymbols().getMonths()[month];
    }
}
