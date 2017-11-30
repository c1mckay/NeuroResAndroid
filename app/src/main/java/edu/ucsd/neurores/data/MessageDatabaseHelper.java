package edu.ucsd.neurores.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import edu.ucsd.neurores.abstraction.Conversation;
import edu.ucsd.neurores.helper.JSONConverter;
import edu.ucsd.neurores.abstraction.Message;
import edu.ucsd.neurores.abstraction.User;

/**
 * Created by tbpetersen on 9/11/2017.
 */

// select *  from Table order  by datetime(datetimeColumn) DESC LIMIT 1
public class MessageDatabaseHelper extends SQLiteOpenHelper {
    Context context;

    private static final String MESSAGE_DATABASE = "neurores.db";


    /************ Message Table ******************/
    private static final String MESSAGE_TABLE = "messages_table";

    private static final String COLUMN_MESSAGE_ID = "message_id";
    private static final String COLUMN_TEXT = "text";
    //private static final String COLUMN_CONVERSATION_ID = "conversation_id";
    private static final String COLUMN_SENDER_ID = "sender_id";
    private static final String COLUMN_DATE = "date";

    private static final int MESSAGE_INDEX_MESSAGE_ID = 0;
    private static final int MESSAGE_INDEX_TEXT = 1;
    private static final int MESSAGE_INDEX_CONVERSATION_ID = 2;
    private static final int MESSAGE_INDEX_SENDER = 3;
    private static final int MESSAGE_INDEX_DATE = 4;


    /************ Conversation Table ******************/
    public static final String CONVERSATION_TABLE = "conversations_table";

    public static final String COLUMN_CONVERSATION_ID = "conversation_id";
    public static final String COLUMN_LAST_SEEN = "last_seen";
    public static final String COLUMN_UNSEEN_COUNT = "unseen_count";

    private static final int CONVERSATION_INDEX_CONVERSATION_ID = 0;
    private static final int CONVERSATION_INDEX_LAST_SEEN = 1;
    private static final int CONVERSATION_INDEX_UNSEEN_COUNT = 2;


    /************ User Table ******************/
    public static final String USER_TABLE = "users_table";

    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_USER_TYPE = "user_type";
    public static final String COLUMN_SUPER_USER = "super_user";

    private static final int USER_INDEX_USER_ID = 0;
    private static final int USER_INDEX_EMAIL = 1;
    private static final int USER_INDEX_USER_TYPE = 2;
    private static final int USER_INDEX_SUPER_USER = 3;


    /************ Member Table ******************/
    public static final String MEMBER_TABLE = "members_table";

    public static final int MEMBER_INDEX_USER_ID = 0;
    public static final int MEMBER_INDEX_CONVERSATION_ID = 1;



    public MessageDatabaseHelper(Context context) {
        super(context, MESSAGE_DATABASE, null, 1);
        this.context = context;
        SQLiteDatabase db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createUsersTable(db);
        createCurrentConversationsTable(db);
        createMessagesTable(db);
        createMemberTable(db);
    }

    private void createMemberTable(SQLiteDatabase db) {
        String userID = COLUMN_USER_ID + " INTEGER,";
        String conversationID = COLUMN_CONVERSATION_ID + " INTEGER";


        String columns = userID + conversationID;
        Log.v("mdatabase", "create table " + MEMBER_TABLE + " (" + columns + ")");

        db.execSQL("create table " + MEMBER_TABLE + " (" + columns + ")");
    }


    private void createUsersTable(SQLiteDatabase db) {
        String userID = COLUMN_USER_ID + " INTEGER PRIMARY KEY,";
        String email = COLUMN_EMAIL + " TEXT,";
        String userType = COLUMN_USER_TYPE + " TEXT,";
        String superUser = COLUMN_SUPER_USER + " BOOLEAN";

        String columns = userID + email + userType + superUser ;
        Log.v("mdatabase", "create table " + USER_TABLE + " (" + columns + ")");

        db.execSQL("create table " + USER_TABLE + " (" + columns + ")");
    }

