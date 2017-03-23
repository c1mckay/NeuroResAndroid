package com.example.tbpetersen.myapplication;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tbpetersen on 3/23/2017.
 */

public class Conversation {
    long id;
    public List<User> users;

    Conversation(List<User> users, long id){
        this.users = users;
        this.id = id;
    }

    Conversation(long id){
        users = new ArrayList<User>();
        this.id = id;
    }

}
