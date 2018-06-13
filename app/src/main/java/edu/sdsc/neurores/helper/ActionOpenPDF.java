package edu.sdsc.neurores.helper;

import android.content.DialogInterface;
import android.view.View;

import edu.sdsc.neurores.activities.MainActivity;

/**
 * Created by tbpetersen on 6/8/2018.
 */

public class ActionOpenPDF implements View.OnClickListener{
    private MainActivity mainActivity;
    private String filename;

    public ActionOpenPDF(MainActivity mainActivity, String filename){
        this.mainActivity = mainActivity;
        this.filename = filename;
    }

    @Override
    public void onClick(View v) {
        mainActivity.viewPDF(filename);
    }
}
