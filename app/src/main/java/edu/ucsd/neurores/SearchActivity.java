package edu.ucsd.neurores;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Overview of this activity: This activity is started by the main activity when the search icon is
 * clicked form the navigation drawer. Once this activity starts, it will request a list of all
 * users from the server. Once the list of users is returned by the server, the data is parsed
 * and stored in the hashmap users. An adapter is used to display the data, which is all the users
 * that the current user can start a conversation with. This list that the user interacts with is
 * populated with userArrayList (also populated when the server responds with the list of users).
 *
 * The user can filter the users that are shown to them by clicking on the search icon on the right
 * of the action bar. The user can tap on any of the displayed users to start a conversation with
 * them. Once tapped, a request is sent to the sever to create a conversation with the tapped on
 * user and the conversation id and the id of all users in the conversation are returned in the
 * server response (if a conversation already exists, the old conversation id and users are
 *  returned; a new conversation is not started). The conversation id and user ids are returned to
 *  the main activity in the intent and this activity ends
 */
public class SearchActivity extends AppCompatActivity {

    ContactSearchAdapter contactSearchAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        final ListView listView = (ListView) findViewById(R.id.usersSearchList);
        final ArrayList<String> userArrayList = new ArrayList<String>();

        // Hash map of all users. Key is the user name and department e.g. "tbpetersen (dev)"
        final HashMap<String, User> users = new HashMap<String, User>();
        // Token for calls to server
        final String token = getIntent().getStringExtra("token");
        // Get all the users. Update the arrayList "users". Set up the search adapter
        SessionWrapper.UpdateUsers(getApplicationContext(), token, new SessionWrapper.OnCompleteListener() {
            @Override
            public void onComplete(String s) {
                try {
                    // An array of all users
                    JSONArray jArray = new JSONArray(s);
                    //Iterate over all users and create user objects and add to users
                    for(int i = 0; i < jArray.length(); i++){
                        JSONObject jObject = (jArray.getJSONObject(i));
                        String name = (String) jObject.get("email");
                        String depart = (String) jObject.get("user_type");
                        long id = jObject.getLong("user_id");
                        String displayInfo = name + " (" + depart + ")";
                        userArrayList.add(displayInfo);
                        users.put(displayInfo, new User(id, name, depart));
                    }

                    //Sort alphabetically and add search adapter
                    Collections.sort(userArrayList, String.CASE_INSENSITIVE_ORDER);
                    contactSearchAdapter = new ContactSearchAdapter(SearchActivity.this,
                            android.R.layout.simple_list_item_1,
                            userArrayList);

                    listView.setAdapter(contactSearchAdapter);

                    // Create a conversation for the clicked on user. End this activity if successful
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position,
                                                long id) {
                            // The text that the user clicked on e.g. "tbpetersen (dev)"
                            String displayInfo = listView.getItemAtPosition(position).toString();
                            // Find the user
                            User user = users.get(displayInfo);
                            //Create the conversation and return from this activity
                            if(user != null){
                                ArrayList<Long> usersInConv = new ArrayList<Long>();
                                usersInConv.add(user.getID());
                                SessionWrapper.CreateConversation(getApplicationContext(), usersInConv, token, new SessionWrapper.OnCompleteListener() {
                                    @Override
                                    public void onComplete(String s) {
                                        try{
                                            JSONObject jsonObject = new JSONObject(s);
                                            long conversationID = jsonObject.getLong("conv_id");
                                            JSONArray userIDs = jsonObject.getJSONArray("user_ids");
                                            // Get the ids from the array
                                            long[] userIDArray = new long[userIDs.length()];
                                            for(int i = 0; i < userIDArray.length; i++){
                                                userIDArray[i] = userIDs.getLong(i);
                                            }
                                            Intent intent = new Intent(SearchActivity.this, MainActivity.class);
                                            intent.putExtra("CONVERSATION_ID", conversationID);
                                            intent.putExtra("USERS_IDS", userIDArray);
                                            setResult(Activity.RESULT_OK, intent);
                                            finish();
                                        }catch(Exception e){
                                            onError("Failed conversion from JSON string to JSON object");
                                        }
                                    }

                                    @Override
                                    public void onError(String s) {
                                        // An error occurred, return as cancelled
                                        Intent intent = new Intent(SearchActivity.this, MainActivity.class);
                                        setResult(Activity.RESULT_CANCELED, intent);
                                        finish();
                                    }
                                });

                            }else{
                                // An error occurred, return as cancelled
                                Intent intent = new Intent(SearchActivity.this, MainActivity.class);
                                setResult(Activity.RESULT_CANCELED, intent);
                                finish();
                            }

                        }
                    });
                }catch(Exception e){
                    userArrayList.add("There was an error loading data from the server");

                    Collections.sort(userArrayList, String.CASE_INSENSITIVE_ORDER);
                    contactSearchAdapter = new ContactSearchAdapter(SearchActivity.this,
                            android.R.layout.simple_list_item_1,
                            userArrayList);

                    listView.setAdapter(contactSearchAdapter);
                }
            }

            @Override
            public void onError(String s) {
                userArrayList.add("There was an error loading data from the server");
            }
        });

        //userArrayList.addAll(Arrays.asList(getResources().getStringArray(R.array.user_list)));




    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.action_search_search_activity).getActionView();
        searchView.setIconifiedByDefault(true);
        searchView.setFocusable(true);
        searchView.setIconified(true);


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener(){

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                contactSearchAdapter.filterData(newText);
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        onBackPressed();

        return super.onOptionsItemSelected(item);
    }
}
