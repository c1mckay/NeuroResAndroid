package edu.sdsc.neurores.abstraction;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tbpetersen on 8/22/2017.
 */

public class Group <T>{
    private List<T> group;
    private int id;
    private boolean isExpanded;

    public Group(int id){
        this.id = id;
        group = new ArrayList<T>();
        isExpanded = true;
    }

    public boolean isVisible(){
        return group.size() > 0;
    }

    public T getItem(int position){
        return group.get(position);
    }

    public void addItem(T item){
        group.add(item);
    }

    public void addItem(int position, T item){
        group.add(position, item);
    }

    public int getID(){
        return id;
    }

    public int size(){
        return group.size();
    }

    public void moveItemToFirstPosition(T item){
        if(contains(item)){
            group.remove(item);
            group.add(0,item);
        }
    }

    public boolean contains(T item){
        return group.contains(item);
    }

    public void removeItem(T item){
        group.remove(item);
    }

    public void removeItem(int position){
        group.remove(position);
    }

    public void setIsExpanded(boolean isExpanded){
        this.isExpanded = isExpanded;
    }

    public boolean isExpanded(){
        return isExpanded;
    }

    @Override
    public String toString(){
        String ret = "";
        for(T item : group){
            ret += item.toString() + "\n";
        }
        return ret;
    }
}
