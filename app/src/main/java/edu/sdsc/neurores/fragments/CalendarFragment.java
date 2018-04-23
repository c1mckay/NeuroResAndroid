package edu.sdsc.neurores.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.Calendar;
import edu.sdsc.neurores.R;
import edu.sdsc.neurores.calendar.CalendarController;

/**
 * Created by tbpetersen on 4/13/2018.
 */


public class CalendarFragment extends Fragment {
    ViewPager viewPager;
    CalendarController calendarController;

    public CalendarFragment() {
        // required empty constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.calendar_root, container, false);

        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        end.add(Calendar.YEAR, 2);

        calendarController = new CalendarController(v.getContext(), start, end);

        viewPager = (ViewPager) v.findViewById(R.id.calendar);
        viewPager.setAdapter(calendarController.getPagerAdapter());
        viewPager.addOnPageChangeListener(calendarController.getOnPageChangeListener());

        alignLeftToolbarTitle();
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        calendarController.getOnPageChangeListener().onPageSelected(0);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        Log.v("taggy", "here it is");
        MenuItem item = menu.findItem(R.id.action_wipe_thread);
        item.setEnabled(false);
        item.setVisible(false);
    }


    private void alignLeftToolbarTitle() {
        TextView toolbarTitle = (TextView) getActivity().findViewById(R.id.toolbar_title);
        Toolbar.LayoutParams params =  (Toolbar.LayoutParams)toolbarTitle.getLayoutParams();
        params.gravity = Gravity.START;
        toolbarTitle.setLayoutParams(params);
    }
}
