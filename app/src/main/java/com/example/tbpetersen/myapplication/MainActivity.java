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
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    NavigationView navigationView = null;
    Toolbar toolbar = null;
    private List<Message> messageList = new ArrayList<Message>();
    private RecyclerView recyclerView;
    private MessageAdapter messageAdapter;
    private EditText messageEditText;
    private MainFragment currentFragment;
    private int SEARCH_USER_REQUEST = 1;
    private int FAKE_ID = 22;

    private int currentID;

    private String newFragmentUser = "";
    private boolean changeFragment = false;

    private SubMenu subMenuPrivate;
    private SubMenu subMenuUnread;
    private SubMenu subMenuStaff;

    private MenuItem selectedItem = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Set the fragment
        currentFragment = new MainFragment();
        currentFragment.addUser("User1");
        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, currentFragment);
        fragmentTransaction.commit();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(false);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        messageEditText = (EditText) findViewById(R.id.message_edit_text);

        final Button messageSendButton = (Button) findViewById(R.id.message_send_button);
        messageSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentFragment.addMessage("Trevor",messageEditText.getText().toString());
                messageEditText.setText("");
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
                messageEditText.clearFocus();
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
        // Inflate the menu; this adds items to the action bar if it is present.
        Menu navMenu = navigationView.getMenu();
        getMenuInflater().inflate(R.menu.activity_main_drawer, navMenu);
        getMenuInflater().inflate(R.menu.main, menu);

        setupNavigationMenu(navMenu);

        return true;
    }

    public void setupNavigationMenu(Menu menu){
        navigationView.setItemIconTintList(null);
        SubMenu root = menu.addSubMenu(Menu.NONE, R.id.root_menu, 0, "");
        menu.setGroupCheckable(R.id.root_menu, false, true);

        MenuItem searchbar = menu.add(R.id.root_menu, R.id.search, 0, R.string.search_label);
        searchbar.setIcon(R.drawable.ic_zoom_in_black_24dp);

        subMenuUnread = menu.addSubMenu(R.id.root_menu, R.id.unread_menu, 0, R.string.unread_menu_title);
        subMenuStaff = menu.addSubMenu(R.id.root_menu, R.id.staff_menu, 1, R.string.staff_menu_title);
        subMenuPrivate = menu.addSubMenu(R.id.root_menu, R.id.private_menu, 2, R.string.private_menu_title);

        menu.setGroupCheckable(R.id.staff_menu, false, true);
        menu.setGroupCheckable(R.id.unread_menu, false, true);
        menu.setGroupCheckable(R.id.private_menu, false, true);

        MenuItem item1 = subMenuStaff.add(R.id.staff_menu, 123, 0, "User 1");
        item1.setIcon(R.drawable.online);
        MenuItem item2 = subMenuStaff.add(R.id.staff_menu, 456, 0, "User 2");
        item2.setIcon(R.drawable.offline);
        MenuItem item3 = subMenuPrivate.add(R.id.private_menu, 789, 0, "User 3");
        item3.setIcon(R.drawable.offline);
        MenuItem item4 = subMenuUnread.add(R.id.private_menu, 111, 0, "User 4");
        item4.setIcon(R.drawable.notify);


        item1.setCheckable(true);
        item2.setCheckable(true);
        item3.setCheckable(true);
        item4.setCheckable(true);
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
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if(id != R.id.search){
            deselectCurrent();
            selectedItem = item;
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
            changeFragment = true;
            newFragmentUser = data.getStringExtra("USERNAME");
            currentID = data.getIntExtra("ID", -1);
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
        if(changeFragment){
            MenuItem menuItem = navigationView.getMenu().findItem(currentID);
            if(menuItem == null) {
                deselectCurrent();
                loadMessages(newFragmentUser);
                MenuItem newlyAdded = subMenuPrivate.add(R.id.private_menu, currentID, Menu.FIRST, newFragmentUser);
                newlyAdded.setIcon(R.drawable.online);
                newlyAdded.setCheckable(true);
                newlyAdded.setChecked(true);
                selectedItem = newlyAdded;
            }else{
                selectItem(menuItem);
            }
            changeFragment = false;
            newFragmentUser = "";
        }
        super.onResume();
    }

    private void deselectCurrent(){
        if(selectedItem != null) {
            selectedItem.setChecked(false);
        }
    }

    private void selectItem(MenuItem item){
        deselectCurrent();
        selectedItem = item;
        item.setChecked(true);
        currentFragment = new MainFragment();
        loadMessages(item.getTitle().toString());
    }
}
