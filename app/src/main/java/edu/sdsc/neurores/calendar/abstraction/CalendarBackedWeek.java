package edu.sdsc.neurores.calendar.abstraction;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by trevor on 4/25/18.
 */

public class CalendarBackedWeek implements Week {
    private Calendar calendar;
    private Day[] days;
    private List<Event> events;

    public CalendarBackedWeek(Calendar calendar, List<Event> events){
        this.events = events;

        if(events == null){
            events = new ArrayList<>();
        }
        this.calendar = (Calendar) calendar.clone();

        this.calendar.set(Calendar.DAY_OF_WEEK, 0);

        this.calendar.set(Calendar.HOUR_OF_DAY, 0);
        this.calendar.clear(Calendar.MINUTE);
        this.calendar.clear(Calendar.SECOND);
        this.calendar.clear(Calendar.MILLISECOND);

        this.calendar.set(Calendar.DAY_OF_WEEK, this.calendar.getFirstDayOfWeek());

        days = new Day[7];
        for(int i = 0; i < 7; i++){
            Calendar temp = (Calendar) calendar.clone();
            temp.add(Calendar.DAY_OF_MONTH, i);

            List<Event> eventsForDay = new ArrayList<>();
            for(Event event : events){
                Calendar calForEvent = Calendar.getInstance();
                calForEvent.setTime(event.getDate());

                boolean sameYear = temp.get(Calendar.YEAR) == calForEvent.get(Calendar.YEAR);
                boolean sameDay = temp.get(Calendar.DAY_OF_YEAR) == calForEvent.get(Calendar.DAY_OF_YEAR);

                if(sameDay && sameYear){
                    eventsForDay.add(event);
                }
            }
            days[i] =  new CalendarBackedDay(temp, eventsForDay);
        }
    }

    private ArrayList<Event> filterEventsByDay(ArrayList<Event> events, int day){
        ArrayList<Event> ret = new ArrayList<>();
        for(Event e: events){
            if(e.isDayOfWeek(day))
                ret.add(e);
        }

        return ret;
    }

    @Override
    public Day getDay(int position) {
        return days[position];
    }

    @Override
    public String getMonthName() {
        return intMonthToString(calendar.get(Calendar.MONTH));
    }

    @Override
    public int getYear() {
        return calendar.get(Calendar.YEAR);
    }

    @Override
    public long getStartOfWeek() {
        return calendar.getTimeInMillis();
    }

    @Override
    public int getNumWeekInYear() {
        return calendar.get(Calendar.WEEK_OF_YEAR);
    }

    private String intMonthToString(int month){
        return new DateFormatSymbols().getMonths()[month];
    }
}
