package edu.ucsd.neurores;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

import static com.google.gson.FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES;

/**
 * Created by tbpetersen on 9/11/2017.
 */

// select *  from Table order  by datetime(datetimeColumn) DESC LIMIT 1
public class MessageDatabaseHelper extends SQLiteOpenHelper {
    Context context;

    private static final String MESSAGE_DATABASE = "messages.db";


    private static final String MESSAGE_TABLE = "messages_table";

    private static final String COLUMN_MESSAGE_ID = "message_id";
    private static final String COLUMN_TEXT = "text";
    private static final String COLUMN_CONV_ID = "conv_id";
    private static final String COLUMN_SENDER_ID = "sender_id";
    private static final String COLUMN_DATE = "date";

    private static final int INDEX_MESSAGE_ID = 0;
    private static final int INDEX_TEXT = 1;
    private static final int INDEX_CONV_ID = 2;
    private static final int INDEX_SENDER = 3;
    private static final int INDEX_DATE = 4;

    public static final String CONVERSATION_TABLE = "conversations_table";
    //TODO Add column names and indexes. Add methods for inserting and getting. Use the methods

    public static final String USER_TABLE = "users_table";

    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_USER_TYPE = "user_type";
    public static final String COLUMN_SUPER_USER = "super_user";

    private static final int INDEX_USER_ID = 0;
    private static final int INDEX_EMAIL = 1;
    private static final int INDEX_USER_TYPE = 2;
    private static final int INDEX_SUPER_USER = 3;


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
    }

    private void createMessagesTable(SQLiteDatabase db) {
        String ID = COLUMN_MESSAGE_ID + " INTEGER PRIMARY KEY,";
        String text = COLUMN_TEXT + " TEXT,";
        String convID = COLUMN_CONV_ID + " INTEGER,";
        String sender = COLUMN_SENDER_ID + " INTEGER,";
        String date = COLUMN_DATE + " TEXT";

        String columns = ID + text + convID + sender + date;
        Log.v("mdatabase", "create table " + MESSAGE_TABLE + " (" + columns + ")");

        db.execSQL("create table " + MESSAGE_TABLE + " (" + columns + ")");
    }



    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " +  MESSAGE_TABLE);
        onCreate(db);
    }

    public boolean insertUser(long userID, String email, String userType, boolean isSuperUser){
        if(userIsAlreadyInDatabase(userID)){
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
            insertUser(user.getID(), user.getName(), user.userType, false);
        }
    }

    public boolean insertMessage(long id, String text, long convID, long senderID, String date){

        if(messageIsAlreadyInDatabase(id)){
            return true;
        }

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_MESSAGE_ID, id);
        contentValues.put(COLUMN_TEXT, text);
        contentValues.put(COLUMN_CONV_ID, convID);
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

    public String getConversationJSON(long conversationID){
        SQLiteDatabase db = this.getWritableDatabase();

        String query = "SELECT * FROM " + MESSAGE_TABLE + " WHERE " + COLUMN_CONV_ID + " = ? ORDER BY " + COLUMN_MESSAGE_ID + " ASC";
        Cursor cursor = db.rawQuery(query, new String[] {Long.toString(conversationID)});

        if(cursor.getCount() == 0){
            return null;
        }
        List<Message> list = new ArrayList<Message>();

        while(cursor.moveToNext()){
            long messageID = cursor.getLong(INDEX_MESSAGE_ID);
            String text = cursor.getString(INDEX_TEXT);
            long senderID = cursor.getLong(INDEX_SENDER);
            String date = cursor.getString(INDEX_DATE);

            Message message = new Message(messageID, conversationID, senderID, text, date);
            list.add(message);
        }

        cursor.close();

        return JSONConverter.messageListToJSON(list);
    }

    public String getUserListJSON(){
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor  cursor = db.rawQuery("SELECT * FROM " + USER_TABLE, null);


        if(cursor.getCount() == 0){
            return null;
        }
        List<User> list = new ArrayList<User>();

        while(cursor.moveToNext()){
            long userID = cursor.getLong(INDEX_USER_ID);
            String email = cursor.getString(INDEX_EMAIL);
            String userType = cursor.getString(INDEX_USER_TYPE);


            User user = new User(userID, email, userType);
            list.add(user);
        }

        cursor.close();
        return JSONConverter.userListToJSON(list);
    }

    private boolean messageIsAlreadyInDatabase(long messageID){
        SQLiteDatabase db = this.getWritableDatabase();

        String query = "SELECT * FROM " + MESSAGE_TABLE + " WHERE " + COLUMN_MESSAGE_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[] {Long.toString(messageID)});

        int count = cursor.getCount();
        cursor.close();
        db.close();

        return count == 1;
    }

    private boolean userIsAlreadyInDatabase(long userID){
        SQLiteDatabase db = this.getWritableDatabase();

        String query = "SELECT * FROM " + USER_TABLE + " WHERE " + COLUMN_USER_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[] {Long.toString(userID)});

        int count = cursor.getCount();
        cursor.close();
        db.close();

        return count == 1;
    }

}
