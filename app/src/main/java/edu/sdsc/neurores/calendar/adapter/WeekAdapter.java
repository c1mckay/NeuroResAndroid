package edu.sdsc.neurores.calendar.adapter;

import android.content.Context;
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
import java.util.List;

import edu.sdsc.neurores.R;
import edu.sdsc.neurores.adapters.MessageAdapter;
import edu.sdsc.neurores.calendar.DayClickListener;
import edu.sdsc.neurores.calendar.abstraction.Day;
import edu.sdsc.neurores.calendar.abstraction.Week;

/**
 * Created by tbpetersen on 4/25/2018.
 */

// TODO Replace the week holder linear layout with a recycler view

public class WeekAdapter extends RecyclerView.Adapter<WeekAdapter.MyViewHolder>{
    private Context context;
    private Week week;
    private List<DayClickListener> dayClickListeners;
    private Day selectedDay;

    WeekAdapter(Context context, Week week, Day selectedDay){
        this.context = context;
        this.week = week;
        this.dayClickListeners = new ArrayList<>();
        this.selectedDay = selectedDay;
    }

    public void addDayClickListener(DayClickListener dayClickListener){
        dayClickListeners.add(dayClickListener);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView dayInMonthTextView, dayOfWeekTextView;
        ListView eventListView;
        View root;
        MyViewHolder(View itemView) {
            super(itemView);
            root = itemView;
            dayInMonthTextView = (TextView) itemView.findViewById(R.id.day_of_month_text_view);
            dayOfWeekTextView = (TextView) itemView.findViewById(R.id.day_of_week_text_view);
            eventListView = (ListView) itemView.findViewById(R.id.event_list_view);
        }

        public void setClickListener(View.OnClickListener onClickListener){
            itemView.setOnClickListener(onClickListener);
        }

        public View getRoot(){
            return itemView;
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.calendar_day, parent, false);

        ViewGroup.LayoutParams layoutParams = itemView.getLayoutParams();
        layoutParams.width = parent.getMeasuredWidth() / getItemCount();
        return new WeekAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final Day day = week.getDay(position);
        day.setView(holder.getRoot());

        holder.dayOfWeekTextView.setText(day.getDayOfWeek());
        holder.dayInMonthTextView.setText(String.valueOf(day.getDayInMonth()));

        BaseAdapter eventAdapter = new EventAdapter(context, day.getEvents());
        holder.eventListView.setAdapter(eventAdapter);

        holder.eventListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                View parent = (View) view.getParent().getParent().getParent().getParent();
                parent.callOnClick();
            }
        });

        holder.setClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(DayClickListener dayClickListener : dayClickListeners){
                    dayClickListener.onDayClicked(day);
                }
            }
        });

        if(selectedDay != null && selectedDay.equals(day)){
            day.select();
        }
    }

    @Override
    public int getItemCount() {
        return 7;
    }
}
