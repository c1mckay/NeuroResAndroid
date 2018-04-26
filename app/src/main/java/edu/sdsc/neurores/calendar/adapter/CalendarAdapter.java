package edu.sdsc.neurores.calendar.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import edu.sdsc.neurores.R;
import edu.sdsc.neurores.calendar.DayClickListener;
import edu.sdsc.neurores.calendar.abstraction.CalendarBackedEventCalendar;
import edu.sdsc.neurores.calendar.abstraction.Event;
import edu.sdsc.neurores.calendar.abstraction.EventCalendar;
import edu.sdsc.neurores.calendar.abstraction.Week;


/**
 * Created by trevor on 4/21/18.
 */

public class CalendarAdapter extends PagerAdapter {
    private static final String[] days = new String[]{"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    private Context context;
    private EventCalendar eventCalendar;
    private DayClickListener dayClickListener;
    private RecyclerView weekHolder;
    List<Calendar> daysOfWeek;
    List<List<Event>> eventsList;

    public CalendarAdapter(Context context, Calendar start, Calendar end, DayClickListener dayClickListener){
        this.context = context;
        eventCalendar = new CalendarBackedEventCalendar(start,end);

        this.dayClickListener =dayClickListener;
        daysOfWeek = new ArrayList<>();
        eventsList = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return eventCalendar.getNumWeeksInCalendar();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Week current = getWeek(position);
        weekHolder = (RecyclerView) LayoutInflater.from(context).inflate(R.layout.calendar_week, container, false);
        WeekAdapter weekAdapter = new WeekAdapter(context, current, dayClickListener);
        weekHolder.setAdapter(weekAdapter);
        container.addView(weekHolder);
        return weekHolder;

        //TODO Replace with recycler view and replace logic
//        LinearLayout weekHolder = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.calendar_week, container, false);
//
//        for(int i = 0; i < 7; i++){
//            Calendar c = (Calendar) current.clone();
//            c.add(Calendar.DAY_OF_MONTH,1 * i);
//            addDayView(c, weekHolder);
//        }
//
//        container.addView(weekHolder);
//        return weekHolder;
    }



    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == (View) object;
    }

    protected Week getWeek(int position){
        return eventCalendar.getWeek(position);
    }

    private String intDayToString(int day){
        return days[day - 1].substring(0,3);
    }

    private void addDayView(Calendar calendar, ViewGroup parent){
        daysOfWeek.add(calendar);
        String dayOfWeek = intDayToString(calendar.get(Calendar.DAY_OF_WEEK));
        String dayOfMonth = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));

        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View root = layoutInflater.inflate(R.layout.calendar_day, parent, false);

        TextView dayOfWeekTextView = (TextView) root.findViewById(R.id.day_of_week_text_view);
        TextView dayOfMonthTextView = (TextView) root.findViewById(R.id.day_of_month_text_view);

        dayOfWeekTextView.setText(dayOfWeek);
        dayOfMonthTextView.setText(dayOfMonth);


        ListView eventListView = (ListView) root.findViewById(R.id.event_list_view);
        BaseAdapter eventAdapter = new EventAdapter(context, getEvents(calendar));
        eventListView.setAdapter(eventAdapter);
        //eventListView.setOnItemClickListener(onItemClickListener);

        //root.setOnClickListener(onClickListener);

        parent.addView(root);
    }

    private List<Event> getEvents(Calendar calendar) {
        List<Event> events = new ArrayList<>();

        Random random = new Random();
        for(int i = 0; i < random.nextInt(5); i++){
            events.add(new Event("Meeting", "11a-12p", "Room Num", "This is an important meeting"));
        }


        eventsList.add(events);
        return events;
    }

    public List<Event> getEventsForDay(int dayPosition){
        return eventsList.get(dayPosition);
    }
}
