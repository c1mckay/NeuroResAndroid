package edu.ucsd.neurores;

import android.content.Context;
import android.util.Log;
import android.view.View;

/**
 * Created by tbpetersen on 3/14/2017.
 */

abstract class NavDrawerItem {
    private Long id;
    private Context c;
    View v;


    NavDrawerItem(Long id, Context c){
        this.id = id;
        this.c = c;
    }

    protected void setView(View v){
        this.v = v;
    }

    public Long getID() {
        return id;
    }

    void select(){
        if(v != null){
            v.setBackgroundColor(c.getResources().getColor(R.color.selected));
        }
    }

    void deselect(){
        if(v != null){
            v.setBackgroundColor(c.getResources().getColor(R.color.colorPrimary));
        }
    }

    public void setContext(Context context) {
        this.c = context;
    }

}
