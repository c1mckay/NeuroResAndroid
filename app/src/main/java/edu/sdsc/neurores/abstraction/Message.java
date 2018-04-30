package edu.sdsc.neurores.abstraction;

/**
 * Created by tbpetersen on 2/14/2017.
 */

import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import edu.sdsc.neurores.helper.FormatHelper;

/**
 * Represents a message from a user. Contains a user's name, message text
 * and the time that the message was sent.
 */
public class Message {
    public static int SHORT = 0;

    private static String SHORT_DATE_FORMAT = "MMM d',' h:mm a";
    private String sender;
    private String messageText;
    private long time;
    private long messageID;
    private long conversationID;
    private long senderID;

    public Message(String sender, String messageText, long time){
        this.sender = sender;
        this.messageText = messageText;
        this.time = time;
        messageID = -1;
        conversationID = -1;
        senderID = -1;
    }

    public Message(long messageID, long conversationID, long senderID, String messageText, long time){
        this.messageID = messageID;
        this.senderID = senderID;
        this.messageText = messageText;
        this.time = time;
        this.conversationID = conversationID;
        this.sender = "unknown";
    }

    public Message(long messageID, long conversationID, long senderID, String messageText, String timeStringDBFormat){
        this.messageID = messageID;
        this.senderID = senderID;
        this.messageText = messageText;
        this.conversationID = conversationID;
        this.sender = "unknown";

        long time;
        time = getMillisecondsFromTimeString(timeStringDBFormat);
        this.time = time;
    }

    /*****  Getters and setters for variables *****/
    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public long getSenderID(){
        return senderID;
    }

    public long getMessageID(){
        return messageID;
    }

    public long getConversationID(){
        return conversationID;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void appendText(String text){
        messageText += ("\n" + text);
    }

    public String getTimeString(int timeLength){
        String timeString;
        switch(timeLength){
            default:
                //DateFormat df = new SimpleDateFormat(SHORT_DATE_FORMAT, Locale.getDefault());
                //df.setTimeZone(TimeZone.getDefault());

                DateFormat custom = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault());
                custom.setTimeZone(TimeZone.getDefault());

                timeString = custom.format(new Date(getTime()));
                break;
        }
        return timeString;
    }

    public String toString(){
        return "Owner: " + sender + "\n" +
                "Text: " + messageText + "\n" +
                "Time: " + time + "\n";
    }

    public static String getTimeStringFormattedForDB(long time){
        SimpleDateFormat formatter = FormatHelper.getDatabaseDateFormatter();
        return formatter.format(new Date(time));
    }

    public String getTimeStringFormattedForDB(){
        return FormatHelper.getDatabaseDateFormatter().format(new Date(getTime()));
    }

    public static long getMillisecondsFromTimeString(String timeString){
        SimpleDateFormat formatter = FormatHelper.getDatabaseDateFormatter();
        try{
            return formatter.parse(timeString).getTime();
        }catch (ParseException e){
            Log.v("taggy", "Error gettinh time from timestring: " + timeString + " " + e.getMessage());
            return 0;
        }
    }


    public void logMessageInfo(){
        Log.v("tag", toString());
    }
}
