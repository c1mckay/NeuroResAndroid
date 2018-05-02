package edu.sdsc.neurores.calendar.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import edu.sdsc.neurores.R;
import edu.sdsc.neurores.calendar.abstraction.Event;

/**
 * Created by trevor on 4/26/18.
 */

public class DetailedEventAdapter extends BaseAdapter {
    Context context;
    List<Event> events;

    public DetailedEventAdapter(Context context, List<Event> events){
        this.context = context;
        this.events = events;
    }
    @Override
    public int getCount() {
        return events.size();
    }

    @Override
    public Object getItem(int i) {
        return events.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View root = layoutInflater.inflate(R.layout.detailed_event_item, viewGroup, false);

        Event event = events.get(i);

        TextView eventTitleTextView = (TextView) root.findViewById(R.id.event_title);
        TextView eventTimeTextView = (TextView) root.findViewById(R.id.event_time);
        TextView eventLocationTextView = (TextView) root.findViewById(R.id.event_location);
        TextView eventDescTextView = (TextView) root.findViewById(R.id.event_desc);

        eventTitleTextView.setText(event.getTitle());
        eventTimeTextView.setText(event.getTimeRange(Event.LONG));
        eventLocationTextView.setText(event.getLocation());
        eventDescTextView.setText(event.getDescription());

        return root;
    }
}
