package edu.sdsc.neurores.calendar.abstraction;

/**
 * Created by trevor on 4/25/18.
 */

public interface Week {
    Day getDay(int position);
    String getMonthName();
    int getYear();
    long getStartOfWeek();
    int getNumWeekInYear();
    boolean isWithinWeek(Day day);
}
