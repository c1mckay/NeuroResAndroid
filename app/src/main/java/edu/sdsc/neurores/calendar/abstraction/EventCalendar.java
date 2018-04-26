package edu.sdsc.neurores.calendar.abstraction;

/**
 * Created by trevor on 4/25/18.
 */

public interface EventCalendar {
    public CalendarBackedWeek getWeek(int position);
    public int getNumWeeksInCalendar();
}
