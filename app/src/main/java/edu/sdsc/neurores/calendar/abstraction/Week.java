package edu.sdsc.neurores.calendar.abstraction;

/**
 * Created by trevor on 4/25/18.
 */

public interface Week {
    public Day getDay(int position);
    public String getMonthName();
    public int getYear();
}
