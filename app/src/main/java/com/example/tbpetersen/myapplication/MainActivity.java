package com.example.tbpetersen.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SubMenu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

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
    public HashMap<Long,User> currentConversations;
    /* The currently selected user */
    public User selectedUser;

    /**
     * Runs when the app is launched
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*Initialize */
        currentConversations = new HashMap<Long, User>();
        messageEditText = (EditText) findViewById(R.id.message_edit_text);


        // Set the fragment
        currentFragment = new MainFragment();
        currentFragment.addUser("Demo");
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

        //Hardcode in private message users
        String[] hardCodedPrivate = getResources().getStringArray(R.array.private_hardcode);
        String[] hardCodedDepartments = getResources().getStringArray(R.array.departments);
        for(int i = 0; i < hardCodedPrivate.length; i++){
            long id = 500 + i;
            addConversation(PRIVATE_MENU_GROUP, new User(this, id, hardCodedPrivate[i]));
        }

        //Hardcode in departments
        for(int i = 0; i < hardCodedDepartments.length; i++){
            addDepartment(hardCodedDepartments[i]);
        }

        addUserToDepartment("Stroke", new User(this, 49L, hardCodedPrivate[1]));
        addUserToDepartment("Stroke", new User(this, 50L, hardCodedPrivate[2]));
        addUserToDepartment("Stroke", new User(this, 51L, hardCodedPrivate[3]));
        addUserToDepartment("Stroke", new User(this, 52L, hardCodedPrivate[4]));




        addConversation(UNREAD_MENU_GROUP, new User(this, 63L, hardCodedPrivate[7]));

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
                    currentFragment.addMessage("Trevor", messageEditText.getText().toString());
                    messageEditText.setText("");
                    hideSoftKeyboard();
                    messageEditText.clearFocus();
                }
            }
        });
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
            String searchedUsername = data.getStringExtra("USERNAME");
            long searchedID = data.getIntExtra("ID", -1);

            // Deselect the previously selected user (change the background
            // color and selectedUser)
            if(selectedUser != null && selectedUser.v != null){
                selectedUser.v.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            }

            /* Set selectedUser */
            if(currentConversations.get(searchedID) == null){
                selectedUser = new User(this, searchedID, searchedUsername);
                currentConversations.put(selectedUser.id, selectedUser);
            }else{
                selectedUser = currentConversations.get(searchedID);
            }
            // Tell the main activity that the fragment needs to be changed
            needToChangeFragment = true;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        // Check if the main fragment needs to be changed
        if(needToChangeFragment){
            if(selectedUser.v == null) {
                // Add a view to the navigation bar for the new user
                addConversation(PRIVATE_MENU_GROUP, selectedUser);
            }else{
                // Highlight the view that is already in the nav bar
                selectedUser.v.setBackgroundColor(getResources().getColor(R.color.selected));
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
    public void userClickedOn(View v) {
        //Get the id of the user that was clicked on
        long id = (long)v.getTag();
        // Find the user in the hashmap
        User u = currentConversations.get(id);
                if(u != null){
                    //Deselect previous
                    if(selectedUser != null && selectedUser.v != null){
                        selectedUser.v.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    }
                    // Select clicked on user
                    selectedUser = u;
                    selectedUser.v = v;
                    selectedUser.v.setBackgroundColor(getResources().getColor(R.color.selected));
                }
        // Reset the input fields and hide it
        messageEditText.setText("");
        hideSoftKeyboard();

        changeFragment();
    }

    /**
     * Add a view to the navigation drawer for the newUser
     * @param newUser the user to have a view added for
     */
    private void addConversation(int groupPosition, User newUser){
        navDrawerAdapter.addConversation(groupPosition, newUser);
        drawerListView.expandGroup(groupPosition);

        currentConversations.put(newUser.id, newUser);
    }

    /**
     * Change the messages in the fragment to be the messages of selectedUser
     */
    private void changeFragment(){
        currentFragment = new MainFragment();
        currentFragment.addUser(selectedUser.name);
        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, currentFragment);
        fragmentTransaction.commit();
        currentFragment.queueMessage(selectedUser.name, "This is the old message");

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
        currentConversations.put(u.id, u);
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

    private void populateNavDrawer(){

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

    private void addDepartment(String name){
        navDrawerAdapter.addDepartment(name);
        drawerListView.expandGroup(STAFF_MENU_GROUP);
    }

    private void addUserToDepartment(String departmentName, User newUser){
        navDrawerAdapter.addUserToDepartment(departmentName,newUser);
        currentConversations.put(newUser.id, newUser);
    }
}
