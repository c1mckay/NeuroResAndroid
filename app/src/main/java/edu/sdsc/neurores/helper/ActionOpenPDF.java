package edu.sdsc.neurores.helper;

import edu.sdsc.neurores.activities.MainActivity;

/**
 * Created by tbpetersen on 6/8/2018.
 */

public class ActionOpenPDF extends NavigationDrawerLinkAction {
    String filename;

    public ActionOpenPDF(MainActivity mainActivity, String filename){
        super(mainActivity);
        this.filename = filename;
    }
    @Override
    public void act() {
        mainActivity.viewPDF(filename);
    }
}
