package edu.sdsc.neurores.calendar.abstraction;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by trevor on 4/25/18.
 */

public class CalendarBackedDay implements Day {
    private Calendar calendar;
    private List<Event> events;

    CalendarBackedDay(Calendar calendar){
        this.calendar = calendar;
        events = new ArrayList<>();
    }

    @Override
    public List<Event> getEvents() {
        return events;
    }

    @Override
    public void addEvent(Event event) {
        events.add(event);
    }

    @Override
    public String getDayOfWeek() {
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        return days[day - 1].substring(0,3);
    }

    @Override
    public int getDayInMonth() {
        return calendar.get(Calendar.DAY_OF_MONTH);
    }
}
