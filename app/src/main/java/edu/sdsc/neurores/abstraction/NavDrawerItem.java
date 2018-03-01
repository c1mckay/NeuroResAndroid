package edu.sdsc.neurores.abstraction;

import android.content.Context;
import android.view.View;

import edu.sdsc.neurores.R;

/**
 * Created by tbpetersen on 3/14/2017.
 */

public abstract class NavDrawerItem {
    private long id;
    private  Context c;
    View viewInNavDrawer;
    boolean isSelected;


    public NavDrawerItem(long id, Context c){
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

    public void select(){
        if(viewInNavDrawer != null){
            viewInNavDrawer.setBackgroundColor(c.getResources().getColor(R.color.selected));
        }
    }

    public void deselect(){
        if(viewInNavDrawer != null){
            viewInNavDrawer.setBackgroundColor(c.getResources().getColor(R.color.colorPrimary));
        }
    }

    public void setContext(Context context) {
        this.c = context;
    }

    public View getViewInNavDrawer(){
        return viewInNavDrawer;
    }

    public void setViewInNavDrawer(View v){
        viewInNavDrawer = v;
    }

}
