package edu.sdsc.neurores.calendar.abstraction;

import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by trevor on 4/25/18.
 */

public class CalendarBackedEventCalendar implements EventCalendar {
    Calendar start, end;

    ArrayList<Event> events;
    public CalendarBackedEventCalendar(Calendar start, Calendar end, ArrayList<Event> events){
        start.set(Calendar.DAY_OF_WEEK, 0);
        end.set(Calendar.DAY_OF_WEEK, 0);

        start.set(Calendar.HOUR_OF_DAY, 0);
        start.clear(Calendar.MINUTE);
        start.clear(Calendar.SECOND);
        start.clear(Calendar.MILLISECOND);

        start.set(Calendar.DAY_OF_WEEK, start.getFirstDayOfWeek());

        this.start = start;
        this.end = end;
        this.events = events;
    }


    @Override
    public CalendarBackedWeek getWeek(int position) {
        Calendar current = Calendar.getInstance();
        long startTime = (long)((1000L * 60L * 60L * 24L * 7L) * position) + start.getTimeInMillis();
        long endTime = (long)((1000L * 60L * 60L * 24L * 7L) * (position + 1)) + start.getTimeInMillis();
        current.setTimeInMillis((long)(startTime));
        return new CalendarBackedWeek(current, filterByWeekRange(events, new Date(startTime), new Date(endTime)));
    }

    private ArrayList<Event> filterByWeekRange(ArrayList<Event> events, Date startDate, Date endDate){
        ArrayList<Event> ret = new ArrayList<>();

        for(Event e: events){
            if(e.isBetween(startDate, endDate))
                ret.add(e);
        }

        return ret;
    }

    @Override
    public int getNumWeeksInCalendar() {
        return (int) ((end.getTimeInMillis() - start.getTimeInMillis()) / (1000L * 60L * 60L * 24L * 7L));
    }

    @Override
    public long getStartTimeMillis() {
        return start.getTimeInMillis();
    }

    @Override
    public int getWeekPosition(Week week) {
        Calendar compareCal = Calendar.getInstance();
        compareCal.setTimeInMillis(week.getStartOfWeek());
        Log.v("calendar", "Looking for " + compareCal.get(Calendar.WEEK_OF_YEAR) + " " + compareCal.get(Calendar.YEAR));


        for(int i = 0; i < getNumWeeksInCalendar(); i++){
            Week currentWeek = getWeek(i);
            Calendar currentCalendar = Calendar.getInstance();
            currentCalendar.setTimeInMillis(currentWeek.getStartOfWeek());

            boolean sameYear = compareCal.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR);
            boolean sameWeek = compareCal.get(Calendar.WEEK_OF_YEAR) == currentCalendar.get(Calendar.WEEK_OF_YEAR);

            if(sameYear && sameWeek){
                Log.v("calendar","Found " + compareCal.get(Calendar.WEEK_OF_YEAR) +
                        " " + compareCal.get(Calendar.YEAR));
                return i;
            }
        }
        return -1;
    }
}
