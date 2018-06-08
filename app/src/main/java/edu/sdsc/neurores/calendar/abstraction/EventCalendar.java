package edu.sdsc.neurores.calendar.abstraction;

import java.util.Calendar;
import java.util.List;

import edu.sdsc.neurores.calendar.DayClickListener;

/**
 * Created by trevor on 4/25/18.
 */

public abstract class EventCalendar implements DayClickListener{
    public abstract CalendarBackedWeek getWeek(int position);
    public abstract int getNumWeeksInCalendar();
    public abstract long getStartTimeMillis();
    public abstract int getWeekPosition(Week week);
    public abstract void setSelectedDay(Day day);
    public abstract Day getSelectedDay();
    public abstract void setEvents(List<Event> events);

    @Override
    public void onDayClicked(Day day) {
        if(getSelectedDay() != null){
            getSelectedDay().deselect();
        }
        day.select();
        setSelectedDay(day);
    }
}
