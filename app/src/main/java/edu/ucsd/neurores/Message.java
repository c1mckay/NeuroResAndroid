package edu.ucsd.neurores;

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
        try{
            time = getFormatter().parse(timeStringDBFormat).getTime();
            this.time = time;
        }catch(ParseException e){
            Log.v("error", "time could not be converted: " + timeStringDBFormat);
            this.time = -1;
        }
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
                DateFormat df = new SimpleDateFormat(SHORT_DATE_FORMAT, Locale.getDefault());
                timeString = df.format(new Date(getTime()));
                // Remove leading 0 from single digit days Ex 09->9
                if(timeString.charAt(4) == '0'){
                    timeString = timeString.substring(0,4) + timeString.substring(5);
                }
                break;
        }
        return timeString;
    }

    public String toString(){
        return "Owner: " + sender + "\n" +
                "Text: " + messageText + "\n" +
                "Time: " + time + "\n";
    }

    public String getTimeStringFormattedForDB(){
        return getFormatter().format(new Date(getTime()));
    }

    public static SimpleDateFormat getFormatter(){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        formatter.setTimeZone(TimeZone.getDefault());
        return formatter;
    }

    public void logMessageInfo(){
        Log.v("tag", toString());
    }
}