    private void createCurrentConversationsTable(SQLiteDatabase db) {
        String conversationID = COLUMN_CONVERSATION_ID + " INTEGER PRIMARY KEY,";
        String lastSeen = COLUMN_LAST_SEEN + " INTEGER,";
        String unseenCount = COLUMN_UNSEEN_COUNT + " INTEGER";

        String columns = conversationID + lastSeen + unseenCount;
        Log.v("mdatabase", "create table " + CONVERSATION_TABLE + " (" + columns + ")");

        db.execSQL("create table " + CONVERSATION_TABLE + " (" + columns + ")");
    }

    private void createMessagesTable(SQLiteDatabase db) {
        String ID = COLUMN_MESSAGE_ID + " INTEGER PRIMARY KEY,";
        String text = COLUMN_TEXT + " TEXT,";
        String convID = COLUMN_CONVERSATION_ID + " INTEGER,";
        String sender = COLUMN_SENDER_ID + " INTEGER,";
        String date = COLUMN_DATE + " TEXT";

        String columns = ID + text + convID + sender + date;
        Log.v("mdatabase", "create table " + MESSAGE_TABLE + " (" + columns + ")");

        db.execSQL("create table " + MESSAGE_TABLE + " (" + columns + ")");
    }



    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " +  MESSAGE_TABLE);
        db.execSQL("drop table if exists " +  USER_TABLE);
        db.execSQL("drop table if exists " +  CONVERSATION_TABLE);
        db.execSQL("drop table if exists " +  MEMBER_TABLE);

