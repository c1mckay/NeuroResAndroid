package com.example.tbpetersen.myapplication;

import android.content.Context;
import android.view.View;

/**
 * Created by tbpetersen on 3/2/2017.
 */

public class User extends NavDrawerItem{
    String userType;
    String name;
    User(Context context, long id, String name){
        super(id, context);
        this.name = name;
    }

    User(Context context, long id, String name, String userType){
        super(id, context);
        this.name = name;
        this.userType = userType;
    }

    User(Context context, long id, String name, View v){
        super(id, context);
        setView(v);
        this.name = name;
    }

    User(long id, String name, String userType){
        super(id, null);
        this.name = name;
        this.userType = userType;
    }

    @Override
    public String toString(){
        return name;
    }



}
