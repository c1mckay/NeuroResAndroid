package edu.ucsd.neurores;

import android.content.Context;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tbpetersen on 2/23/2017.
 */

public class NavDrawerAdapter extends BaseExpandableListAdapter{

    private static final int UNREAD_GROUP = 0;
    private static final int STAFF_GROUP = 1;
    private static final int PRIVATE_GROUP = 2;

    private List<Group<NavDrawerItem>> groups;

    private MainActivity activity;

    NavDrawerAdapter(MainActivity activity){

        this.activity = activity;

        groups = new ArrayList<Group<NavDrawerItem>>();

        groups.add(new Group<NavDrawerItem>(UNREAD_GROUP));
        groups.add(new Group<NavDrawerItem>(STAFF_GROUP));
        groups.add(new Group<NavDrawerItem>(PRIVATE_GROUP));


    }

    @Override
    public int getGroupCount() {
        return getVisibleGroups().size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if(groupPosition == -1){
            Log.v("warning", "Trying to get groupPosition -1");
            return 0;
        }
        Group<NavDrawerItem> group = getGroup(groupPosition);
        return group.size();
    }

    @Override
    public Group<NavDrawerItem> getGroup(int groupPosition) {
        return getVisibleGroups().get(groupPosition);
    }

    @Override
    public NavDrawerItem getChild(int groupPosition, int childPosition) {
        return getGroup(groupPosition).getItem(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, final ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.menu_header, parent, false);

        TextView textView = (TextView) convertView.findViewById(android.R.id.text1);
        textView.setText(getGroupTitle(groupPosition));
        textView.setPaintFlags(textView.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);


        setExpanderImage(convertView, parent, groupPosition);


        return convertView;
    }

    private void setExpanderImage(View convertView, ViewGroup parent, int groupPosition) {
        final Group<NavDrawerItem> group = getGroup(groupPosition);
        ImageView iv = (ImageView) convertView.findViewById(R.id.expander);

        if(group.isExpanded()){
            ((ExpandableListView)parent).expandGroup(groupPosition);
            iv.setImageResource(R.drawable.contrator);

        }else{
            ((ExpandableListView)parent).collapseGroup(groupPosition);
            iv.setImageResource(R.drawable.expander);
        }
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View child = null;
        Group<NavDrawerItem> group = getGroup(groupPosition);
        TextView userTextView = null;

        switch (group.getID()){

            case UNREAD_GROUP:
            case PRIVATE_GROUP:

                child = inflater.inflate(R.layout.custom_nav_drawer_row, parent, false);


                if(group.getID() == UNREAD_GROUP){
                    setUnreadNumber(child, groupPosition, childPosition);
                }

                Conversation conversation = (Conversation) getChild(groupPosition,childPosition);
                conversation.viewInNavDrawer = child;

                setConversationNameAndTag(child, conversation, R.id.CONVERSATION);

                if(activity.selectedConversation != null && activity.selectedConversation.getID() == conversation.getID()){
                    activity.selectedConversation.deselect();
                    conversation.select();
                }else{
                    conversation.deselect();
                }

                if(conversation.getNumberOfUsers() == 1){
                    if(conversation.getUserAtIndex(0).isOnline()){
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
                    String name = u.getName();
                    Long id = u.getID();

                    View userView = inflater.inflate(R.layout.custom_nav_drawer_row, userHolder, false);
                    userHolder.addView(userView);
                    userTextView = (TextView) userView.findViewById(R.id.nav_row_text_view);
                    userTextView.setText(name);
                    userView.setTag(R.id.USER, id);

                    ImageView onlineImage = (ImageView) userView.findViewById(R.id.nav_row_status_image_view);
                    if(onlineImage != null && u.isOnline()){
                        onlineImage.setImageResource(R.drawable.online);
                        child.invalidate();
                    }

                    userView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            activity.onViewClicked(v    );
                        }
                    });

                    u.viewInNavDrawer = userView;
                }

                child.setTag(R.id.STAFFGROUP, childPosition);


