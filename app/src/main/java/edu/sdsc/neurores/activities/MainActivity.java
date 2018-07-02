package edu.sdsc.neurores.activities;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.stetho.Stetho;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import edu.sdsc.neurores.abstraction.Conversation;
import edu.sdsc.neurores.abstraction.Group;
import edu.sdsc.neurores.adapters.NavDrawerAdapter;
import edu.sdsc.neurores.abstraction.NavDrawerItem;
import edu.sdsc.neurores.fragments.CalendarFragment;
import edu.sdsc.neurores.fragments.PDFFragment;
import edu.sdsc.neurores.R;
import edu.sdsc.neurores.abstraction.User;
import edu.sdsc.neurores.helper.ActionOpenPDF;
import edu.sdsc.neurores.helper.ActionViewCalendar;
import edu.sdsc.neurores.helper.FormatHelper;
import edu.sdsc.neurores.network.WebSocket;
import edu.sdsc.neurores.data.MessageDatabaseHelper;
import edu.sdsc.neurores.fragments.MainFragment;
import edu.sdsc.neurores.helper.JSONConverter;
import edu.sdsc.neurores.helper.OnCompleteListener;
import edu.sdsc.neurores.network.HTTPRequestCompleteListener;
import edu.sdsc.neurores.network.RequestWrapper;

