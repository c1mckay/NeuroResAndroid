package edu.sdsc.neurores.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.barteksc.pdfviewer.PDFView;

import java.util.Calendar;

import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.HorizontalCalendarView;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener;
import edu.sdsc.neurores.R;
import edu.sdsc.neurores.helper.OnSwipeTouchListener;

/**
 * Created by tbpetersen on 4/13/2018.
 */


public class CalendarFragment extends Fragment {

    public CalendarFragment() {
        // required empty constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_calendar, container, false);


        /* starts before 1 month from now */
        Calendar startDate = Calendar.getInstance();
        startDate.add(Calendar.MONTH, -100);

        /* ends after 1 month from now */
        Calendar endDate = Calendar.getInstance();
        endDate.add(Calendar.MONTH, 100);

        final HorizontalCalendar horizontalCalendar = new HorizontalCalendar.Builder(v, R.id.calendar_view)
                .range(startDate,endDate)
                .datesNumberOnScreen(7)
                .build();

        horizontalCalendar.setCalendarListener(new HorizontalCalendarListener() {

            @Override
            public void onCalendarScroll(HorizontalCalendarView calendarView, int dx, int dy) {

            }

            @Override
            public void onDateSelected(Calendar date, int position) {

            }
        });

        Log.v("taggy", horizontalCalendar.positionOfDate(Calendar.getInstance()) + "");
        View calView = v.findViewById(R.id.calendar_view);


        calView.setOnTouchListener(new OnSwipeTouchListener(getContext()) {
            @Override
            public void onSwipeRight() {
                Log.v("taggy", "Right");
                horizontalCalendar.centerCalendarToPosition(horizontalCalendar.getSelectedDatePosition() - 7);
            }

            @Override
            public void onSwipeLeft() {
                Log.v("taggy", "Left");
                horizontalCalendar.centerCalendarToPosition(horizontalCalendar.getSelectedDatePosition() + 7);

            }

            @Override
            public void onSwipeTop() {
                Log.v("taggy", "Top");

            }

            @Override
            public void onSwipeBottom() {
                Log.v("taggy", "Bot");

            }
        });


        setHasOptionsMenu(true);
        alignLeftToolbarTitle();
        return v;
    }


    @Override
    public void onPrepareOptionsMenu(Menu menu) {
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
