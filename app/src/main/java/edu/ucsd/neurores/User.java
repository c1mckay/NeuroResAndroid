package edu.ucsd.neurores;

import android.content.Context;
import android.view.View;

/**
 * Created by tbpetersen on 3/2/2017.
 */

public class User extends NavDrawerItem{
    String userType;
    String name;
    private boolean isOnline;
    User(Context context, long id, String name){
        super(id, context);
        this.name = name;
        isOnline = false;
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
    public boolean isOnline(){
        return isOnline;
    }

    public void setIsOnline(boolean isOnline){
        this.isOnline = isOnline;
    }

    @Override
    public String toString(){
        return name;
    }



}
