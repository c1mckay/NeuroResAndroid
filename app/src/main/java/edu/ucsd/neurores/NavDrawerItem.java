package edu.ucsd.neurores;

import android.content.Context;
import android.view.View;

/**
 * Created by tbpetersen on 3/14/2017.
 */

abstract class NavDrawerItem {
    private long id;
    private  Context c;
    View viewInNavDrawer;
    boolean isSelected;


    NavDrawerItem(long id, Context c){
        this.id = id;
        this.c = c;
        isSelected = false;
    }

    protected void setView(View v){
        this.viewInNavDrawer = v;
    }

    public long getID() {
        return id;
    }

    void select(){
        if(viewInNavDrawer != null){
            viewInNavDrawer.setBackgroundColor(c.getResources().getColor(R.color.selected));
        }
    }

    void deselect(){
        if(viewInNavDrawer != null){
            viewInNavDrawer.setBackgroundColor(c.getResources().getColor(R.color.colorPrimary));
        }
    }

    public void setContext(Context context) {
        this.c = context;
    }

}
