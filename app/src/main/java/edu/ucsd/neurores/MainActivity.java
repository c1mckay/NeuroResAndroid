package edu.ucsd.neurores;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.crash.FirebaseCrash;

import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, DrawerLayout.DrawerListener{

    public static final int UNREAD_MENU_GROUP = 0;
    public static final int STAFF_MENU_GROUP = 1;
    public static final int PRIVATE_MENU_GROUP = 2;


    Toolbar toolbar = null;
    //  This adapter controls the Navigation Drawer's views and data
    private NavDrawerAdapter navDrawerAdapter;
    // The list view in the Navigation Drawer
    private ExpandableListView drawerListView;
    // The input for messages at the bottom of the screen
    private EditText messageEditText;
    // The fragment that holds the messages of the selected user
    private MainFragment currentFragment;
    /* Request for when the search activity is launched by clicking "Search"
       in the navigation drawer */
    private int SEARCH_USER_REQUEST = 1;

    /* Boolean used to keep track of when a different user's messages needs to
       be loaded */
    private boolean needToChangeFragment = false;

    /* Contains all the users that that there are converstaions with */
    public HashMap<Long,User> userList;
    public HashMap<Long,Conversation> currentConversations;
    /* The currently selected user */
    public Conversation selectedConversation;

    public User loggedInUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Check for the login Token

        if(getToken() == null){
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        /*Initialize */
        currentConversations = new HashMap<>();
        userList = new HashMap<>();
        messageEditText = (EditText) findViewById(R.id.message_edit_text);


        // Set the fragment
        currentFragment = startMainFragment();
        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, currentFragment);
        fragmentTransaction.commit();

        // Setup the toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Do not display the app name in the toolbar
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Get a reference to the list view that holds the main data in the
        // navigation drawer
        drawerListView = (ExpandableListView) findViewById(R.id.nav_view);
        // Setup the adapter used to populate the list navigation drawer
        navDrawerAdapter = new NavDrawerAdapter(this);
        drawerListView.setAdapter(navDrawerAdapter);

        // Expand the list on start
        for (int i = 0; i < navDrawerAdapter.getGroupCount(); i++){
            drawerListView.expandGroup(i);
        }




        // Set up the drawer and its listeners
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        drawer.addDrawerListener(this);
        toggle.syncState();

        // Setup the button used to send messages
        final Button messageSendButton = (Button) findViewById(R.id.message_send_button);
        messageSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // The text in the input field
                String newMessage = messageEditText.getText().toString();
                //Only send the message if it is not empty
                if(! newMessage.equals("")){

                    //currentFragment.addMessage("Trevor", messageEditText.getText().toString());
                    currentFragment.socket.pushMessage(newMessage);
                    messageEditText.setText("");
                }

                hideSoftKeyboard();
                messageEditText.clearFocus();
            }
        });

        loadData();
    }

    protected String getToken(){
        SharedPreferences sPref = getSharedPreferences(LoginActivity.PREFS, Context.MODE_PRIVATE);
        return sPref.getString(LoginActivity.TOKEN, null);
    }

    private String getUsername(){
        SharedPreferences sPref = getSharedPreferences(LoginActivity.PREFS, Context.MODE_PRIVATE);
        return sPref.getString(LoginActivity.NAME, null);
    }

    private MainFragment startMainFragment(){
        MainFragment mFrag = new MainFragment();
        Bundle i = new Bundle();
        i.putString("token", getToken());
        mFrag.setArguments(i);
        return mFrag;
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        // Close the drawer if its open
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // If the returning activity was the search activity
        if(requestCode == SEARCH_USER_REQUEST && resultCode == Activity.RESULT_OK){
            // Get the username and id of the newly searched user
            String searchedConversation = data.getStringExtra("USERNAME");
            long searchedID = data.getIntExtra("ID", -1);
            boolean isConversation = searchedConversation != null;

            // Deselect the previously selected user (change the background
            // color and selectedUser)
            if(selectedConversation != null && selectedConversation.v != null){
                selectedConversation.v.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            }

            /* Set selectedUser */
            if(userList.containsKey(searchedID)) {
                selectedConversation = currentConversations.get(searchedID);
            }
                //userList.put(selectedUser.id, selectedUser);
            //}else{
                //selectedUser = userList.get(searchedID);
            //}
            // Tell the main activity that the fragment needs to be changed
            needToChangeFragment = true;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        // Check if the main fragment needs to be changed
        if(needToChangeFragment){
            if(selectedConversation.v == null) {
                // Add a view to the navigation bar for the new user
                addToNavBar(PRIVATE_MENU_GROUP, selectedConversation);
            }else{
                // Highlight the view that is already in the nav bar
                selectedConversation.select();
                //selectedUser.v.setBackgroundColor(getResources().getColor(R.color.selected));
            }
            changeFragment();
            needToChangeFragment = false;
        }
        super.onResume();
        hideSoftKeyboard();
    }

    /**
     * The method called when the search button in the nav drawer is clicked.
     * Starts the search activity "SearchActivity" for a result
     * @param view the view that was clicked on
     */
    public void searchOnClick(View view) {
        Intent startSearch = new Intent(MainActivity.this, SearchActivity.class);
        startActivityForResult(startSearch, SEARCH_USER_REQUEST);
    }

    /**
     * The method called when a user is clicked in the nav drawer is clicked.
     * Deselects the previously selected user and selects the clicked on user.
     * @param v user clicked on in the nav drawer
     */
    public void onViewClicked(View v) {
        //Get the id of the user that was clicked on

        if(v.getTag(R.id.CONVERSATION) != null){
            onConversationClick(v, (Long) v.getTag(R.id.CONVERSATION));
        }else if(v.getTag(R.id.USER) != null){
            onUserClick((Long) v.getTag(R.id.USER));
        }
        return;
    }

    private void onUserClick(long user_id){

    }

    private void onConversationClick(View v, long conversation_id){
        NavDrawerItem c = currentConversations.get(conversation_id);
        if(currentConversations.containsKey(conversation_id)){ //a conversation was clicked, and we're about to load it
            //Deselect previous
            if(selectedConversation != null){
                selectedConversation.deselect();
            }
            // Select clicked on user
            selectedConversation = currentConversations.get(conversation_id);;
            selectedConversation.v = v;
            selectedConversation.select();
        }
        // Reset the input fields and hide it
        messageEditText.setText("");
        hideSoftKeyboard();

        changeFragment();
    }

    /**
     * Add a view to the navigation drawer for the newUser
     * @param conversation the user to have a view added for
     */
    private void addToNavBar(int groupPosition, Conversation conversation){
        navDrawerAdapter.addConversation(groupPosition, conversation);
        drawerListView.expandGroup(groupPosition);

        //userList.put(cogetID(), newUser);
    }

    /**
     * Change the messages in the fragment to be the messages of selectedUser
     */
    private void changeFragment(){
        currentFragment = startMainFragment();
        currentFragment.conversation = selectedConversation;
        currentFragment.userName = loggedInUser.name;
        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, currentFragment);
        fragmentTransaction.commit();
        //currentFragment.queueMessage(selectedConversation.name, "Messages should be loaded at this point");
        currentFragment.loadMessages(selectedConversation, userList);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
    }

    /**
     * Toggle the visibility of the given view between "gone" and "visible"
     * @param v the view that will have its visibility toggled
     */
    public void toggleVisibility(View v){
        v = (View) v.getParent();
        View innerLayout = v.findViewById(R.id.inner_layout);
        int newVisibility;
        if(innerLayout.getVisibility() == LinearLayout.VISIBLE){
            newVisibility = LinearLayout.GONE;
        }else{
            newVisibility = LinearLayout.VISIBLE;
        }
        innerLayout.setVisibility(newVisibility);
    }

    /**
     * Adds a user to the hashmap containing all current conversations
     * @param u the user to be added to the hashmap
     */
    public void addUserToHashTable(User u){
        //currentConversations.put(u.id, u);
    }

    /**
     * Hides the soft keyboard
     */
    public void hideSoftKeyboard() {
        if(getCurrentFocus()!=null) {
            InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }


    private void addDepartment(String name){
        navDrawerAdapter.addDepartment(name);
        drawerListView.expandGroup(STAFF_MENU_GROUP);
    }

    private void addUserToDepartment(String departmentName, User newUser){
        navDrawerAdapter.addUserToDepartment(departmentName,newUser);
        userList.put(newUser.getID(), newUser);
    }

    private void loadData(){
        hideMainElements();
        // Load data from server
        SessionWrapper.UpdateUsers(getToken(), new SessionWrapper.OnCompleteListener() {

            public void onComplete(String s) {
                List<User> users = SessionWrapper.GetUserList(s);
                for(User u: users){
                    u.setContext(MainActivity.this);
                }
                onUsersLoaded(users);
            }


            public void onError(String s) {

            }
        });
        // sessionWrapper.updateConversations is called at the end of onUsersLoaded
        //(currentConversations needs to be populated before the call is made to updateConversations)

        /* How to add to unread section */
        //addConversation(UNREAD_MENU_GROUP, new User(this, 63L, hardCodedPrivate[7]));


        onLoadComplete();
    }

    public void onUsersLoaded(List<User> users){
        String username = getUsername();
        for(User u : users){
            u.setContext(this);
            if(u.name.equals(username)){
                loggedInUser = u;
                TextView nameInSettingsView = (TextView) findViewById(R.id.username_in_settings_text_view);
                nameInSettingsView.setText(loggedInUser.name);
            }
        }
        if(users != null) {
            populateStaff(users);
        }else{
            Log.v("tag", "Users is null");
        }

        SessionWrapper.UpdateConversations(getToken(), new SessionWrapper.OnCompleteListener() {
            public void onComplete(String s) {
                if(s == null){
                    //indicates the http request returned null, and something went wrong. have them login again
                    Intent i = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(i);
                    finish();
                    return;
                }
                List<Conversation> conversations = SessionWrapper.TranslateConversationMetadata(s);

                //called update conversation without the context
                //needs to be connected here now
                for(Conversation c: conversations){
                    c.setContext(MainActivity.this);
                }
                if(conversations != null) {
                    populatePrivate(conversations);
                }else{
                    Log.v("tag", "Users is null");
                }
            }

            @Override
            public void onError(String s) {

            }
        });
    }

    public void populateStaff(List<User> users){
        TreeSet<String> departments = new TreeSet<String>();
        for(User u : users){
            departments.add(u.userType);
        }

        String curr = departments.first();
        while(curr != null){
            addDepartment(curr);
            curr = departments.higher(curr);
        }

        for(User u : users){
            if(u != loggedInUser){
                //currentConversations.put(u.id, u);
                addUserToDepartment(u.userType, u);
            }
        }

    }

    public void populatePrivate(List<Conversation> conversations){
        for(Conversation c : conversations){
            if(c.users.get(0) == null){
                Log.v("tag", "User is null : " + c.users);
            }
        }
        for(Conversation c : conversations){
            currentConversations.put(c.getID(), c);
            addToNavBar(PRIVATE_MENU_GROUP, c);
        }
    }

    private void hideMainElements(){
        getSupportActionBar().hide();
        findViewById(R.id.main_recycler_view_holder).setVisibility(View.GONE);
        findViewById(R.id.message_edit_text).setVisibility(View.GONE);
        findViewById(R.id.message_send_button).setVisibility(View.GONE);
        ((DrawerLayout)findViewById(R.id.drawer_layout)).setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    private void onLoadComplete(){
        getSupportActionBar().show();
        findViewById(R.id.main_recycler_view_holder).setVisibility(View.VISIBLE);
        findViewById(R.id.message_edit_text).setVisibility(View.VISIBLE);
        findViewById(R.id.message_send_button).setVisibility(View.VISIBLE);
        ((DrawerLayout)findViewById(R.id.drawer_layout)).setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        findViewById(R.id.loading_logo_image_view).setVisibility(View.GONE);
    }

    /***** Methods for listening for the navigation drawer opening/closing *****/

    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {

    }

    @Override
    public void onDrawerOpened(View drawerView) {
        hideSoftKeyboard();
    }

    @Override
    public void onDrawerClosed(View drawerView) {

    }

    @Override
    public void onDrawerStateChanged(int newState) {

    }
}
