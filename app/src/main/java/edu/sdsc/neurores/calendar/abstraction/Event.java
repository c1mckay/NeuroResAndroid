package edu.sdsc.neurores.calendar.abstraction;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by trevor on 4/21/18.
 */

public class Event {
    private String title;
    private Date date;
    private String location;
    private String description;
    private String timeRange;

    public Event(JSONObject jDate) throws JSONException {
        this.title = jDate.getString("title");
        this.date = new Date(jDate.getString("date"));
        if(jDate.has("start") && !jDate.get("start").equals(JSONObject.NULL) && jDate.has("end") && !jDate.get("end").equals(JSONObject.NULL)){
            timeRange = jDate.getString("start") + jDate.getString("end");
        }else if(jDate.has("start") && !jDate.get("start").equals(JSONObject.NULL) )
            timeRange = jDate.getString("start");
        else if(jDate.has("end") && !jDate.get("end").equals(JSONObject.NULL) )
            timeRange = jDate.getString("end");
        else
            timeRange = "";

        if(jDate.has("description") && !jDate.get("description").equals(JSONObject.NULL))
            this.description = jDate.getString("description");
        if(jDate.has("location") && !jDate.get("location").equals(JSONObject.NULL))
            this.location = jDate.getString("location");
    }

    public Event(String title, Date date, String location, String description){
        this.title = title;
        this.date = date;
        this.location = location;
        this.description = description;
    }

    public String getTitle(){
        return title;
    }

    public String getTimeRange(){
        return timeRange;
    }

    public boolean isDayOfWeek(int day){
        return date.getDay() == day;
    }

    public boolean isDate(Date date){
        return date.equals(this.date);
    }

    public boolean isBetween(Date start, Date end){
        return date.after(start) && date.before(end);
    }


    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
