package edu.sdsc.neurores.calendar;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Calendar;

import edu.sdsc.neurores.R;

/**
 * Created by tbpetersen on 4/25/2018.
 */

// TODO Replace the week holder linear layout with a recycler view

public class WeekAdapter extends RecyclerView.Adapter<WeekAdapter.MyViewHolder>{

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public MyViewHolder(View itemView) {
            super(itemView);

        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 7;
    }
}