                break;

        }
        return child;
    }

    private void setConversationNameAndTag(View child, Conversation conversation, int tagID) {
        TextView userTextView = (TextView) child.findViewById(R.id.nav_row_text_view);
        userTextView.setText(conversation.getName());
        child.setTag(tagID, conversation.getID());
    }

    private void setUnreadNumber(View child, int groupPosition, int childPosition) {
        TextView notificationTextView = (TextView) child.findViewById(R.id.nav_row_notification_text_view);
        notificationTextView.setVisibility(View.VISIBLE);
        Conversation conversation = (Conversation) getChild(groupPosition, childPosition);
        if(conversation.getNumOfUnread() == 0){
            notificationTextView.setVisibility(View.INVISIBLE);
        }else {
            notificationTextView.setVisibility(View.VISIBLE);
            if (conversation.getNumOfUnread() > 9) {
                notificationTextView.setText(R.string.max_num_unread_messages);
            } else {
                notificationTextView.setText(conversation.getNumOfUnread() + "");
            }
        }
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true ;
    }

    public String getGroupTitle(int groupPosition){
        String title = "";
        Group group = getGroup(groupPosition);
        switch(group.getID()){
            case UNREAD_GROUP:
                title = activity.getResources().getString(R.string.unread_menu_title);
                break;
            case PRIVATE_GROUP:
                title = activity.getResources().getString(R.string.private_menu_title);
                break;
            case STAFF_GROUP:
                title = activity.getResources().getString(R.string.staff_menu_title);
                break;
        }
        return title;
    }

    public void addConversation(int groupID, Conversation c){
        Group<NavDrawerItem> group = getGroupByID(groupID);
        group.addItem(c);

        dataSetChanged();
    }

    public void dataSetChanged(){
        activity.runOnUiThread(new Runnable(){
            public void run(){
                notifyDataSetChanged();
            }
        });
    }


    public void addDepartment(String name){
        Group<NavDrawerItem> staffGroup = getGroupByID(STAFF_GROUP);
        if(staffGroup != null){
            NavDrawerInnerGroup newGroup = new NavDrawerInnerGroup(activity, name);
            staffGroup.addItem(newGroup);
            dataSetChanged();
        }else{
            Log.v("warning", "Staff group could not be found while trying to add department");
        }

    }

    public void addUserToDepartment(String departmentName, User newUser){
        Group<NavDrawerItem> staffGroup = getGroupByID(STAFF_GROUP);
        if(staffGroup != null){
            for(int i = 0; i < staffGroup.size(); i++){
                NavDrawerInnerGroup innerGroup = (NavDrawerInnerGroup) staffGroup.getItem(i);
                if(innerGroup.getName().equals(departmentName)){
                    innerGroup.addChild(newUser);
                    dataSetChanged();
                    return;
                }
            }
        }
    }

    public void moveConversationToFirstPosition (int groupID, Conversation conversation){
        Group<NavDrawerItem> group = getGroupByID(groupID);
        if(group == null){
            return;
        }
        group.moveItemToFirstPosition(conversation);
        dataSetChanged();
    }

    public void moveConversationToPrivate(Conversation conversation){
        Group<NavDrawerItem> unreadGroup = getGroupByID(UNREAD_GROUP);
        Group<NavDrawerItem> privateGroup = getGroupByID(PRIVATE_GROUP);


        if(unreadGroup == null || privateGroup == null){
            Log.v("warning", "Could not move conversation to private");
            return;
        }


        if(unreadGroup.contains(conversation)){
            unreadGroup.removeItem(conversation);
        }else{
            Log.v("warning", "Conversation to move is not in unread");
        }

        if(! privateGroup.contains(conversation)){
            if(conversation.hasOnlineUser()){
                privateGroup.addItem(0, conversation);
            }else{
                privateGroup.addItem(conversation);
            }
        }

        dataSetChanged();
    }

    public void moveConversationToUnread(Conversation conversation){
        Group<NavDrawerItem> unreadGroup = getGroupByID(UNREAD_GROUP);
        Group<NavDrawerItem> privateGroup = getGroupByID(PRIVATE_GROUP);

        if(unreadGroup == null || privateGroup == null){
            Log.v("error", "Could not move conversation to unread");
            return;
        }


        if(privateGroup.contains(conversation)){
            privateGroup.removeItem(conversation);
        }

        if(! unreadGroup.contains(conversation)){
            if(conversation.hasOnlineUser()){
                unreadGroup.addItem(0, conversation);
            }else{
                unreadGroup.addItem(conversation);
            }
        }

        dataSetChanged();
    }


    public List<Conversation> getOnlineInGroup(int groupID){
        List<Conversation> newList = new ArrayList<Conversation>();
        Group<NavDrawerItem> backingList = getGroupByID(groupID);
        for(int i = 0; i < backingList.size(); i++){
            Conversation conversation = (Conversation) backingList.getItem(i);
            if(conversation.hasOnlineUser()){
                newList.add(conversation);
            }
        }
        return newList;
    }

    public void toggleIsExpanded(int childPosition){
        Group<NavDrawerItem> staffGroup = getGroupByID(STAFF_GROUP);
        NavDrawerInnerGroup inner = (NavDrawerInnerGroup) staffGroup.getItem(childPosition);
        inner.setIsExpanded(! inner.getIsExpanded());
    }

    public void printLists(){
        Log.v("taggy", "Unread:");
        printList(UNREAD_GROUP);
        Log.v("taggy", "Private:");
        printList(PRIVATE_GROUP);
        Log.v("taggy", "\n");
    }

    private void printList(int groupID){
        Group<NavDrawerItem> group = getGroupByID(groupID);
        for(int i =0; i < group.size(); i++){
            Conversation conversation = (Conversation) group.getItem(i);
            Log.v("taggy", conversation.toString());
        }
    }

    private Group<NavDrawerItem> getGroupByID(int groupID){
        for(Group<NavDrawerItem> g : groups){
            if(groupID == g.getID()){
                return g;
            }
        }
        Log.v("warning", "No group with ID " + groupID + " was found");
        return null;
    }

    private List<Group<NavDrawerItem>> getVisibleGroups(){
        List<Group<NavDrawerItem>> visibleGroups = new ArrayList<Group<NavDrawerItem>>();
        for(Group g : groups){
            if(g.isVisible()){
                visibleGroups.add(g);
            }
        }
        return visibleGroups;
    }

    public int getGroupPosition(int groupID){
        List<Group<NavDrawerItem>> visibleGroups = getVisibleGroups();
        for(int i = 0; i < visibleGroups.size(); i++){
            if(visibleGroups.get(i).getID() == groupID){
                return i;
            }
        }
        Log.v("error", "Group with id " + groupID + " is not currently visible");
        return -1;
    }

    public boolean groupIsVisible(int groupID){
        return getGroupByID(groupID).isVisible();
    }

}
