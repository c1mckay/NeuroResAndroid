package edu.sdsc.neurores.calendar.abstraction;

import java.util.Calendar;

/**
 * Created by trevor on 4/25/18.
 */

public interface EventCalendar {
    public CalendarBackedWeek getWeek(int position);
    public int getNumWeeksInCalendar();
    public long getStartTimeMillis();
    public int getWeekPosition(Week week);
}
