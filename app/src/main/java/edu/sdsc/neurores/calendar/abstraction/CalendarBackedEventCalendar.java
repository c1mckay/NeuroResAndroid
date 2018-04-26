package edu.sdsc.neurores.calendar.abstraction;

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
        return (int) ((end.getTimeInMillis() - start.getTimeInMillis()) / (1000 * 60 * 60 * 24 * 7));
    }

    @Override
    public long getStartTimeMillis() {
        return start.getTimeInMillis();
    }
}