//TODO Handle the errors in HTTP calls
//TODO Handle case where token is set in prefs but username is not (otherwise there are null ptrs)
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, DrawerLayout.DrawerListener, ExpandableListView.OnGroupClickListener, ExpandableListView.OnChildClickListener {

    public static final int UNREAD_MENU_GROUP = 0;
    public static final int STAFF_MENU_GROUP = 1;
    public static final int PRIVATE_MENU_GROUP = 2;

    public static final String PREV_CONVERSATION_ID = "previousConversationID";
    public static final String CONVERSATION_ID = "conv_id";
    public static final String CALENDAR_FLAG = "start_time";

    private static final int TYPE_PDF = 0;
    private static final int TYPE_CAL = 1;


    Toolbar toolbar = null;
    //  This adapter controls the Navigation Drawer's views and data
    private NavDrawerAdapter navDrawerAdapter;
    // The list view in the Navigation Drawer
    private ExpandableListView drawerListView;
    // The fragment that holds the messages of the selected user
    private Fragment currentFragment;
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
    private Conversation previousConversation;


    public User loggedInUser;
    Toast mostRecentToast;

    private WebSocket webSocket;
    private BroadcastReceiver screenStateReceiver;
    boolean isPaused;
    boolean screenIsOn;
    int nonMessageType;
    String queuedToastMessage;
    private TextView toolbarTitle;
    private LinearLayout warningBanner;
    private int calendarDayOffset;
    String pdfFilename = PDFFragment.HANDBOOK_FILE_NAME;

    MessageDatabaseHelper messageDatabaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeVariables();

        if(getToken() == null ||  (! isConnectedToNetwork() && messageDatabaseHelper.databaseIsEmpty())){
            goToLogin();
            return;
        }

        setUp();
        logFireBaseToken();
    }

    private void setUp(){
        registerReceiverForScreen();
        setupToolbar();
        initializeDrawer();
        addDrawerLinks();
        loadData();
    }

    private void addDrawerLinks() {
        addDrawerLink(R.drawable.calendar, getResources().getString(R.string.calendar), new ActionViewCalendar(this));
        addDrawerLink(R.drawable.open_book, getResources().getString(R.string.handbook), new ActionOpenPDF(this, PDFFragment.HANDBOOK_FILE_NAME));
        addDrawerLink(R.drawable.clipboard, getResources().getString(R.string.clinics_sessions), new ActionOpenPDF(this, PDFFragment.CLINIC_SESSIONS_FILE_NAME));
    }

    private void addDrawerLink(int drawableID, String linkText, View.OnClickListener clickListener){
        ViewGroup navDrawerLinkHolder = (ViewGroup) findViewById(R.id.nav_drawer_link_holder);
        LayoutInflater layoutInflater = LayoutInflater.from(this);

        ViewGroup linkGroup = (ViewGroup) layoutInflater.inflate(R.layout.nav_drawer_link_group, navDrawerLinkHolder, false);
        ImageView imageView = (ImageView) linkGroup.findViewById(R.id.link_image_view);
        TextView textView = (TextView) linkGroup.findViewById(R.id.link_text_view);

        imageView.setImageDrawable(getResources().getDrawable(drawableID));
        imageView.setContentDescription(linkText);
        textView.setText(linkText);

        linkGroup.setOnClickListener(clickListener);

        navDrawerLinkHolder.addView(linkGroup);
    }

    private void initializeDrawer() {
        drawerListView = (ExpandableListView) findViewById(R.id.nav_view);
        navDrawerAdapter = new NavDrawerAdapter(this);
        drawerListView.setAdapter(navDrawerAdapter);
        drawerListView.setOnGroupClickListener(this);
        drawerListView.setOnChildClickListener(this);

        for (int i = 0; i < navDrawerAdapter.getGroupCount(); i++){
            drawerListView.expandGroup(i);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        drawer.addDrawerListener(this);
        toggle.syncState();
    }

    private void setupToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            //getSupportActionBar().setDisplayShowTitleEnabled(true);
            //getSupportActionBar().setTitle(getResources().getString(R.string.welcome));
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            toolbarTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
            toolbarTitle.setText(getResources().getString(R.string.welcome));
        }
    }

    private void initializeVariables() {
        isPaused = false;
        screenIsOn = true;
        queuedToastMessage = null;
        calendarDayOffset = 0;
        currentConversations = new HashMap<>();
        userList = new HashMap<>();
        messageDatabaseHelper = new MessageDatabaseHelper(this);
        previousConversation = null;

        //This is used to view the sql data base by going to chrome://inspect on a browser
        Stetho.initializeWithDefaults(this);


        warningBanner = (LinearLayout) findViewById(R.id.warning_banner);
        warningBanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBannerClicked();
            }
        });
    }

    private void onBannerClicked() {

        connectWebSocket(new OnCompleteListener() {
            @Override
            public void onComplete(String s) {
                hideMainElements();
                updateNavDrawer();
                reloadCurrentFragment();
            }

            @Override
            public void onError(String s) {
                showToast(getResources().getString(R.string.no_connection), Toast.LENGTH_LONG);
                closeWebSocket();
            }
        });
    }

    private void logFireBaseToken() {
        if(FirebaseInstanceId.getInstance().getToken() != null)
            Log.d("token", FirebaseInstanceId.getInstance().getToken());
    }

    private void registerReceiverForScreen() {
       screenStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch(intent.getAction()){
                    case Intent.ACTION_SCREEN_ON:
                        onScreenTurnedOn();
                        break;
                    case Intent.ACTION_SCREEN_OFF:
                        onScreenTurnedOff();
                        break;
                }

            }
        };
        IntentFilter screenStateFilter = new IntentFilter();
        screenStateFilter.addAction(Intent.ACTION_SCREEN_ON);
        screenStateFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(screenStateReceiver, screenStateFilter);
    }

    private void onScreenTurnedOn(){
        Log.v("sockett","Screen On");
        screenIsOn = true;
        if(! isPaused){
            connectWebSocket();
            reloadCurrentFragment();
            showQueuedToast();
        }
    }

    private void onScreenTurnedOff(){
        Log.v("webSocket","Screen Off");
        screenIsOn = false;
    }

    private void showQueuedToast(){
        if(queuedToastMessage != null){
            showToast(queuedToastMessage, Toast.LENGTH_SHORT);
            queuedToastMessage = null;
        }
    }

    private void unregisterReceiverForScreen(){
        try{
            unregisterReceiver(screenStateReceiver);
        }catch (IllegalArgumentException e){
            // Receiver is not registered. Not a problem
        }
    }

    private boolean isConnectedToNetwork() {
        final ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();

        return activeNetwork != null && activeNetwork.isConnected();
    }

    public void checkServerIsOnline(HTTPRequestCompleteListener onCompleteListener){
        final Context context = this;
        RequestWrapper.checkServerIsOnline(this,  onCompleteListener);
    }

    @Override
    protected void onPause() {
        isPaused = true;
        closeWebSocket();
        //hideMainElements();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        closeWebSocket();
        unregisterReceiverForScreen();
        super.onDestroy();
    }

    public void setupFragmentAndSocket(){
        invalidateNavigationDrawer();
        setInitialFragment();
        connectWebSocket();
    }

    public String getToken(){
        SharedPreferences sPref = getSharedPreferences(LoginActivity.PREFS, Context.MODE_PRIVATE);
        return sPref.getString(LoginActivity.TOKEN, null);
    }


    protected void clearToken(){
        SharedPreferences sp = getSharedPreferences(LoginActivity.PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(LoginActivity.TOKEN);
        editor.commit();
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
        previousConversation = null;

        MainFragment mFrag = new MainFragment();
        Bundle i = new Bundle();
        i.putString("token", getToken());
        mFrag.setArguments(i);
        return mFrag;
    }

    private PDFFragment startPDFFragment(String pdfFileName){
        PDFFragment pdfFrag = new PDFFragment();
        Bundle i = new Bundle();
        i.putString(PDFFragment.KEY_TOKEN, getToken());
        i.putString(PDFFragment.KEY_FILE_NAME, pdfFileName);
        pdfFrag.setArguments(i);
        return pdfFrag;
    }

    private CalendarFragment startCalendarFragment(){
        CalendarFragment calendarFragment = new CalendarFragment();
        Bundle i = new Bundle();
        i.putString("token", getToken());
        i.putInt(CalendarFragment.CALENDAR_DAY_OFFSET, calendarDayOffset);
        calendarDayOffset = 0;
        calendarFragment.setArguments(i);
        return calendarFragment;
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
        } else if(previousConversation != null){
            onConversationClick(previousConversation.getViewInNavDrawer(), previousConversation.getID());
        }else{
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
        switch (id){
            case R.id.action_wipe_thread:
                if(currentFragment instanceof MainFragment && selectedConversation != null){
                    wipeAlert();
                }else{
                   Log.v("taggy", "Conversation wipe requested when currentFragment is not MainFragment or selected conversation is null");
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        isPaused = false;
        // If the returning activity was the search activity

        if(requestCode == SEARCH_USER_REQUEST && ! isConnectedToNetwork()){
            showToast(getResources().getString(R.string.reconnect_to_start_conversation), Toast.LENGTH_LONG);
            return;
        }

        if(requestCode == SEARCH_USER_REQUEST && resultCode == Activity.RESULT_OK){

            // Get the username and id of the newly searched user
            // TODO Change this string to be a constant in the search activity
            long searchedID = data.getLongExtra("CONVERSATION_ID", -1);
            long[] userIDs = data.getLongArrayExtra("USERS_IDS");
            if(searchedID != -1 && userIDs.length > 0){

                // Deselect the previously selected user (change the background
                // color and selectedUser)
                if(selectedConversation != null){
                    selectedConversation.select();
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
                            Log.v("warning", "Error: User with id of " + l + " was said to be in conv "
                                    + searchedID + " but was not found in list of users");
                        }
                    }
                    currentConversations.put(searchedID, newConversation);
                    selectedConversation = newConversation;
                }

                // Tell the main activity that the fragment needs to be changed
                needToChangeFragment = true;
            }
        }

    }

    @Override
    protected void onResume() {
        Log.v("taggy", "On resume called");
        hideMainElements();
        isPaused = false;

        super.onResume();


        if(isConnectedToNetwork()){
            hideWarningBanner();
        }else{
            showMainElements();
            showWarningBanner();
        }

        // Check if the main fragment needs to be changed
        if(needToChangeFragment){
            if(selectedConversation.getViewInNavDrawer() == null) {
                // Add a view to the navigation bar for the new user
                addToNavBar(PRIVATE_MENU_GROUP, selectedConversation);
            }else{
                // Highlight the view that is already in the nav bar
                selectedConversation.select();
                //selectedUser.viewInNavDrawer.setBackgroundColor(getResources().getColor(R.color.selected));
            }
            changeFragment();
            needToChangeFragment = false;
        }else if(currentFragment != null){
            updateNavDrawer();
            reloadCurrentFragment();
        }
        connectWebSocket();

        hideSoftKeyboard();
    }

    public MessageDatabaseHelper getMessageDatabaseHelper(){
        return messageDatabaseHelper;
    }

    /**
     * The method called when the search button in the nav drawer is clicked.
     * Starts the search activity "SearchActivity" for a result
     * @param view the view that was clicked on
     */
    public void searchOnClick(View view) {
        if(isConnectedToNetwork()){
            Intent startSearch = new Intent(MainActivity.this, SearchActivity.class);
            startSearch.putExtra("token", getToken());
            startActivityForResult(startSearch, SEARCH_USER_REQUEST);
        }else{
            showToast(getString(R.string.reconnect_search), Toast.LENGTH_LONG);
        }
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
        for(Conversation conversation: currentConversations.values()){
            if(conversation.getNumberOfUsers() == 1 && conversation.getUserAtIndex(0).getID() == user_id){
                onConversationClick(conversation.getViewInNavDrawer(), conversation.getID());
                return;
            }
        }
        ArrayList<Long> userIDs = new ArrayList<>();
        userIDs.add(user_id);
        createConversation(userIDs,PRIVATE_MENU_GROUP , true, 0);
    }

    public void wipeAlert(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Delete messages with " + selectedConversation.getName() + "?");

        alertDialogBuilder
                .setMessage(R.string.wipe_messages_message)
                .setCancelable(true)
                .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        wipeConversation(selectedConversation.getID(), true);
                    }
                })
                .setNegativeButton("No",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void wipeConversation(long conversationID, boolean alertServer){
        if(currentFragment instanceof MainFragment && ((MainFragment)currentFragment).conversation.getID() == conversationID){
            ((MainFragment)currentFragment).wipeConversation(alertServer);
        }

        messageDatabaseHelper.removeAllMessagesInConversation(conversationID);
        updateNavDrawer();
    }

    private void createConversation(List<Long> userIDs, final int groupID, final boolean changeFragment, final int numOfUnseen){
        RequestWrapper.CreateConversation(this, userIDs, getToken(), new HTTPRequestCompleteListener() {

            public void onComplete(String s) {
                if(s == null){
                    //indicates the http request returned null, and something went wrong. have them login again
                    Intent i = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(i);
                    finish();
                    return;
                }

                try {
                    //TODO Use jsonconverter
                    JSONObject jo = new JSONObject(s);
                    JSONArray users = jo.getJSONArray("user_ids");
                    long id = jo.getLong("conv_id");

                    Conversation conversation = new Conversation(id, MainActivity.this);
                    for(int i = 0; i < users.length(); i++){
                        id = users.getLong(i);
                        if(id != loggedInUser.getID())
                            conversation.addUser(userList.get(id));
                    }
                    conversation.setNumOfUnread(numOfUnseen);
                    currentConversations.put(conversation.getID(), conversation);
                    Log.v("taggy", "Adding to nav bar");
                    addToNavBar(groupID, conversation);

                    long conversationID = conversation.getID();
                    List<Long> members = conversation.getUserIDs();
                    long unseen = conversation.getNumOfUnread();
                    messageDatabaseHelper.insertConversation(conversationID, members, -1, unseen);

                    if(changeFragment){
                        onConversationClick(conversation.getViewInNavDrawer(), conversation.getID());
                    }
                    Log.v("taggy", "done creating");

                } catch (JSONException e) {
                    Log.v("taggy", "Fail");
                    Log.v("taggy",  e.getMessage() + "");
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(int i) {
                Log.v("taggy", "Error creating conversation");
                if(i == 401){
                    showToast(getString(R.string.cred_expired), Toast.LENGTH_LONG);
                    logout(null);
                    return;
                }
                showToast(getString(R.string.reconnect_to_start_conversation), Toast.LENGTH_LONG);
            }
        });
    }

    public void onNewConversationDetected(final long conversationID){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                RequestWrapper.UpdateConversations(getApplication(), getToken(), new HTTPRequestCompleteListener() {
                    @Override
                    public void onComplete(String s) {
                        if(s == null){
                            //indicates the http request returned null, and something went wrong. have them login again
                            Intent i = new Intent(MainActivity.this, LoginActivity.class);
                            startActivity(i);
                            finish();
                            return;
                        }
                        List<Conversation> conversations = JSONConverter.toConversationList(s, userList);

                        // Find the newly detected conversation
                        Conversation newConversation = null;
                        for(Conversation c: conversations){
                            if(c.getID() == conversationID){
                                newConversation = c;
                            }
                        }

                        if(newConversation == null){
                            Log.v("error ","conversation with ID of " + conversationID + " was not found in the list of conversations");
                            return;
                        }
                        Log.v("taggy", "new conversation found! Creating it");
                        createConversation(newConversation.getUserIDs(), UNREAD_MENU_GROUP, false, 1);
                    }

                    @Override
                    public void onError(int i) {
                        if(i == 401){
                            showToast(getString(R.string.cred_expired), Toast.LENGTH_LONG);
                            logout(null);
                            return;
                        }
                        Log.v("taggy", "Error updating conversations");
                        logout(null);
                    }
                });
            }
        });

    }

    private void onConversationClick(View v, long conversation_id){
        if(selectedConversation != null && selectedConversation.getID() == conversation_id){
            closeDrawer();
            return;
        }



        if(currentConversations.containsKey(conversation_id)){ //a conversation was clicked, and we're about to load it
            //Deselect previous
            if(selectedConversation != null){
                selectedConversation.deselect();
            }
            // Select clicked on user
            selectedConversation = currentConversations.get(conversation_id);;
            selectedConversation.setViewInNavDrawer(v);
            Log.v("taggy", "Selecting the new conversation " + selectedConversation.toString());
            selectedConversation.select();
        }
        // Reset the input fields and hide it
        hideSoftKeyboard();

        changeFragment();

    }

    /**
     * Add a view to the navigation drawer for the newUser
     * @param conversation the user to have a view added for
     */
    private void addToNavBar(int groupID, Conversation conversation){
        navDrawerAdapter.addConversation(groupID, conversation);
        drawerListView.invalidateViews();
    }

    /**
     * Change the messages in the fragment to be the messages of selectedUser
     */
    private void changeFragment(){

        if(isConnectedToNetwork()){
            hideMainElements();
        }

        if(selectedConversation != null){
            currentFragment = startMainFragment();

            MainFragment mainFragment = (MainFragment) currentFragment;

            mainFragment.conversation = selectedConversation;
            mainFragment.userName = loggedInUser.getName();
            android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, currentFragment);
            fragmentTransaction.commitAllowingStateLoss();
            mainFragment.loadMessages(this, selectedConversation, userList);
            toolbarTitle.setText(selectedConversation.getName());
            RecyclerView recyclerView = (RecyclerView)findViewById(R.id.recycler_view);
            if(recyclerView != null){
                recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
            }
            updateMostRecentConversation(selectedConversation.getID());
        }else{
            if(nonMessageType == TYPE_PDF){
                Log.v("taggy", "Showing pdf");
                currentFragment = startPDFFragment(pdfFilename);
                if(pdfFilename.equals(PDFFragment.HANDBOOK_FILE_NAME)){
                    toolbarTitle.setText(getString(R.string.handbook));
                }else{
                    toolbarTitle.setText(getString(R.string.clinics_sessions));
                }
            }else{
                Log.v("taggy", "Showing cal");
                currentFragment = startCalendarFragment();
                toolbarTitle.setText(getString(R.string.calendar));
            }

            android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, currentFragment);
            fragmentTransaction.commitAllowingStateLoss();
            showMainElements();
        }


        closeDrawer();
        updateFrag();
    }

    private void changeFragment(Fragment newFragment){
        currentFragment = newFragment;




        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, currentFragment);
        fragmentTransaction.commitAllowingStateLoss();
        showMainElements();
    }

    private void closeDrawer() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    private void reloadCurrentFragment(){
        if(currentFragment != null){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(selectedConversation == null){
                        showMainElements();
                    }else{
                        hideMainElements();
                        changeFragment();
                        drawerListView.invalidateViews();
                    }

                }
            });
        }
    }

    private void updateFrag(){
        if(webSocket != null){
            webSocket.updateFrag(currentFragment);
        }
    }

    public int daysBetween(Date d1, Date d2){
        Calendar c1 = Calendar.getInstance();
        c1.setTime(d1);
        c1.set(Calendar.HOUR_OF_DAY,0);
        c1.set(Calendar.HOUR,0);
        c1.set(Calendar.MINUTE,0);
        c1.set(Calendar.SECOND,0);
        c1.set(Calendar.AM_PM, Calendar.AM);


        Calendar c2 = Calendar.getInstance();
        c2.setTime(d2);
        c2.set(Calendar.HOUR_OF_DAY, 0);
        c2.set(Calendar.HOUR, 0);
        c2.set(Calendar.MINUTE, 0);
        c2.set(Calendar.SECOND, 0);
        c2.set(Calendar.AM_PM, Calendar.AM);

        Log.v("calendar", "Original Date: " + d2);
        Log.v("calendar", "Dates: " + c1.getTime() + " " + c2.getTime());

        long differenceInMilliseconds = c2.getTime().getTime() -  c1.getTime().getTime();

        Log.v("calendar", "Float: " + ((float)(differenceInMilliseconds) / (1000 * 60 * 60 * 24)));
        return Math.round(((float)(differenceInMilliseconds) / (1000 * 60 * 60 * 24)));
    }

    /**
     * Change the messages in the fragment to be the messages of selectedUser
     */
    private void setInitialFragment(){

        if(isNewUser()){
            currentFragment = loadOnboardingFragment();
            android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, currentFragment);
            fragmentTransaction.commitAllowingStateLoss();
            return;
        }

        Intent intent = getIntent();

        if(getIntent().hasExtra(CALENDAR_FLAG)){
            String eventStartTime = getIntent().getStringExtra(CALENDAR_FLAG);
            //SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat simpleDateFormat = FormatHelper.getDatabaseDateFormatter();
            try{
                Date startDate = simpleDateFormat.parse(eventStartTime);
                Date today = new Date();
                calendarDayOffset = daysBetween(today, startDate);
                Log.v("calendar", "Calendar offset set to " + calendarDayOffset);
            } catch (ParseException e) {
                e.printStackTrace();
                Log.v("calendar", "Failed to parse date: " + eventStartTime);
            }

            Log.v("calendar", "Viewing cal");
            viewCalendar(null);
            return;
        }

        long conversationID = getConversationIDForInitialLoad();



        selectedConversation = currentConversations.get(conversationID);
        if(selectedConversation == null){

            if(hasPreviousConversation()){
                setPreviousConversationID(-1);
                setInitialFragment();
                return;
            }

            Log.v("warning", "User has ongoing conversations and no previous conversation but cannot load the first ongoing conversation");
            logout(null);
            finish();
            return;
        }

        assert currentFragment instanceof MainFragment;

        currentFragment = startMainFragment();

        MainFragment mainFragment = (MainFragment) currentFragment;

        mainFragment.conversation = selectedConversation;
        mainFragment.userName = loggedInUser.getName();
        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, mainFragment);
        fragmentTransaction.commitAllowingStateLoss();
        mainFragment.loadMessages(this, selectedConversation, userList);
        if(getSupportActionBar() != null){
            toolbarTitle.setText(selectedConversation.getName());
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        invalidateNavigationDrawer();
    }

    private long getConversationIDForInitialLoad(){
        if(getIntent().hasExtra(CONVERSATION_ID)){
            //long lastConversationID = getIntent().getLongExtra(CONVERSATION_ID, -1);
            long lastConversationID = Long.valueOf(getIntent().getStringExtra(CONVERSATION_ID));
            setPreviousConversationID(lastConversationID);
            Log.v("mynotif", "previous set to: " + lastConversationID);
        }else{
            Log.v("mynotif", "Could not find ID in intent");
        }


        if(hasPreviousConversation()){
            return getPreviousConversationID();
        }else if( hasOngoingConversations()){
            return getFirstConversationID();
        }else{
            return -1;
        }

        /*
        if(hasOngoingConversations() && ! hasPreviousConversation()){
            Log.v("taggy", "Has ongoing");
            return getFirstConversationID();
        }else{
            Log.v("taggy", "Got previous");
            return getPreviousConversationID();
        }
        */


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
            NavDrawerItem item = navDrawerAdapter.getChild(getGroupPosition(PRIVATE_MENU_GROUP), 0);
            return item.getID();
        }else{
         return -1;
        }
    }

    private long getFirstUnreadConversationID(){
        if(getNumOfUnreadConversations() > 0){
            NavDrawerItem item = navDrawerAdapter.getChild(getGroupPosition(UNREAD_MENU_GROUP), 0);
            return item.getID();
        }else{
            return -1;
        }
    }

    private int getNumOfPrivateConversations(){
        return navDrawerAdapter.getChildrenCount(getGroupPosition(PRIVATE_MENU_GROUP));
    }

    private int getNumOfUnreadConversations(){
        return navDrawerAdapter.getChildrenCount(getGroupPosition(UNREAD_MENU_GROUP));
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

    public boolean isNewUser(){
        return (! hasPreviousConversation()) && (! hasOngoingConversations());
    }

    /**
     * Hides the soft keyboard
     */
    public void hideSoftKeyboard() {
        if(getCurrentFocus()!= null) {
            InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }


    private void addDepartment(String name){
        navDrawerAdapter.addDepartment(name);
    }

    private void addUserToDepartment(String departmentName, User newUser){
        navDrawerAdapter.addUserToDepartment(departmentName,newUser);
        userList.put(newUser.getID(), newUser);
    }

    private void loadData(){
        final MainActivity mainActivity = this;
        hideMainElements();
        // Load data from server
        RequestWrapper.UpdateUsers(this, getToken(), new HTTPRequestCompleteListener() {

            public void onComplete(String s) {
                if(s == null){
                    goToLogin();
                    return;
                }
                Log.v("taggy", "Token: " + getToken());
                Log.v("taggy", s);
                List<User> users = JSONConverter.toUserList(s, mainActivity);
                messageDatabaseHelper.insertUsers(users);
                onUsersLoaded(users);
            }


            public void onError(int i) {
                Log.v("taggy", "Error while loading data");
                if(i == 401){
                    showToast(getString(R.string.cred_expired), Toast.LENGTH_LONG);
                    logout(null);
                    return;
                }else if(messageDatabaseHelper.getUserListJSON() != null){
                    List<User> users = messageDatabaseHelper.getUserList();
                    for(User u: users){
                        u.setContext(MainActivity.this);
                    }
                    onUsersLoaded(users);
                    return;
                }
                logout(null);
            }
        });
    }

    private void updateNavDrawer(){
        RequestWrapper.UpdateConversations(this, getToken(), new HTTPRequestCompleteListener() {
            public void onComplete(final String s) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(s == null){
                            goToLogin();
                            return;
                        }
                        List<Conversation> conversations = JSONConverter.toConversationList(s, userList);

                        List<Conversation> newConversations = new ArrayList<Conversation>();
                        for( Conversation conversation : conversations){

                            conversation.setContext(MainActivity.this);

                            Conversation oldConversation = currentConversations.get(conversation.getID());
                            if(oldConversation == null){
                                newConversations.add(conversation);
                            }else{
                                oldConversation.setNumOfUnread(conversation.getNumOfUnread());
                                if(oldConversation.hasUnreadMessages()){
                                    moveConversationToUnread(oldConversation);
                                }else{
                                    moveConversationToPrivate(oldConversation);
                                }
                            }

                        }

                        messageDatabaseHelper.insertConversations(conversations);
                        populateUnread(newConversations);
                        moveAllOnlineConversationsUp();
                    }
                });


            }

            @Override
            public void onError(int i) {
                Log.v("taggy", "Error  updating nav drawer");
                if(i == 401){
                    showToast(getString(R.string.cred_expired), Toast.LENGTH_LONG);
                    logout(null);
                    return;
                }
                showMainElements();
                //logout(null);
            }
        });
    }

    public void onUsersLoaded(List<User> users){
        String username = getUsername();
        for(User u : users){
            u.setContext(this);
            if(u.getName().equals(username)){
                loggedInUser = u;
                setNameInNavDrawer();
            }
        }

        populateStaff(users);
        initializeConversations();
    }

    private void initializeConversations() {
        final MainActivity mainActivity = this;
        RequestWrapper.UpdateConversations(this, getToken(), new HTTPRequestCompleteListener() {
            public void onComplete(String s) {
                if(s == null){
                    goToLogin();
                    return;
                }
                List<Conversation> conversations = JSONConverter.toConversationList(s, userList);

                //called update conversation without the context
                //needs to be connected here now
                for(Conversation c: conversations){
                    c.setContext(MainActivity.this);
                }
                populateConversations(conversations);
                messageDatabaseHelper.insertConversations(conversations);

                onLoadComplete();
            }

            @Override
            public void onError(int i) {
                Log.v("taggy", "Error initializing conversations");
                if(i == 401){
                    showToast(getString(R.string.cred_expired), Toast.LENGTH_LONG);
                    logout(null);
                    return;
                }
                List<Conversation> conversations = messageDatabaseHelper.getConversationsList(mainActivity);
                if(conversations != null){
                    populateConversations(conversations);
                    onLoadComplete();
                }
            }
        });
    }

    private void setNameInNavDrawer() {
        TextView nameInSettingsView = (TextView) findViewById(R.id.username_in_settings_text_view);
        nameInSettingsView.setText(loggedInUser.getName());
    }

    public void populateStaff(List<User> users){
        TreeSet<String> departments = new TreeSet<String>();
        for(User u : users){
            // Do not include the dev department, unless you are dev or super user
            if(u.getUserType() != null &&!u.getUserType().equals("dev")){
                departments.add(u.getUserType());
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
                addUserToDepartment(u.getUserType(), u);
            }
        }

    }

    public void populatePrivate(List<Conversation> conversations){
        for(Conversation c : conversations){
            if( ! c.hasUsers()){
                Log.v("warning", c.getName() + ":" + c.getID() + " has no users");
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
                Log.v("warning", c.getName() + ":" + c.getID() + " has no users");
            }
        }
        for(Conversation conversation : conversations){
            currentConversations.put(conversation.getID(), conversation);
            addToNavBar(UNREAD_MENU_GROUP, conversation);
        }

        moveOnlineConversationsUp(conversations, UNREAD_MENU_GROUP);
    }

    private void moveOnlineConversationsUp(List<Conversation> conversations, int groupID){
        for(Conversation c : conversations){
            if(c.hasOnlineUser()){
                navDrawerAdapter.moveConversationToFirstPosition(groupID, c);
            }
        }
    }

    private void moveAllOnlineConversationsUp(){
        moveOnlineConversationsUp(navDrawerAdapter.getOnlineInGroup(UNREAD_MENU_GROUP), UNREAD_MENU_GROUP);
        moveOnlineConversationsUp(navDrawerAdapter.getOnlineInGroup(PRIVATE_MENU_GROUP), PRIVATE_MENU_GROUP);
    }

    public void populateConversations(List<Conversation> conversations){
        List<Conversation> unreadConversations = new ArrayList<Conversation>();
        List<Conversation> privateConversations = new ArrayList<Conversation>();

        for(Conversation conversation : conversations){
            if(conversation.getNumOfUnread() > 0){
                unreadConversations.add(conversation);
            }else{
                privateConversations.add(conversation);
            }
        }

        populateUnread(unreadConversations);
        populatePrivate(privateConversations);
    }

    private void hideMainElements(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hideSoftKeyboard();
                findViewById(R.id.loading_logo_image_view).setVisibility(View.VISIBLE);

                if(getSupportActionBar() != null){
                    getSupportActionBar().hide();
                }
                findViewById(R.id.main_recycler_view_holder).setVisibility(View.GONE);
                ((DrawerLayout)findViewById(R.id.drawer_layout)).setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            }
        });

    }

    public void hideWarningBanner(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                warningBanner.setVisibility(View.GONE);
            }
        });
    }

    public void showWarningBanner(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                warningBanner.setVisibility(View.VISIBLE);
            }
        });
    }

    public void showMainElements(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.loading_logo_image_view).setVisibility(View.GONE);

                if(getSupportActionBar() != null){
                    getSupportActionBar().show();
                }
                findViewById(R.id.main_recycler_view_holder).setVisibility(View.VISIBLE);
                ((DrawerLayout)findViewById(R.id.drawer_layout)).setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            }
        });

    }

    public void onLoadComplete(){
        setupFragmentAndSocket();
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
        //editor.putString(LoginActivity.NAME , null);
        editor.putLong(PREV_CONVERSATION_ID , -1);
        closeWebSocket();
        editor.commit();

        goToLogin();
    }

    public  void viewPDF(String pdfFilename){
        if(currentFragment instanceof PDFFragment && this.pdfFilename.equals(pdfFilename)){
            closeDrawer();
            return;
        }

        this.pdfFilename = pdfFilename;

        if(selectedConversation != null){
            previousConversation = selectedConversation;
            selectedConversation.deselect();
            selectedConversation = null;
        }
        nonMessageType = TYPE_PDF;
        changeFragment();
    }

    public  void viewCalendar(View v){
        if(currentFragment instanceof CalendarFragment){
            closeDrawer();
            return;
        }

        if(selectedConversation != null){
            previousConversation = selectedConversation;
            selectedConversation.deselect();
            selectedConversation = null;
        }
        nonMessageType = TYPE_CAL;
        changeFragment();
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
                    updateGroup(userID, PRIVATE_MENU_GROUP);
                    updateGroup(userID, UNREAD_MENU_GROUP);
                    updateGroupStaff(userID, isOnline);
                }
                moveAllOnlineConversationsUp();
            }
        });

    }

    private Conversation findConversationWithUser(long userID, int groupID){
        for(int i = 0; i < navDrawerAdapter.getChildrenCount(getGroupPosition(groupID)); i++) {
            NavDrawerItem item = navDrawerAdapter.getChild(getGroupPosition(groupID), i);

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

    private void updateGroup(final long userID, final int groupID){
        if(! navDrawerAdapter.groupIsVisible(groupID)){
            return;
        }
        Conversation conversation = findConversationWithUser(userID, groupID);

        if(conversation != null){
            navDrawerAdapter.moveConversationToFirstPosition(groupID, conversation);
        }
        drawerListView.invalidateViews();

    }

    private void updateGroupStaff(final long userID, final boolean isOnline){
        drawerListView.invalidateViews();

    }

    public void moveConversationToPrivate(final Conversation conversation){
        navDrawerAdapter.moveConversationToPrivate(conversation);
        conversation.setNumOfUnread(0);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                drawerListView.invalidateViews();
            }
        });

    }

    public void moveConversationToUnread(final Conversation conversation){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                navDrawerAdapter.moveConversationToUnread(conversation);
                drawerListView.invalidateViews();
            }
        });
    }

    public void dismissNotifications(long conversationID){
        if(! isPaused){
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel((int) conversationID);
        }
    }

    public boolean screenIsOn(){
        return screenIsOn;
    }

    public void queueToast(String s){
        queuedToastMessage = s;
    }


    /***** Methods for listening for the navigation drawer opening/closing *****/

    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {
        hideSoftKeyboard();
    }

    @Override
    public void onDrawerOpened(View drawerView) {

    }

    @Override
    public void onDrawerClosed(View drawerView) {

    }

    @Override
    public void onDrawerStateChanged(int newState) {

    }

    /***************************************************/

    private void connectWebSocket(OnCompleteListener ocl){
        closeWebSocket();
        webSocket = new WebSocket(currentFragment, this, ocl);
    }

    private void connectWebSocket(){
        closeWebSocket();
        webSocket = new WebSocket(currentFragment, this, new OnCompleteListener() {
            @Override
            public void onComplete(String s) {



                hideWarningBanner();
                if(webSocket != null){
                    Log.v("taggy", "Websocket is open: " + webSocket.isOpen());
                }else{
                    onError("Socket is null on onComplete of connectWebSocket");
                }
            }

            @Override
            public void onError(String s) {
                showWarningBanner();
                closeWebSocket();
            }
        });
    }

    private void closeWebSocket(){
        if(webSocket != null){
            Log.v("sockett", "Closing webSocket!");
            webSocket.close();
            webSocket = null;
        }
    }

    public void pushMessage(final String message){
        if(! (currentFragment instanceof MainFragment)){
            Log.v("taggy", "Trying to push message when current fragment is not MainFragment");
            return;
        }

        final MainFragment mainFragment = (MainFragment) currentFragment;

        if(webSocket == null || webSocket.isClosed() || ! webSocket.isOpen()){
            Log.v("sockett", "Socket is not in working condition while trying to send message. Reconnecting and resending message");
            connectWebSocket(new OnCompleteListener() {
                @Override
                public void onComplete(String s) {
                    webSocket.pushMessage(message);
                    mainFragment.clearMessage();
                }

                @Override
                public void onError(String s) {
                    showToast(getResources().getString(R.string.no_connection), Toast.LENGTH_LONG);
                }
            });
        }else{
            if(currentFragment == null){
                Log.v("sockett", "currentfragment is null when trying to send message");
                return;
            }
            webSocket.pushMessage(message);
            mainFragment.clearMessage();

        }
    }

    /***************************************************/

    private void goToLogin(){
        closeWebSocket();
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
        finish();
    }


    public void showToast( final String message,final int length){
        //TODO Make custom toast
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(! isPaused){
                    if (mostRecentToast != null && mostRecentToast.getView().isShown()){
                        mostRecentToast.cancel();
                    }
                    mostRecentToast = Toast.makeText(MainActivity.this, message, length);
                    mostRecentToast.show();
                }
            }
        });

    }

    public void toggleSettings(View view) {
        View dropdown = findViewById(R.id.settings_menu_dropdown);
        if(dropdown.getVisibility() == View.GONE){
            dropdown.setVisibility(View.VISIBLE);
        }else{
            dropdown.setVisibility(View.GONE);
        }
    }

    public void openZoom(View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://uchealth.zoom.us/j/3329671357"));
        startActivity(browserIntent);
    }

    public void printNavDrawer(){
        navDrawerAdapter.printLists();
    }

    public int getGroupPosition(int groupID){
        return navDrawerAdapter.getGroupPosition(groupID);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //No call for super(). Bug on API Level > 11.
    }

    public void invalidateNavigationDrawer(){
        drawerListView.invalidateViews();
    }


    @Override
    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
        Group group = navDrawerAdapter.getGroup(groupPosition);
        group.setIsExpanded(! group.isExpanded());
        navDrawerAdapter.dataSetChanged();
        return false;
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        onViewClicked(v);
        return true;
    }

    public void onSocketDisconnected(){
        Log.v("taggy", "!! disconnected webSocket !!");
        if(! isPaused){
            showWarningBanner();
            //showDisconnectMessage();
            //forceSocketReconnect();
        }else{
            Log.v("sockett", "activity is paused, not connecting webSocket");
        }
    }

    private void showDisconnectMessage() {
        if(currentFragment instanceof MainFragment){
            MainFragment current = (MainFragment) currentFragment;
            current.addMessage("NeuroRes",getString(R.string.disconnected_message), System.currentTimeMillis(), true);
        }
    }

    /**************************************************
     * Dev functions
     */


    public void saveBadToken(View v){
        SharedPreferences sp = getSharedPreferences(LoginActivity.PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(LoginActivity.TOKEN, "This_is_a_bad_login_token");
        editor.commit();
    }


    public void logDB(View v){
        Log.v("taggy", selectedConversation.toString() );
    }
}