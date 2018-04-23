package edu.sdsc.neurores.helper;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.HorizontalCalendarView;

/**
 * Created by tbpetersen on 4/16/2018.
 */

public class MyCal extends HorizontalCalendarView {
    Context context;
    OnSwipeTouchListener onSwipeTouchListener;

    public MyCal(Context context) {
        super(context);
        init(context);
    }

    public MyCal(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context){
        this.context = context;
        onSwipeTouchListener = new OnSwipeTouchListener(context) {
            @Override
            public void onSwipeRight() {
                scrollToPosition(getPositionOfCenterItem() - 7);
            }

            @Override
            public void onSwipeLeft() {
                scrollToPosition(getPositionOfCenterItem() + 7);
            }

            @Override
            public void onSwipeTop() {

            }

            @Override
            public void onSwipeBottom() {

            }
        };

    }


    @Override
    public boolean onTouchEvent(MotionEvent e) {
        //return super.onTouchEvent(e);
        return true;
    }
}
