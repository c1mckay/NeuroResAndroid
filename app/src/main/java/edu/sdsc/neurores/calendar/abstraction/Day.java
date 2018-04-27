package edu.sdsc.neurores.calendar.abstraction;

import android.view.View;

import java.util.List;

/**
 * Created by trevor on 4/25/18.
 */

public abstract class Day {
    static final String[] days = new String[]{"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

    public abstract List<Event> getEvents();
    public abstract void addEvent(Event event);
    public abstract String getDayOfWeek();
    public abstract int getDayInMonth();
    public abstract int getMonth();
    public abstract int getYear();
    public abstract void deselect();
    public abstract void select();
    public abstract void setView(View view);

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Day)){
            return false;
        }
        Day day = (Day) obj;
        boolean sameYear = getYear() == day.getYear();
        boolean sameMonth = getMonth() == day.getMonth();
        boolean sameDay = getDayInMonth() == day.getDayInMonth();

        return sameDay && sameMonth && sameYear;
    }
}
