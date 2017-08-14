package edu.ucsd.neurores;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.PowerManager;
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
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.Socket;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import javax.net.ssl.SSLSocket;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, DrawerLayout.DrawerListener {

    public static final int UNREAD_MENU_GROUP = 0;
    public static final int STAFF_MENU_GROUP = 1;
    public static final int PRIVATE_MENU_GROUP = 2;

    public static final String PREV_CONVERSATION_ID = "previousConversationID";
    public static final String CONVERSATION_ID = "conversationID";


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
    Toast mostRecentToast;

    private WebSocket socket;
    boolean isPaused;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        // Check for the login Token
        if(getToken() == null || ! isConnectedToNetwork()){
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
            finish();
            return;
        }
        if(FirebaseInstanceId.getInstance().getToken() != null)
            Log.d("token", FirebaseInstanceId.getInstance().getToken());


        if(getIntent().hasExtra(CONVERSATION_ID)){
            setPreviousConversationID(getIntent().getLongExtra(CONVERSATION_ID, -1));
        }

        setContentView(R.layout.activity_main);

        /*Initialize */
        // TODO Reconnect socket when screen wakes from idle
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        //pm.isDeviceIdleMode();
        //pm.isInteractive();

        currentConversations = new HashMap<>();
        userList = new HashMap<>();
        isPaused = false;
        messageEditText = (EditText) findViewById(R.id.message_edit_text);


        // Set the fragment
        //currentFragment = startMainFragment();



        // Setup the toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Set the navigation bar text to be the empty string
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.welcome));

        // Get a reference to the list view that holds the main data in the
        // navigation drawer
        drawerListView = (ExpandableListView) findViewById(R.id.nav_view);
        // Setup the adapter used to populate the list navigation drawer
        navDrawerAdapter = new NavDrawerAdapter(this);
        drawerListView.setAdapter(navDrawerAdapter);


        // Expand the lists on start
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
                if(! newMessage.equals("") && currentFragment.conversation != null){
                    //currentFragment.pushMessage(newMessage);
                    pushMessage(newMessage);
                    setKeyboardPushing();
                    messageEditText.setText("");
                    scrollToMostRecentMessage();
                }
                //messageEditText.clearFocus();

            }
        });

        loadData();
    }

    private boolean isConnectedToNetwork() {
        final ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();

        return activeNetwork != null && activeNetwork.isConnected();
    }

    @Override
    protected void onPause() {
        isPaused = true;
        closeSocket();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        closeSocket();
        super.onDestroy();
    }

    public void setupMainElements(){
        setInitialFragment();
        connectSocket();
    }

    protected String getToken(){
        SharedPreferences sPref = getSharedPreferences(LoginActivity.PREFS, Context.MODE_PRIVATE);
        return sPref.getString(LoginActivity.TOKEN, null);
    }

    protected boolean hasPreviousConversation(){
        SharedPreferences sPref = getSharedPreferences(LoginActivity.PREFS, Context.MODE_PRIVATE);
        return sPref.getLong(PREV_CONVERSATION_ID, -1) != -1;
    }

    protected boolean hasOngoingConversations(){
        return currentConversations.size() > 0;
    }

    protected long getPreviousConversationID(){
        SharedPreferences sPref = getSharedPreferences(LoginActivity.PREFS, Context.MODE_PRIVATE);
        return sPref.getLong(PREV_CONVERSATION_ID, -1);
    }

    protected void setPreviousConversationID(long newID){
        SharedPreferences sPref = getSharedPreferences(LoginActivity.PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sPref.edit();
        editor.putLong(PREV_CONVERSATION_ID , newID);
        editor.commit();
    }

    private String getUsername(){
        SharedPreferences sPref = getSharedPreferences(LoginActivity.PREFS, Context.MODE_PRIVATE);
        return sPref.getString(LoginActivity.NAME, null);
    }

    private MainFragment startMainFragment(){
        MainFragment mFrag = new MainFragment();
        //mFrag.setupSocket(this);
        Bundle i = new Bundle();
        i.putString("token", getToken());
        mFrag.setArguments(i);
        return mFrag;
    }

    public void setKeyboardPushing(){

    }

    private MainFragment loadOnboardingFragment(){
        MainFragment mFrag = new MainFragment();
        Bundle i = new Bundle();
        i.putString("token", getToken());
        i.putBoolean("hasConversation", false);
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
            long searchedID = data.getLongExtra("CONVERSATION_ID", -1);
            long[] userIDs = data.getLongArrayExtra("USERS_IDS");
            if(searchedID != -1 && userIDs.length > 0){

                // Deselect the previously selected user (change the background
                // color and selectedUser)
                if(selectedConversation != null && selectedConversation.v != null){
                    selectedConversation.v.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                }

            /* Set selectedUser */
                if(currentConversations.containsKey(searchedID)) {
                    selectedConversation = currentConversations.get(searchedID);
                }else{
                    Conversation newConversation = new Conversation(searchedID, this);
                    for(long l : userIDs){
                        if(userList.containsKey(l)){
                            newConversation.addUser(userList.get(l));
                        }else{
                            Log.v("tag", "Error: User with id of " + l + " was said to be in conv "
                                    + searchedID + " but was not found in list of users");
                        }
                    }
                    currentConversations.put(searchedID, newConversation);
                    selectedConversation = newConversation;
                    //addToNavBar(PRIVATE_MENU_GROUP, newConversation);
                }
                //userList.put(selectedUser.id, selectedUser);
                //}else{
                //selectedUser = userList.get(searchedID);
                //}
                // Tell the main activity that the fragment needs to be changed
                needToChangeFragment = true;
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        // Check if the main fragment needs to be changed
        isPaused = false;
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
        connectSocket();
        //TODO checkForNewMessages();
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
        startSearch.putExtra("token", getToken());
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
        }else if(v.getTag(R.id.STAFFGROUP) != null){
            toggleVisibility(v);
            navDrawerAdapter.toggleIsExpanded((int) v.getTag(R.id.STAFFGROUP));
        }
        v.invalidate();
    }

    private void onUserClick(long user_id){
        for(Conversation c: currentConversations.values()){
            if(c.getNumberOfUsers() == 1 && c.getUserAtIndex(0).getID() == user_id){
                onConversationClick(c.v, c.getID());
                return;
            }
        }
        ArrayList<Long> userIDs = new ArrayList<>();
        userIDs.add(user_id);
        createConversation(userIDs,PRIVATE_MENU_GROUP , true, 0);
    }

    private void createConversation(List<Long> userIDs, final int groupID, final boolean changeFragment, final int numOfUnseen){
        SessionWrapper.CreateConversation(this, userIDs, getToken(), new SessionWrapper.OnCompleteListener() {

            public void onComplete(String s) {
                if(s == null){
                    //indicates the http request returned null, and something went wrong. have them login again
                    Intent i = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(i);
                    finish();
                    return;
                }

                try {
                    JSONObject jo = new JSONObject(s);
                    JSONArray users = jo.getJSONArray("user_ids");
                    long id = jo.getLong("conv_id");

                    Conversation conversation = new Conversation(id, MainActivity.this);
                    for(int i = 0; i < users.length(); i++){
                        id = users.getLong(i);
                        if(id != loggedInUser.getID())
                            conversation.addUser(userList.get(id));
                    }
                    conversation.setNumOfUnseen(numOfUnseen);
                    currentConversations.put(conversation.getID(), conversation);
                    Log.v("tag2","Size: " + currentConversations.size());
                    Log.v("taggy", "Adding to nav bar");
                    addToNavBar(groupID, conversation);

                    if(changeFragment){
                        onConversationClick(conversation.v, conversation.getID());
                    }
                    Log.v("taggy", "done creating");

                } catch (JSONException e) {
                    Log.v("taggy", "Fail");
                    Log.v("taggy",  e.getMessage() + "");
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String s) {

            }
        });
    }

    public void onNewConversationDetected(final long conversationID){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SessionWrapper.UpdateConversations(getApplication(), getToken(), new SessionWrapper.OnCompleteListener() {
                    @Override
                    public void onComplete(String s) {
                        if(s == null){
                            //indicates the http request returned null, and something went wrong. have them login again
                            Intent i = new Intent(MainActivity.this, LoginActivity.class);
                            startActivity(i);
                            finish();
                            return;
                        }
                        List<Conversation> conversations = SessionWrapper.TranslateConversationMetadata(s, userList);

                        // Find the newly detected conversation
                        Conversation newConversation = null;
                        for(Conversation c: conversations){
                            if(c.getID() == conversationID){
                                newConversation = c;
                            }
                        }

                        if(newConversation == null){
                            onError("conversation with ID of " + conversationID + " was not found in the list of conversations");
                            return;
                        }
                        Log.v("taggy", "new conversation found! Creating it");
                        createConversation(newConversation.getUserIDs(), UNREAD_MENU_GROUP, false, 1);
                    }

                    @Override
                    public void onError(String s) {

                    }
                });
            }
        });

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
        drawerListView.invalidateViews();

        //userList.put(cogetID(), newUser);
    }

    /**
     * Change the messages in the fragment to be the messages of selectedUser
     */
    private void changeFragment(){
        hideMainElements();
        currentFragment = startMainFragment();
        currentFragment.conversation = selectedConversation;
        currentFragment.userName = loggedInUser.getName();
        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, currentFragment);
        fragmentTransaction.commit();
        //currentFragment.queueMessage(selectedConversation.name, "Messages should be loaded at this point");
        currentFragment.loadMessages(this,selectedConversation, userList);
        getSupportActionBar().setTitle(selectedConversation.getName());

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount() - 1);

        updateMostRecentConversation(selectedConversation.getID());
        updateFrag();
    }

    private void updateFrag(){
        if(socket != null){
            socket.updateFrag(currentFragment);
        }
    }


    /**
     * Change the messages in the fragment to be the messages of selectedUser
     */
    private void setInitialFragment(){
        long conversationID;
        if(hasOngoingConversations() && ! hasPreviousConversation()){
            conversationID = getFirstConversationID();
        }else{
            conversationID = getPreviousConversationID();
        }

        if(isNewUser() || conversationID == -1){
            currentFragment = loadOnboardingFragment();
            android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, currentFragment);
            fragmentTransaction.commit();
            return;
        }

        selectedConversation = currentConversations.get(conversationID);
        if(selectedConversation == null){
            logout(null);
            finish();
            return;
        }
        currentFragment = startMainFragment();
        currentFragment.conversation = selectedConversation;
        currentFragment.userName = loggedInUser.getName();
        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, currentFragment);
        fragmentTransaction.commit();
        //currentFragment.queueMessage(selectedConversation.name, "Messages should be loaded at this point");
        currentFragment.loadMessages(this, selectedConversation, userList);
        getSupportActionBar().setTitle(selectedConversation.getName());

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        //RecyclerView recyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        //recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
    }

    private long getFirstConversationID(){
        if(getNumOfPrivateConversations() > 0){
            return getFirstPrivateConversationID();
        }else{
            return getFirstUnreadConversationID();
        }
    }

    private long getFirstPrivateConversationID(){
        if(getNumOfPrivateConversations() > 0){
            NavDrawerItem item = navDrawerAdapter.getChild(PRIVATE_MENU_GROUP, 0);
            return item.getID();
        }else{
         return -1;
        }
    }

    private long getFirstUnreadConversationID(){
        if(getNumOfUnreadConversations() > 0){
            NavDrawerItem item = navDrawerAdapter.getChild(UNREAD_MENU_GROUP, 0);
            return item.getID();
        }else{
            return -1;
        }
    }

    private int getNumOfPrivateConversations(){
        return navDrawerAdapter.getChildrenCount(PRIVATE_MENU_GROUP);
    }

    private int getNumOfUnreadConversations(){
        return navDrawerAdapter.getChildrenCount(UNREAD_MENU_GROUP);
    }

    private int getTotalNumOfConversations(){
        return getNumOfPrivateConversations() + getNumOfUnreadConversations();
    }

    /**
     * Toggle the visibility of the given view between "gone" and "visible"
     * @param v the view that will have its visibility toggled
     */
    public void toggleVisibility(View v){
        View innerLayout = v.findViewById(R.id.inner_layout);
        int newVisibility;
        int imageID;
        if(innerLayout.getVisibility() == LinearLayout.VISIBLE){
            newVisibility = LinearLayout.GONE;
            imageID = R.drawable.expander;
        }else{
            newVisibility = LinearLayout.VISIBLE;
            imageID = R.drawable.contrator;
        }
        innerLayout.setVisibility(newVisibility);

        ImageView iv = (ImageView) v.findViewById(R.id.expander);
        iv.setImageResource(imageID);
    }

    /**
     * Adds a user to the hashmap containing all current conversations
     * @param u the user to be added to the hashmap
     */
    public void addUserToHashTable(User u){
        //currentConversations.put(u.id, u);
    }

    public boolean isNewUser(){
        return (! hasPreviousConversation()) && (! hasOngoingConversations());
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
        Log.v("contextp", "Loading data");
        SessionWrapper.UpdateUsers(this, getToken(), new SessionWrapper.OnCompleteListener() {

            public void onComplete(String s) {
                if(s == null){
                    //indicates the http request returned null, and something went wrong. have them login again
                    Intent i = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(i);
                    finish();
                    return;
                }
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


    }

    public void onUsersLoaded(List<User> users){
        String username = getUsername();
        for(User u : users){
            u.setContext(this);
            if(u.getName().equals(username)){
                loggedInUser = u;
                TextView nameInSettingsView = (TextView) findViewById(R.id.username_in_settings_text_view);
                nameInSettingsView.setText(loggedInUser.getName());
            }
        }

        populateStaff(users);

        SessionWrapper.UpdateConversations(this, getToken(), new SessionWrapper.OnCompleteListener() {
            public void onComplete(String s) {
                if(s == null){
                    //indicates the http request returned null, and something went wrong. have them login again
                    Intent i = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(i);
                    finish();
                    return;
                }
                List<Conversation> conversations = SessionWrapper.TranslateConversationMetadata(s, userList);

                //called update conversation without the context
                //needs to be connected here now
                for(Conversation c: conversations){
                    c.setContext(MainActivity.this);
                }
                populateConversations(conversations);

                onLoadComplete();
            }

            @Override
            public void onError(String s) {

            }
        });
    }

    public void populateStaff(List<User> users){
        TreeSet<String> departments = new TreeSet<String>();
        for(User u : users){
            // Do not include the dev department
            if(!u.userType.equals("dev")){
                departments.add(u.userType);
            }
        }

        if(departments.size() > 0){
            String curr = departments.first();
            while(curr != null){
                addDepartment(curr);
                curr = departments.higher(curr);
            }
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
            if( ! c.hasUsers()){
                Log.v("tag", c.getName() + ":" + c.getID() + " has no users");
            }
        }
        for(Conversation c : conversations){
            currentConversations.put(c.getID(), c);
            addToNavBar(PRIVATE_MENU_GROUP, c);
        }

        moveOnlineConversationsUp(conversations, PRIVATE_MENU_GROUP);
    }

    public void populateUnread(List<Conversation> conversations){
        for(Conversation c : conversations){
            if( ! c.hasUsers()){
                Log.v("tag", c.getName() + ":" + c.getID() + " has no users");
            }
        }
        for(Conversation c : conversations){
            currentConversations.put(c.getID(), c);
            addToNavBar(UNREAD_MENU_GROUP, c);
        }

        moveOnlineConversationsUp(conversations, UNREAD_MENU_GROUP);
    }

    private void moveOnlineConversationsUp(List<Conversation> conversations, int group){
        for(Conversation c : conversations){
            if(c.hasOnlineUser()){
                Log.v("taggy", "moving up");
                navDrawerAdapter.moveConversationToFirstPosition(group, c);
            }
        }
    }

    public void populateConversations(List<Conversation> conversations){
        List<Conversation> unreadConversations = new ArrayList<Conversation>();
        List<Conversation> privateConversations = new ArrayList<Conversation>();

        for(Conversation conversation : conversations){
            if(conversation.getNumOfUnseen() > 0){
                unreadConversations.add(conversation);
            }else{
                privateConversations.add(conversation);
            }
        }

        populateUnread(unreadConversations);
        populatePrivate(privateConversations);
    }

    private void hideMainElements(){
        findViewById(R.id.loading_logo_image_view).setVisibility(View.VISIBLE);


        getSupportActionBar().hide();
        findViewById(R.id.main_recycler_view_holder).setVisibility(View.GONE);
        findViewById(R.id.message_edit_text).setVisibility(View.GONE);
        findViewById(R.id.message_send_button).setVisibility(View.GONE);
        ((DrawerLayout)findViewById(R.id.drawer_layout)).setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    public void showMainElements(){
        findViewById(R.id.loading_logo_image_view).setVisibility(View.GONE);

        getSupportActionBar().show();
        findViewById(R.id.main_recycler_view_holder).setVisibility(View.VISIBLE);
        findViewById(R.id.message_edit_text).setVisibility(View.VISIBLE);
        findViewById(R.id.message_send_button).setVisibility(View.VISIBLE);
        ((DrawerLayout)findViewById(R.id.drawer_layout)).setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    public void onLoadComplete(){
        setupMainElements();
        //showMainElements();
        if(isNewUser()){
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.openDrawer(GravityCompat.START);
            showMainElements();
        }
    }

    public  void logout(View v){
        SharedPreferences sPref = getSharedPreferences(LoginActivity.PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sPref.edit();
        editor.putString(LoginActivity.TOKEN, null);
        editor.putString(LoginActivity.NAME , null);
        editor.putLong(PREV_CONVERSATION_ID , -1);
        editor.commit();

        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
        finish();
    }

    public void updateMostRecentConversation(long conversationID){
        SharedPreferences sp = getSharedPreferences(LoginActivity.PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong(PREV_CONVERSATION_ID, conversationID);
        editor.commit();
    }

    public void updateUserOnline(final long userID, final boolean isOnline){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(userList.containsKey(userID)){
                    User u = userList.get(userID);
                    u.setIsOnline(isOnline);
                    updateGroup(userID, isOnline, PRIVATE_MENU_GROUP);
                    updateGroup(userID, isOnline, UNREAD_MENU_GROUP);
                    updateGroupStaff(userID, isOnline);
                }
            }
        });

    }

    private void checkForNewMessages(){
        checkCurrentConversationForNewMessages();
        checkAllConversationsForNewMessages();
    }

    private void checkCurrentConversationForNewMessages(){
        if( currentFragment != null && ! currentFragment.isLoading()){
            SessionWrapper.GetConversationData(this, selectedConversation.getID(), getToken(), new SessionWrapper.OnCompleteListener() {
                @Override
                public void onComplete(String s) {
                    if(s == null){
                        //indicates the http request returned null, and something went wrong. have them login again
                        Intent i = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(i);
                        finish();
                        return;
                    }
                    //TODO: Append any new messages to the current conversatoin and update any unread messages
                    currentFragment.updateMessageView(getApplicationContext() ,s, userList);

                }

                @Override
                public void onError(String s) {

                }
            });
        }

    }

    private void checkAllConversationsForNewMessages(){

    }

    private Conversation findConversationWithUser(long userID, int groupID){
        for(int i = 0; i < navDrawerAdapter.getChildrenCount(groupID); i++) {
            NavDrawerItem item = navDrawerAdapter.getChild(groupID, i);

            Conversation conversation = currentConversations.get(item.getID());
            boolean isConversation = true;
            for (int j = 0; j < conversation.getNumberOfUsers(); j++) {
                User userInConv = conversation.getUserAtIndex(j);
                if (userInConv.getID() != loggedInUser.getID() && userInConv.getID() != userID) {
                    isConversation = false;
                }
            }
            if(isConversation){
                return conversation;
            }
        }
        return null;
    }

    private void updateGroup(final long userID, final boolean isOnline, final int groupID){

            Conversation conversation = findConversationWithUser(userID, groupID);

            /*
            if(conversation != null && conversation.v != null){

                ImageView statusImage = (ImageView) conversation.v.findViewById(R.id.nav_row_status_image_view);
                if(statusImage != null) {
                    if (isOnline) {
                        statusImage.setImageResource(R.drawable.online);
                    } else {
                        statusImage.setImageResource(R.drawable.offline);
                    }
                }
            } */
        // Updates are handeled by the navdrawer adapter now
        if(conversation != null){
            navDrawerAdapter.moveConversationToFirstPosition(groupID, conversation);
        }
        drawerListView.invalidateViews();

    }

    private void updateGroupStaff(final long userID, final boolean isOnline){
        /*
        User user = userList.get(userID);
        if(user == null){
            Log.v("taggy", "Couldn't find user");
            return;
        }else{
            Log.v("taggy", "Found user: " + user.getName());
        }
        if(user.v != null){

            ImageView statusImage = (ImageView) user.v.findViewById(R.id.nav_row_status_image_view);
            if(statusImage != null) {
                if (isOnline) {
                    statusImage.setImageResource(R.drawable.online);
                } else {
                    statusImage.setImageResource(R.drawable.offline);
                }
            }
        }
        */
        // Updates are handeled by the navdrawer adapter now
        drawerListView.invalidateViews();

    }

    public void moveConversationToPrivate(Conversation conversation){
        navDrawerAdapter.moveConversationToPrivate(conversation);
        conversation.setNumOfUnseen(0);
        drawerListView.invalidateViews();
    }

    public void moveConversationToUnread(Conversation conversation){
        navDrawerAdapter.moveConversationToUnread(conversation);
        drawerListView.invalidateViews();
    }

    public void scrollToMostRecentMessage(){
        currentFragment.scrollToBottom();
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

    /***************************************************/



    /********** Socket Methods **********/

    private void connectSocket(){
        try{
            Log.v("sockett", "Closing socket!");
            socket = new WebSocket(currentFragment, this);
            setupSSL(this, socket);
        }catch (URISyntaxException e){
            Log.v("sockett", "The socket failed to intially connect: " + e.getMessage());
            socket = null;
        }
    }

    private void closeSocket(){
        if(socket != null){
            Log.v("sockett", "Closing socket!");
            socket.close();
            socket = null;
        }
    }


    private void setupSSL(final Context context, final WebSocket sock){

        Runnable r = new Runnable() {
            @Override
            public void run() {
                try{
                    NeuroSSLSocketFactory neuroSSLSocketFactory = new NeuroSSLSocketFactory(context);
                    org.apache.http.conn.ssl.SSLSocketFactory sslSocketFactory = neuroSSLSocketFactory.createAdditionalCertsSSLSocketFactory();
                    Socket sock1 = new Socket(SessionWrapper.BASE_URL, 443);
                    SSLSocket socketSSL = (SSLSocket) sslSocketFactory.createSocket(sock1, SessionWrapper.BASE_URL, 443, false);


                    sock.setSocket(socketSSL);
                    if(! sock.connectBlocking()){
                        Log.v("sockett", "Failed to connect socket");
                        throw new Exception("Error connecting to the web socket");
                    }else{
                        Log.v("sockett", "Connected");
                    }

                }catch (Exception e){
                    showToast(getResources().getString(R.string.no_connection), Toast.LENGTH_LONG);
                    Log.v("taggy", "There was a problem setting up ssl websocket");
                }
            }
        };

        Thread thread = new Thread(r);
        thread.start();

    }

    private void setupSSLAndSendMessage(final Context context, final WebSocket sock, final String message){

        Runnable r = new Runnable() {
            @Override
            public void run() {
                try{
                    NeuroSSLSocketFactory neuroSSLSocketFactory = new NeuroSSLSocketFactory(context);
                    org.apache.http.conn.ssl.SSLSocketFactory sslSocketFactory = neuroSSLSocketFactory.createAdditionalCertsSSLSocketFactory();
                    Socket sock1 = new Socket(SessionWrapper.BASE_URL, 443);
                    SSLSocket socketSSL = (SSLSocket) sslSocketFactory.createSocket(sock1, SessionWrapper.BASE_URL, 443, false);


                    sock.setSocket(socketSSL);
                    if(! sock.connectBlocking()){
                        Log.v("sockett", "Failed to connect socket");
                        throw new Exception("Error connecting to the web socket");
                    }else{
                        Log.v("sockett", "Connected");
                        sock.pushMessage(message);
                    }

                }catch (Exception e){
                    ((MainActivity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showToast(context.getResources().getString(R.string.no_connection), Toast.LENGTH_LONG);
                        }
                    });
                    Log.v("taggy", "There was a problem setting up ssl websocket");
                }
            }
        };

        Thread thread = new Thread(r);
        thread.start();

    }

    public void pushMessage(String s){
        if(socket == null || socket.isClosed() || ! socket.isOpen()){
            reconnectSocketAndSendMessage(s);
        }else{
            socket.pushMessage(s);
        }
    }

    private void reconnectSocket(){
        try {
            if(socket != null && socket.isOpen()){
                return;
            }
            socket = new WebSocket(currentFragment, this);
            showToast(getResources().getString(R.string.reconnecting_to_server), Toast.LENGTH_SHORT);
            setupSSL(this, socket);
            // TODO Reload messages
        }catch (URISyntaxException e){
            Log.v("taggy","Error with uri when creating socket");
        }
    }


    private void reconnectSocketAndSendMessage(String message){
        try {
            if(socket != null && socket.isOpen()){
                    return;
            }
            socket = new WebSocket(currentFragment, this);
            showToast(getResources().getString(R.string.reconnecting_to_server), Toast.LENGTH_SHORT);
            setupSSLAndSendMessage(this, socket,message);
            // TODO Reload messages
        }catch (URISyntaxException e){
            Log.v("taggy","Error with uri when creating socket");
        }
    }

    public void showToast(String message, int length){
        if (mostRecentToast != null && mostRecentToast.getView().isShown()){
            mostRecentToast.cancel();
        }
        mostRecentToast = Toast.makeText(this, message, length);
        mostRecentToast.show();
    }

    /***************************************************/


    private void print(String s){
        Log.v("tag", s);
    }

    public void toggleSettings(View view) {
        View dropdown = findViewById(R.id.settings_menu_dropdown);
        if(dropdown.getVisibility() == View.GONE){
            dropdown.setVisibility(View.VISIBLE);
        }else{
            dropdown.setVisibility(View.GONE);
        }
    }
}
