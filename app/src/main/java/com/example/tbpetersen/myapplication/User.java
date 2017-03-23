package com.example.tbpetersen.myapplication;

import android.content.Context;
import android.view.View;

/**
 * Created by tbpetersen on 3/2/2017.
 */

public class User extends NavDrawerItem{
    public long id;
    public String userType;
    public View v;

    private Context context;
    User(Context context, long id, String name){
        this.context = context;
        this.name = name;
        this.id = id;
    }

    User(Context context, long id, String name, String userType){
        this.context = context;
        this.name = name;
        this.id = id;
        this.userType = userType;
    }

    User(Context context, long id, String name, View v){
        this.context = context;
        this.name = name;
        this.id = id;
        this.v = v;
    }

    @Override
    public String toString(){
        return name;
    }

    public void select(){
        if(v != null){
            v.setBackgroundColor(context.getResources().getColor(R.color.selected));
        }
    }

    public void deselect(){
        if(v != null){
            v.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
        }
    }

}
