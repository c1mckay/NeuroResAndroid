package edu.sdsc.neurores.calendar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import edu.sdsc.neurores.R;
import edu.sdsc.neurores.calendar.abstraction.Event;

/**
 * Created by trevor on 4/21/18.
 */

public class EventAdapter extends BaseAdapter {
    Context context;
    List<Event> events;

    public EventAdapter(Context context, List<Event> events){
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
        Event event = events.get(i);

        View root = LayoutInflater.from(context).inflate(R.layout.item_event, viewGroup, false);

        TextView title = (TextView) root.findViewById(R.id.title);
        TextView time = (TextView) root.findViewById(R.id.time);

        title.setText(event.getTitle());
        time.setText(event.getTimeRange());

        return root;
    }
}
