package edu.sdsc.neurores.helper;

import edu.sdsc.neurores.activities.MainActivity;

/**
 * Created by tbpetersen on 6/8/2018.
 */

public abstract class NavigationDrawerLinkAction {
    MainActivity mainActivity;

    NavigationDrawerLinkAction(MainActivity mainActivity){
        this.mainActivity = mainActivity;
    }
    abstract public void act();
}
