package com.example.tbpetersen.myapplication;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tbpetersen on 3/14/2017.
 */

public class NavDrawerInnerGroup extends NavDrawerItem {
    private Context context;
    private List<User> children;

    NavDrawerInnerGroup(Context context, String name){
        this.name = name;
        this.context = context;

        children = new ArrayList<User>();
    }

    public void addChild(User newChild){
        children.add(newChild);
    }

    public User getChild(int childIndex){
        return children.get(childIndex);
    }

    public int numOfChildren(){
        return children.size();
    }

    public List<User> getChildren(){
        return children;
    }
}
