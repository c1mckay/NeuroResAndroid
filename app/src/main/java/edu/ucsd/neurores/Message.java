package edu.ucsd.neurores;

/**
 * Created by tbpetersen on 2/14/2017.
 */

import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Represents a message from a user. Contains a user's name, message text
 * and the time that the message was sent.
 */
public class Message {
    public static int SHORT = 0;

    private static String SHORT_DATE_FORMAT = "MMM d',' h:mm a";
    private String owner;
    private String messageText;
    private long time;

    /**
     * Public constructor
     * @param owner the owner of the message
     * @param messageText the message text
     * @param time the time the message was sent
     */
    public Message(String owner, String messageText, long time){
        this.owner = owner;
        this.messageText = messageText;
        this.time = time;
    }

    /*****  Getters and setters for variables *****/
    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
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

    public void logMessageInfo(){
        Log.v("tag", "Owner: " + owner + "\n" +
                      "Text: " + messageText + "\n" +
                      "Time: " + time + "\n");
    }
}
