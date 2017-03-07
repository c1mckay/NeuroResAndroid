package com.example.tbpetersen.myapplication;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by tbpetersen on 2/23/2017.
 */

public class NavDrawerAdapter extends BaseExpandableListAdapter {
    private static final int NUM_OF_SUBMENUS = 3;

    List<List<String>> submenus;

    List<String> unreadMenu;
    List<String> privateMenu;
    List<String> staffMenu;

    String[] groupTitles;

    private MainActivity activity;
    public View previouslySelected;

    NavDrawerAdapter(MainActivity activity){
        this.activity = activity;
        unreadMenu = new ArrayList<String>();
        privateMenu = new ArrayList<String>();
        staffMenu = new ArrayList<String>();

        groupTitles = activity.getResources().getStringArray(R.array.sub_menus);
        submenus = new ArrayList<List<String>>();

        submenus.add(unreadMenu);
        submenus.add(privateMenu);
        submenus.add(staffMenu);
    }

    @Override
    public int getGroupCount() {
        return NUM_OF_SUBMENUS;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return submenus.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return submenus.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return submenus.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View group = inflater.inflate(R.layout.custom_nav_header, parent, false);
            group = inflater.inflate(android.R.layout.simple_expandable_list_item_1, parent, false);
            TextView textView = (TextView) group.findViewById(android.R.id.text1);
            textView.setText(groupTitles[groupPosition]);
            textView.setTextColor(activity.getResources().getColor(R.color.white));
            textView.setTextSize(activity.getResources().getDimension(R.dimen.nav_drawer_group_text_size));
            return group;
        }else{
            return convertView;
        }
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        View child = null;

        if(previouslySelected != null && convertView == null){
            previouslySelected.setBackgroundColor(activity.getResources().getColor(R.color.colorPrimary));
        }

        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            child = inflater.inflate(R.layout.custom_nav_drawer_row, parent, false);

            TextView rowTextView = (TextView) child.findViewById(R.id.nav_row__text_view);
            ImageView statusImageView = (ImageView) child.findViewById(R.id.nav_row_status_image_view);
            ImageView notifyImageView = (ImageView) child.findViewById(R.id.nav_row_notification_image_view);

            String name = (String) getChild(groupPosition,childPosition);
            rowTextView.setText(name);
            notifyImageView.setImageResource(R.drawable.notify);
            statusImageView.setImageResource(R.drawable.online);

            activity.selectedUser.v = child;

        }else {
            child = convertView;
        }

        if(convertView == null){
            child.setBackgroundColor(activity.getResources().getColor(R.color.selected));
            previouslySelected = child;
        }


        return child;

    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true ;
    }

    public void addConversation(int groupPosition, User newUser){
        List group = submenus.get(groupPosition);
        group.add(newUser.name);
        notifyDataSetChanged();
    }

}
