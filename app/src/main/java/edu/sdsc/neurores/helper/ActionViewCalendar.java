package edu.sdsc.neurores.helper;

import android.view.View;

import edu.sdsc.neurores.activities.MainActivity;

/**
 * Created by tbpetersen on 6/8/2018.
 */

public class ActionViewCalendar implements View.OnClickListener {
    private MainActivity mainActivity;

    public ActionViewCalendar(MainActivity mainActivity){
        this.mainActivity = mainActivity;
    }

    @Override
    public void onClick(View v) {
        mainActivity.viewCalendar(null);
    }
}
