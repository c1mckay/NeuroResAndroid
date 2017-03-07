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
        implements NavigationView.OnNavigationItemSelectedListener, ExpandableListView.OnChildClickListener{

    Toolbar toolbar = null;
    private NavDrawerAdapter navDrawerAdapter;
    private ExpandableListView drawerListView;
    private EditText messageEditText;
    private MainFragment currentFragment;
    private int SEARCH_USER_REQUEST = 1;

    private boolean needToChangeFragment = false;

    private HashMap<Long,User> currentConversations;
    public User selectedUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize
        currentConversations = new HashMap<Long, User>();

        // Set the fragment
        currentFragment = new MainFragment();
        currentFragment.addUser("User1");
        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, currentFragment);
        fragmentTransaction.commit();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(false);

        drawerListView = (ExpandableListView) findViewById(R.id.nav_view);
        navDrawerAdapter = new NavDrawerAdapter(this);
        drawerListView.setAdapter(navDrawerAdapter);
        drawerListView.setOnChildClickListener(this);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        messageEditText = (EditText) findViewById(R.id.message_edit_text);

        final Button messageSendButton = (Button) findViewById(R.id.message_send_button);
        messageSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newMessage = messageEditText.getText().toString();
                if(! newMessage.equals("")){
                    currentFragment.addMessage("Trevor", messageEditText.getText().toString());
                    messageEditText.setText("");
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                    messageEditText.clearFocus();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        if(1==1) return true;
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if(id != R.id.search){
            //selectedItem = item;
        }


        if (id == 123) {
            // Set the fragment
            currentFragment = new MainFragment();
            currentFragment.addUser("User1");
            android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, currentFragment);
            fragmentTransaction.commit();
        } else if (id == 456) {
            // Set the fragment
            currentFragment = new MainFragment();
            currentFragment.addUser("User2");
            android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, currentFragment);
            fragmentTransaction.commit();
        } else if (id == 789) {
            // Set the fragment
            currentFragment = new MainFragment();
            currentFragment.addUser("User3");
            android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, currentFragment);
            fragmentTransaction.commit();
        } else if (id == 111) {
            // Set the fragment
            currentFragment = new MainFragment();
            currentFragment.addUser("User4");
            android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, currentFragment);
            fragmentTransaction.commit();
        }else if(id != R.id.search){
            currentFragment = new MainFragment();
            currentFragment.addUser(item.getTitle().toString());
            android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, currentFragment);
            fragmentTransaction.commit();
            currentFragment.queueMessage(item.getTitle().toString(), "This is the old message");
        }else if(id == R.id.search){
            Intent startSearch = new Intent(MainActivity.this, SearchActivity.class);
            startActivityForResult(startSearch, SEARCH_USER_REQUEST);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount() - 1);

        return true;

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == SEARCH_USER_REQUEST && resultCode == Activity.RESULT_OK){
            String searchedUsername = data.getStringExtra("USERNAME");
            long searchedID = data.getIntExtra("ID", -1);

            if(currentConversations.get(searchedID) == null){
                selectedUser = new User(searchedID, searchedUsername);
                currentConversations.put(selectedUser.id, selectedUser);
            }else{
                if(selectedUser != null){
                    selectedUser.v.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                }
                selectedUser = currentConversations.get(searchedID);
            }
            needToChangeFragment = true;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void loadMessages(String username){
                currentFragment = new MainFragment();
                currentFragment.queueMessage(username, "This is a new comment from me!");
                android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, currentFragment);
                fragmentTransaction.commit();
    }

    @Override
    protected void onResume() {
        if(needToChangeFragment){
            if(selectedUser.v == null) {
                addConversation(selectedUser);
            }else{
                selectedUser.v.setBackgroundColor(getResources().getColor(R.color.selected));
            }
            changeFragment();
            needToChangeFragment = false;
        }
        super.onResume();
    }

    public void searchOnClick(View view) {
        Intent startSearch = new Intent(MainActivity.this, SearchActivity.class);
        startActivityForResult(startSearch, SEARCH_USER_REQUEST);
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        for(User u : currentConversations.values()){
            if(u.v == v){
                selectedUser.v.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                selectedUser = u;
                selectedUser.v.setBackgroundColor(getResources().getColor(R.color.selected));
                break;
            }
        }

        changeFragment();
        return true;
    }

    private void addConversation(User newUser){
        navDrawerAdapter.addConversation(0, newUser);
        drawerListView.expandGroup(0);
    }

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
}
