package edu.ucsd.neurores;

import android.content.Context;

import com.google.gson.annotations.SerializedName;

/**
 * Created by tbpetersen on 3/2/2017.
 */

public class User extends NavDrawerItem{
    String userType;
    private String name;
    private boolean isOnline;

    User(Context context, long id, String name, String userType, boolean isOnline){
        super(id, context);
        this.name = name;
        this.userType = userType;
        this.isOnline = isOnline;
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

    public String getName(){
        return name;
    }

    @Override
    public String toString(){
        return name;
    }



}
