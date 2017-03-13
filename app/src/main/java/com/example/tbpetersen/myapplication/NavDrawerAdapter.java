package com.example.tbpetersen.myapplication;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.graphics.Paint;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by tbpetersen on 2/23/2017.
 */

public class NavDrawerAdapter extends BaseExpandableListAdapter {
    List<List<String>> submenus;

    List<String> unreadMenu;
    List<String> privateMenu;
    List<String> staffMenu;

    String[] groupTitles;
    String[] departmentTitles;

    private MainActivity activity;
    public View previouslySelected;

    NavDrawerAdapter(MainActivity activity){
        this.activity = activity;
        unreadMenu = new ArrayList<String>();
        privateMenu = new ArrayList<String>();
        staffMenu = new ArrayList<String>();

        groupTitles = activity.getResources().getStringArray(R.array.sub_menus);
        departmentTitles = activity.getResources().getStringArray(R.array.departments);

        submenus = new ArrayList<List<String>>();

        submenus.add(unreadMenu);
        submenus.add(privateMenu);
        submenus.add(staffMenu);
    }

    @Override
    public int getGroupCount() {
        return submenus.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if(groupPosition == 1){
            return departmentTitles.length;
        }
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
            View group = inflater.inflate(R.layout.menu_header, parent, false);
            TextView textView = (TextView) group.findViewById(android.R.id.text1);
            textView.setText(groupTitles[groupPosition]);
            //textView.setTextSize(activity.getResources().getDimension(R.dimen.nav_drawer_group_text_size)); //aka the header
            textView.setPaintFlags(textView.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
            return group;
        }else{
            return convertView;
        }
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (groupPosition == 1) {

            if(convertView == null || convertView.getTag() instanceof Long){

                LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                LinearLayout outer = (LinearLayout) inflater.inflate(R.layout.inner_list, parent, false);
                TextView departmentTextView = (TextView) outer.findViewById(R.id.department_text_view);
                departmentTextView.setText(departmentTitles[childPosition]);

                LinearLayout inner = (LinearLayout) outer.findViewById(R.id.inner_layout);


                LinearLayout child1 = (LinearLayout) inflater.inflate(R.layout.custom_nav_drawer_row, inner, false);
                LinearLayout child2 = (LinearLayout) inflater.inflate(R.layout.custom_nav_drawer_row, inner, false);

                TextView firstChild = (TextView) child1.findViewById(R.id.nav_row_text_view);
                TextView secondChild = (TextView) child2.findViewById(R.id.nav_row_text_view);

                firstChild.setText("Trevor Petersen");
                secondChild.setText("Charles McKay");

                child1.setTag(123L);
                child2.setTag(456L);

                inner.addView(child1);
                inner.addView(child2);

                User first = new User(123, firstChild.getText().toString(), child1);
                User second = new User(456, secondChild.getText().toString(), child2);

                activity.addUserToHashTable(first);
                activity.addUserToHashTable(second);

                outer.setTag("Department");
                return outer;
            }else{
                return convertView;
            }
        }else{
            if(convertView == null || convertView.getTag() instanceof String){
                LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                LinearLayout user = (LinearLayout) inflater.inflate(R.layout.custom_nav_drawer_row, parent, false);
                TextView userNameTextView = (TextView) user.findViewById(R.id.nav_row_text_view);
                userNameTextView.setText(activity.selectedUser.name);
                user.setTag(activity.selectedUser.id);
                user.setBackgroundColor(activity.getResources().getColor(R.color.selected));
                activity.selectedUser.v = user;
                return user;
            }else{
                return convertView;
            }

        }
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
