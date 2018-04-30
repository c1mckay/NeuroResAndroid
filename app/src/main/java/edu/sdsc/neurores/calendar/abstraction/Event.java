package edu.sdsc.neurores.calendar.abstraction;

import java.util.Date;

/**
 * Created by trevor on 4/21/18.
 */

public class Event {
    private String title;
    private Date date;
    private String location;
    private String description;

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
        return "TODO";
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

    public Date getDate(){
        return date;
    }
}
