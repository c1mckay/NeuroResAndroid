package edu.ucsd.neurores;

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

    NavDrawerInnerGroup(Context context, String name){
        super(null, context);
        this.name = name;
        this.context = context;

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
}
