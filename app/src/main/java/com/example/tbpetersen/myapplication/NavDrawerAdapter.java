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
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Created by tbpetersen on 2/23/2017.
 */

public class NavDrawerAdapter extends BaseExpandableListAdapter {

    private static final int UNREAD_GROUP = 0;
    private static final int STAFF_GROUP = 1;
    private static final int PRIVATE_GROUP = 2;

    private static final int NUM_OF_SUBMENUS = 3;


    private List<NavDrawerItem> unreadMenu;
    private List<NavDrawerItem> privateMenu;
    private List<NavDrawerInnerGroup> staffMenu;

    private MainActivity activity;

    NavDrawerAdapter(MainActivity activity){

        this.activity = activity;
        unreadMenu = new ArrayList<NavDrawerItem>();
        privateMenu = new ArrayList<NavDrawerItem>();
        staffMenu = new ArrayList<NavDrawerInnerGroup>();

    }

    @Override
    public int getGroupCount() {
        return NUM_OF_SUBMENUS;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        List group = getGroup(groupPosition);
        return group.size();
    }

    @Override
    public List getGroup(int groupPosition) {
        List group = null;
        switch(groupPosition){
            case UNREAD_GROUP:
                group = unreadMenu;
                break;
            case PRIVATE_GROUP:
                group = privateMenu;
                break;
            case STAFF_GROUP:
                group = staffMenu;
                break;
        }
        return group;
    }

    @Override
    public NavDrawerItem getChild(int groupPosition, int childPosition) {
        return (NavDrawerItem) getGroup(groupPosition).get(childPosition);

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
            convertView = inflater.inflate(R.layout.menu_header, parent, false);
        }
        TextView textView = (TextView) convertView.findViewById(android.R.id.text1);
        textView.setText(getGroupTitle(groupPosition));
        //textView.setTextSize(activity.getResources().getDimension(R.dimen.nav_drawer_group_text_size)); //aka the header
        textView.setPaintFlags(textView.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View child = null;

        switch (groupPosition){

            case UNREAD_GROUP:
            case PRIVATE_GROUP:
                child = inflater.inflate(R.layout.custom_nav_drawer_row, parent, false);
                TextView userTextView = (TextView) child.findViewById(R.id.nav_row_text_view);

                if(groupPosition == UNREAD_GROUP){
                    TextView notificationImageView = (TextView) child.findViewById(R.id.nav_row_notification_text_view);
                    notificationImageView.setVisibility(View.VISIBLE);
                }

                Conversation convo = (Conversation) getChild(groupPosition,childPosition);
                convo.v = child;

                userTextView.setText(convo.getName());
                child.setTag(R.id.CONVERSATION, convo.getID());

                if(activity.selectedConversation != null && activity.selectedConversation.getID() == convo.getID()){
                    activity.selectedConversation.deselect();
                    convo.select();
                }else{
                    convo.deselect();
                }

                break;

            case STAFF_GROUP:
                child = inflater.inflate(R.layout.inner_list, parent, false);
                TextView departmentTextView = (TextView) child.findViewById(R.id.department_text_view);

                NavDrawerInnerGroup innerGroup = (NavDrawerInnerGroup) getChild(groupPosition, childPosition);
                departmentTextView.setText(innerGroup.getName());

                List<User> users = innerGroup.getChildren();
                LinearLayout userHolder = (LinearLayout) child.findViewById(R.id.inner_layout);
                for(User u : users){
                    String name = u.name;
                    Long id = u.getID();

                    View userView = inflater.inflate(R.layout.custom_nav_drawer_row, userHolder, false);
                    userHolder.addView(userView);
                    userTextView = (TextView) userView.findViewById(R.id.nav_row_text_view);
                    userTextView.setText(name);
                    userView.setTag(R.id.USER, id);

                    u.v = userView;

                    if(activity.selectedConversation != null && activity.selectedConversation.getID() == u.getID()){
                        activity.selectedConversation.deselect();
                        u.select();
                    }else{
                        u.deselect();
                    }
                }


                break;

        }
        return child;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true ;
    }

    public String getGroupTitle(int groupPosition){
        String title = "";
        switch(groupPosition){
            case UNREAD_GROUP:
                title = "Unread";
                break;
            case PRIVATE_GROUP:
                title = "Private";
                break;
            case STAFF_GROUP:
                title = "Staff";
                break;
        }
        return title;
    }

    public void addConversation(int groupPosition, Conversation c){
        List<NavDrawerItem> group = null;

        switch(groupPosition){
            case UNREAD_GROUP:
                group = unreadMenu;
                group.add(c);
                break;
            case PRIVATE_GROUP:
                group = privateMenu;
                group.add(c);

                break;
        }

        dataSetChanged();
    }

    private void dataSetChanged(){
        activity.runOnUiThread(new Runnable(){
            public void run(){
                notifyDataSetChanged();
            }
        });
    }


    public void addDepartment(String name){
        NavDrawerInnerGroup newGroup = new NavDrawerInnerGroup(activity, name);
        staffMenu.add(newGroup);
        dataSetChanged();
    }

    public void addUserToDepartment(String departmentName, User newUser){
        NavDrawerInnerGroup depart = null;
        for(NavDrawerInnerGroup g: staffMenu){
            if(g.getName().equals(departmentName)){
                depart = g;
                break;
            }
        }

        if(depart == null){
            //This should not happen
            return;
        }

        depart.addChild(newUser);
        dataSetChanged();
    }

}