        onCreate(db);
    }

    public boolean insertUser(long userID, String email, String userType, boolean isSuperUser){
        if(alreadyInDatabase(USER_TABLE, COLUMN_USER_ID, userID)){
            return true;
        }

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_USER_ID, userID);
        contentValues.put(COLUMN_EMAIL, email);
        contentValues.put(COLUMN_USER_TYPE, userType);
        contentValues.put(COLUMN_SUPER_USER, isSuperUser);

        long result = db.insert(USER_TABLE, null, contentValues);
        db.close();

        return result != -1;
    }

    public void insertUsers(List<User> userList){
        for(User user : userList){
            insertUser(user.getID(), user.getName(), user.getUserType(), false);
        }
    }


    public boolean insertMessage(long messageID, String text, long convID, long senderID, String date){
        if(alreadyInDatabase(MESSAGE_TABLE, COLUMN_MESSAGE_ID, messageID)){
            return true;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_MESSAGE_ID, messageID);
        contentValues.put(COLUMN_TEXT, text);
        contentValues.put(COLUMN_CONVERSATION_ID, convID);
        contentValues.put(COLUMN_SENDER_ID, senderID);
        contentValues.put(COLUMN_DATE, date);

        long result = db.insert(MESSAGE_TABLE, null, contentValues);
        db.close();

        return result != -1;

    }

    public void insertMessages(List<Message> messageList){
        for(Message message : messageList){
            insertMessage(message.getMessageID(), message.getMessageText(), message.getConversationID(), message.getSenderID(), message.getTimeStringFormattedForDB());
        }
    }

    public void removeAllMessagesInConversation(long conversationID){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(MESSAGE_TABLE, COLUMN_CONVERSATION_ID + "=" + conversationID, null);
    }

    public boolean insertConversation(Conversation conversation){
        return insertConversation(conversation.getID(), conversation.getUserIDs(), -1, conversation.getNumOfUnread());
    }

    public boolean insertConversation(long conversationID, List<Long> memberIDS, long lastSeen, long unseenCount){

        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_CONVERSATION_ID, conversationID);
        contentValues.put(COLUMN_LAST_SEEN, lastSeen);
        contentValues.put(COLUMN_UNSEEN_COUNT, unseenCount);

        long result;
        SQLiteDatabase db;
        if(alreadyInDatabase(CONVERSATION_TABLE, COLUMN_CONVERSATION_ID,conversationID)){
            db = this.getWritableDatabase();
            result = db.update(CONVERSATION_TABLE, contentValues,COLUMN_CONVERSATION_ID + "=" + conversationID, null);
            boolean successfulConversationInsertion = result != -1;

            db.close();
            return  successfulConversationInsertion;
        }else{
            db = this.getWritableDatabase();
            result = db.insert(CONVERSATION_TABLE, null, contentValues);
            boolean successfulConversationInsertion = result != -1;
            boolean successfulMembersInsertion = insertMembers(conversationID, memberIDS);

            db.close();
            return  successfulConversationInsertion && successfulMembersInsertion;
        }



    }

    private boolean insertMembers(long conversationID, List<Long> memberIDS) {
        List<Boolean> results = new ArrayList<Boolean>();

        for(Long memberID : memberIDS){
            boolean insertionSuccess = insertMember(conversationID, memberID);
            results.add(insertionSuccess);
        }

        for(Boolean bool : results){
            if(!bool){
                return false;
            }
        }
        return true;
    }

    private boolean insertMember(long conversationID, Long memberID) {
        //TODO check if member with conversationID is already in db

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_USER_ID, memberID);
        contentValues.put(COLUMN_CONVERSATION_ID, conversationID);

        long result = db.insert(MEMBER_TABLE, null, contentValues);
        db.close();

        return result != -1;
    }

    public User getUser(long userID, Context context){
        SQLiteDatabase db = this.getWritableDatabase();

        String query = "SELECT * FROM " + USER_TABLE + " WHERE " + COLUMN_USER_ID + " = ? LIMIT 1";
        Cursor cursor = db.rawQuery(query, new String[]{Long.toString(userID)});

        if(cursor.getCount() == 0){
            cursor.close();
            db.close();
            return null;
        }

        User user = null;
        if(cursor.moveToNext()){
            String name = cursor.getString(USER_INDEX_EMAIL);
            String userType = cursor.getString(USER_INDEX_USER_TYPE);

            user = new User(context, userID, name, userType, false);
        }
        cursor.close();
        db.close();

        return user;
    }


    public void insertConversations(List<Conversation> list){
        for(Conversation conversation : list){
            insertConversation(conversation.getID(), conversation.getUserIDs(), -1, conversation.getNumOfUnread());
        }
    }

    public List<Conversation> getConversationsList(Context context){
        List<Long> conversationIDs = getAllConversationIDs();
        List<Conversation> conversations = new ArrayList<Conversation>();

        for(long conversationID : conversationIDs){
            Conversation conversation = getConversation(conversationID);
            if(conversation == null){
                continue;
            }
            conversation.setContext(context);
            conversations.add(conversation);
        }
        return conversations;
    }

    private List<Long> getAllConversationIDs(){
        SQLiteDatabase db = this.getWritableDatabase();

        String query = "SELECT * FROM " + CONVERSATION_TABLE;
        Cursor cursor = db.rawQuery(query, null);

        if(cursor.getCount() == 0){
            cursor.close();
            db.close();
            return null;
        }

        List<Long> ids = new ArrayList<Long>();

        while(cursor.moveToNext()){
            long id = cursor.getLong(CONVERSATION_INDEX_CONVERSATION_ID);
            ids.add(id);
        }
        cursor.close();
        db.close();

        return ids;
    }

    private Conversation getConversation(long conversationID){
        SQLiteDatabase db = this.getWritableDatabase();

        String query = "SELECT * FROM " + CONVERSATION_TABLE + " WHERE " + COLUMN_CONVERSATION_ID + " = ? LIMIT 1";
        Cursor cursor = db.rawQuery(query, new String[] {Long.toString(conversationID)});

        if(cursor.getCount() == 0){
            cursor.close();
            db.close();
            return null;
        }

        long lastSeen = -1;
        long unseenCount = -1;
        if(cursor.moveToNext()) {
            lastSeen = cursor.getLong(CONVERSATION_INDEX_LAST_SEEN);
            unseenCount = cursor.getLong(CONVERSATION_INDEX_UNSEEN_COUNT);
        }
        cursor.close();
        db.close();

        List<Long> memberIDS = getMemberIDSInConversation(conversationID);
        Conversation conversation = new Conversation(conversationID,null);
        conversation.setNumOfUnread(unseenCount);

        for(Long memberID : memberIDS){
            User current = getUser(memberID, context);
            if(current != null){
                conversation.addUser(current);
            }
        }

        return conversation;
    }

    public void makeDatabaseMatchMessageList(long conversationID, List<Message> messageList){
        removeAllMessagesInConversation(conversationID);
        insertMessages(messageList);
    }

    public String getMessagesJSON(long conversationID){
        SQLiteDatabase db = this.getWritableDatabase();

        String query = "SELECT * FROM " + MESSAGE_TABLE + " WHERE " + COLUMN_CONVERSATION_ID + " = ? ORDER BY " + COLUMN_MESSAGE_ID + " ASC";
        Cursor cursor = db.rawQuery(query, new String[] {Long.toString(conversationID)});

        if(cursor.getCount() == 0){
            cursor.close();
            db.close();
            return "[]";
        }
        List<Message> list = new ArrayList<Message>();

        while(cursor.moveToNext()){
            long messageID = cursor.getLong(MESSAGE_INDEX_MESSAGE_ID);
            String text = cursor.getString(MESSAGE_INDEX_TEXT);
            long senderID = cursor.getLong(MESSAGE_INDEX_SENDER);
            String date = cursor.getString(MESSAGE_INDEX_DATE);

            Message message = new Message(messageID, conversationID, senderID, text, date);
            list.add(message);
        }

        cursor.close();

        return JSONConverter.toJSON(list);
    }

    public List<Long> getMemberIDSInConversation(long conversationID){
        SQLiteDatabase db = this.getWritableDatabase();
        List<Long> memberIDS = new ArrayList<Long>();

        String query = "SELECT * FROM " + MEMBER_TABLE + " WHERE " + COLUMN_CONVERSATION_ID + " = ?" ;
        Cursor cursor = db.rawQuery(query, new String[] {Long.toString(conversationID)});

        if(cursor.getCount() == 0){
            return memberIDS;
        }
        while(cursor.moveToNext()){
            long messageID = cursor.getLong(MEMBER_INDEX_USER_ID);
            memberIDS.add(messageID);
        }

        cursor.close();

        return memberIDS;
    }

    public String getUserListJSON(){
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor  cursor = db.rawQuery("SELECT * FROM " + USER_TABLE, null);


        if(cursor.getCount() == 0){
            cursor.close();
            db.close();
            return null;
        }
        List<User> list = new ArrayList<User>();

        while(cursor.moveToNext()){
            long userID = cursor.getLong(USER_INDEX_USER_ID);
            String email = cursor.getString(USER_INDEX_EMAIL);
            String userType = cursor.getString(USER_INDEX_USER_TYPE);


            User user = new User(userID, email, userType);
            list.add(user);
        }

        cursor.close();
        return JSONConverter.toJSON(list);
    }

    public boolean databaseIsEmpty(){
        SQLiteDatabase db = this.getWritableDatabase();

        String query = "SELECT * FROM " + USER_TABLE ;
        Cursor cursor = db.rawQuery(query,null);
        int count = cursor.getCount();
        cursor.close();
        db.close();

        return count == 0;
    }

    public List<User> getUserList(){
        return JSONConverter.toUserList(getUserListJSON(), context);
    }

    private boolean alreadyInDatabase(String tableName, String columnName, long whereValue){
        SQLiteDatabase db = this.getWritableDatabase();

        String query = "SELECT * FROM " + tableName + " WHERE " + columnName + " = ?";
        Cursor cursor = db.rawQuery(query, new String[] {Long.toString(whereValue)});

        int count = cursor.getCount();
        cursor.close();
        db.close();

        return count > 0;
    }

}
