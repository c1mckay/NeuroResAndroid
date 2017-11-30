package edu.ucsd.neurores.abstraction;

import android.content.Context;

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

    public NavDrawerInnerGroup(Context context, String name){
        super(0, context);
        this.name = name;
        this.context = context;
        this.isExpanded = false;
        children = new ArrayList<>();
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
