package edu.sdsc.neurores.calendar.abstraction;

import android.view.View;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import edu.sdsc.neurores.R;

/**
 * Created by trevor on 4/25/18.
 */

public class CalendarBackedDay extends Day {
    private Calendar calendar;
    private List<Event> events;
    View view;

    public CalendarBackedDay(Calendar calendar){
        this.calendar = calendar;
        events = new ArrayList<>();
        view = null;

        Random random = new Random();
        for(int i = 0; i < random.nextInt(5); i++){
            events.add(new Event("Meeting", "11a-12p", "Room Num", "This is an important meeting"));
        }
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

    @Override
    public int getMonth() {
        return calendar.get(Calendar.MONTH);
    }

    @Override
    public int getYear() {
        return calendar.get(Calendar.YEAR);
    }

    @Override
    public void deselect() {
        if(view != null){
            View backgroundHolder = view.findViewById(R.id.day_background_holder);
            backgroundHolder.setBackgroundDrawable(view.getContext().getResources().getDrawable(R.drawable.calendar_day_unselected));
        }
    }

    @Override
    public void select() {
        if(view != null){
            View backgroundHolder = view.findViewById(R.id.day_background_holder);
            backgroundHolder.setBackgroundDrawable(view.getContext().getResources().getDrawable(R.drawable.calendar_day_selected));
        }
    }

    @Override
    public void setView(View view) {
        this.view = view;
    }
}
