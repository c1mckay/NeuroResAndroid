package com.example.tbpetersen.myapplication;

import android.view.View;

/**
 * Created by tbpetersen on 3/2/2017.
 */

public class User {
    public long id;
    public View v;
    public String name;
    User(long id, String name){
        this.name = name;
        this.id = id;
    }

    User(long id, String name, View v){
        this.name = name;
        this.id = id;
        this.v = v;
    }
}
