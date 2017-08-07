package edu.ucsd.neurores;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

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
        final View headerView = convertView;
        TextView textView = (TextView) convertView.findViewById(android.R.id.text1);
        textView.setText(getGroupTitle(groupPosition));
        //textView.setTextSize(activity.getResources().getDimension(R.dimen.nav_drawer_group_text_size)); //aka the header
        textView.setPaintFlags(textView.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);


        // Assign the correct indicator (expanded or collapsed)
        ImageView iv = (ImageView) convertView.findViewById(R.id.expander);
            if(isExpanded){
                iv.setImageResource(R.drawable.contrator);
            }else{
                iv.setImageResource(R.drawable.expander);
            }

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
                    TextView notificationTextView = (TextView) child.findViewById(R.id.nav_row_notification_text_view);
                    notificationTextView.setVisibility(View.VISIBLE);
                    Conversation conversation = (Conversation) getChild(groupPosition, childPosition);
                    if(conversation.getNumOfUnseen() == 0){
                        notificationTextView.setVisibility(View.INVISIBLE);
                    }else {
                        notificationTextView.setVisibility(View.VISIBLE);
                        if (conversation.getNumOfUnseen() > 9) {
                            notificationTextView.setText(R.string.max_num_unread_messages);
                        } else {
                            notificationTextView.setText(conversation.getNumOfUnseen() + "");
                        }
                    }
                }

                Conversation convo = (Conversation) getChild(groupPosition,childPosition);
                convo.v = child;

                userTextView.setText(convo.getName());
                child.setTag(R.id.CONVERSATION, convo.getID());

                if(activity.selectedConversation != null && activity.selectedConversation.getID().equals(convo.getID())){
                    activity.selectedConversation.deselect();
                    convo.select();
                }else{
                    convo.deselect();
                }

                if(convo.getNumberOfUsers() == 1){
                    if(convo.getUserAtIndex(0).isOnline()){
                        ImageView onlineImage = (ImageView) child.findViewById(R.id.nav_row_status_image_view);
                        if(onlineImage != null){
                            onlineImage.setImageResource(R.drawable.online);
                        }
                    }
                }

                break;

            case STAFF_GROUP:
                child = inflater.inflate(R.layout.inner_list, parent, false);

                TextView departmentTextView = (TextView) child.findViewById(R.id.department_text_view);

                NavDrawerInnerGroup innerGroup = (NavDrawerInnerGroup) getChild(groupPosition, childPosition);
                departmentTextView.setText(innerGroup.getName());
                LinearLayout userHolder = (LinearLayout) child.findViewById(R.id.inner_layout);
                int imageID;
                if(innerGroup.getIsExpanded()){
                    userHolder.setVisibility(View.VISIBLE);
                    imageID = R.drawable.contrator;
                }else{
                    userHolder.setVisibility(View.GONE);
                    imageID = R.drawable.expander;
                }

                ImageView iv = (ImageView) child.findViewById(R.id.expander);
                iv.setImageResource(imageID);

                List<User> users = innerGroup.getChildren();
                for(User u : users){
                    String name = u.name;
                    Long id = u.getID();

                    View userView = inflater.inflate(R.layout.custom_nav_drawer_row, userHolder, false);
                    userHolder.addView(userView);
                    userTextView = (TextView) userView.findViewById(R.id.nav_row_text_view);
                    userTextView.setText(name);
                    userView.setTag(R.id.USER, id);

                    u.v = userView;
                }

                child.setTag(R.id.STAFFGROUP, childPosition);


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

    public void moveConversationToFirstPosition (int groupPosition, Conversation conversation){
        List group = getGroup(groupPosition);
        group.remove(conversation);
        group.add(0, conversation);
        notifyDataSetChanged();
    }

    public void moveConversationToPrivate(Conversation conversation){
        List<Conversation> unreadGroup = getGroup(UNREAD_GROUP);
        List<Conversation> privateGroup = getGroup(PRIVATE_GROUP);

        if(unreadGroup.contains(conversation)){
            unreadGroup.remove(conversation);
        }

        if(! privateGroup.contains(conversation)){
            if(conversation.hasOnlineUser()){
                privateGroup.add(0, conversation);
            }else{
                privateGroup.add(conversation);
            }
        }
        notifyDataSetChanged();
    }

    public void moveConversationToUnread(Conversation conversation){
        List<Conversation> unreadGroup = getGroup(UNREAD_GROUP);
        List<Conversation> privateGroup = getGroup(PRIVATE_GROUP);

        if(privateGroup.contains(conversation)){
            privateGroup.remove(conversation);
        }

        if(! unreadGroup.contains(conversation)){
            if(conversation.hasOnlineUser()){
                unreadGroup.add(0, conversation);
            }else{
                unreadGroup.add(conversation);
            }
        }

        notifyDataSetChanged();
    }

    public void toggleIsExpanded(int childPosition){
        NavDrawerInnerGroup inner = staffMenu.get(childPosition);
        inner.setIsExpanded(! inner.getIsExpanded());
    }
}
