package edu.sdsc.neurores.calendar.abstraction;

import android.util.Log;

import java.util.Calendar;

/**
 * Created by trevor on 4/25/18.
 */

public class CalendarBackedEventCalendar implements EventCalendar {
    Calendar start, end;

    public CalendarBackedEventCalendar(Calendar start, Calendar end){
        start.set(Calendar.DAY_OF_WEEK, 0);
        end.set(Calendar.DAY_OF_WEEK, 0);

        start.set(Calendar.HOUR_OF_DAY, 0);
        start.clear(Calendar.MINUTE);
        start.clear(Calendar.SECOND);
        start.clear(Calendar.MILLISECOND);

        start.set(Calendar.DAY_OF_WEEK, start.getFirstDayOfWeek());

        this.start = start;
        this.end = end;
    }


    @Override
    public CalendarBackedWeek getWeek(int position) {
        Calendar current = Calendar.getInstance();
        current.setTimeInMillis((long)((1000L * 60L * 60L * 24L * 7L) * position) + start.getTimeInMillis());
        return new CalendarBackedWeek(current);
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
