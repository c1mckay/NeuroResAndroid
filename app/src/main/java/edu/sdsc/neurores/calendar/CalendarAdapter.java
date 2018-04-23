package edu.sdsc.neurores.calendar;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import edu.sdsc.neurores.R;


/**
 * Created by trevor on 4/21/18.
 */

public class CalendarAdapter extends PagerAdapter {
    private static final String[] days = new String[]{"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    private Context context;
    private Calendar start, end;
    private View.OnClickListener onClickListener;

    CalendarAdapter(Context context, Calendar start, Calendar end, View.OnClickListener onItemClickListener){
        this.context = context;
        this.start = start;
        this.end = end;

        this.onClickListener = onItemClickListener;
    }

    @Override
    public int getCount() {
        return (int) ((end.getTimeInMillis() - start.getTimeInMillis()) / (1000 * 60 * 60 * 24 * 7));
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Calendar current = getWeek(position);

        LinearLayout weekHolder = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.calendar_week, container, false);

        for(int i = 0; i < 7; i++){
            Calendar c = (Calendar) current.clone();
            c.add(Calendar.DAY_OF_MONTH,1 * i);
            addDayView(c, weekHolder);
        }

        container.addView(weekHolder);
        return weekHolder;
    }



    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == (View) object;
    }

    protected Calendar getWeek(int position){
        Calendar current = Calendar.getInstance();
        current.setTimeInMillis((long)((1000L * 60L * 60L * 24L * 7L) * position) + start.getTimeInMillis());
        return current;
    }

    private String intDayToString(int day){
        return days[day - 1].substring(0,3);
    }

    private void addDayView(Calendar calendar, ViewGroup parent){

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

        root.setOnClickListener(onClickListener);

        parent.addView(root);
    }

    private List<Event> getEvents(Calendar calendar) {
        List<Event> events = new ArrayList<>();

        Random random = new Random();
        for(int i = 0; i < random.nextInt(5); i++){
            events.add(new Event("Meeting", "11a-12p", "Room Num", "This is an important meeting"));
        }


        return events;
    }
}
