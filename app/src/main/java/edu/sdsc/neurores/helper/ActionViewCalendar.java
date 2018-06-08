package edu.sdsc.neurores.helper;

import edu.sdsc.neurores.activities.MainActivity;

/**
 * Created by tbpetersen on 6/8/2018.
 */

public class ActionViewCalendar extends NavigationDrawerLinkAction {

    public ActionViewCalendar(MainActivity mainActivity){
        super(mainActivity);
    }
    @Override
    public void act() {
        mainActivity.viewCalendar(null);
    }
}
