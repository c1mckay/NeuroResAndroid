package edu.sdsc.neurores.calendar;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.List;

import edu.sdsc.neurores.calendar.abstraction.Event;
import edu.sdsc.neurores.calendar.adapter.CalendarAdapter;

/**
 * Created by trevor on 4/21/18.
 */

public class CalendarClickListener implements ListView.OnItemClickListener, View.OnClickListener {
    CalendarAdapter calendarAdapter;


    public void setCalendarAdapter(CalendarAdapter calendarAdapter){
        this.calendarAdapter = calendarAdapter;
    }
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        /*
        View root = view.getRootView();
        ViewGroup weekGroup = (ViewGroup)root.findViewById(R.id.week_holder);

        Log.v("taggy", weekGroup.getChildCount() + "");
        for(int i = 0; i < weekGroup.getChildCount(); i++){
            View child = weekGroup.getChildAt(i).findViewById(R.id.day_background_holder);
            child.setBackgroundDrawable(view.getContext().getResources().getDrawable(R.drawable.calendar_day_unselected));
        }

        View clickedDay = (View)view.getParent().getParent().getParent();
        clickedDay.setBackgroundDrawable(view.getContext().getResources().getDrawable(R.drawable.calendar_day_selected));

        EventAdapter eventAdapter = (EventAdapter) adapterView.getAdapter();
        Event event = (Event) eventAdapter.getItem(position);

        // Get title and desc and add data
        TextView title = (TextView) root.findViewById(R.id.event_title);
        TextView time = (TextView) root.findViewById(R.id.event_time);
        TextView location = (TextView) root.findViewById(R.id.event_location);
        TextView desc = (TextView) root.findViewById(R.id.event_desc);


        title.setText(event.getTitle());
        time.setText((event.getTimeRange()));
        location.setText(event.getLocation());
        desc.setText(event.getDescription());

        Log.v("taggy", event.getTitle());
        */
        onClick((View)view.getParent().getParent().getParent());
    }

    @Override
    public void onClick(View view) {
        //TODO Set up recycler view for days of week and set up recycler view for event details
        Log.v("taggy", "Click");
//
//        LinearLayout weekHolder = (LinearLayout) view.getParent();
//
//        for(int i = 0; i < weekHolder.getChildCount(); i++){
//            if(view.equals(weekHolder.getChildAt(i))){
//                List<Event> events = calendarAdapter.getEventsForDay(i);
//                for(Event event : events){
//                    Log.v("taggy", event.getTitle());
//                }
//            }
//        }
    }
}
