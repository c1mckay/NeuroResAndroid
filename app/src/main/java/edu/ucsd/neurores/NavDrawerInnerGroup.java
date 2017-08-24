package edu.ucsd.neurores;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tbpetersen on 3/14/2017.
 */

public class NavDrawerInnerGroup extends NavDrawerItem {
    private Context context;
    private List<User> children;
    private String name;
    private boolean isExpanded;

    NavDrawerInnerGroup(Context context, String name){
        super(null, context);
        this.name = name;
        this.context = context;
        this.isExpanded = false;
        children = new ArrayList<>();
    }

    void addChild(User newChild){
        children.add(newChild);
    }

    public User getChild(int childIndex){
        return children.get(childIndex);
    }

    public int numOfChildren(){
        return children.size();
    }

    List<User> getChildren(){
        return children;
    }

    public String getName(){
        return name;
    }

    public void setIsExpanded(boolean expanded){
        isExpanded = expanded;
    }

    public boolean getIsExpanded(){
        return isExpanded;
    }

    @Override
    public String toString(){
        return getName();
    }
}
