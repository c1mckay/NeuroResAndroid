package edu.sdsc.neurores.calendar;

/**
 * Created by trevor on 4/21/18.
 */

public class Event {
    private String title;
    private String timeRange;
    private String location;
    private String description;

    public Event(String title, String timeRange, String location, String description){
        this.title = title;
        this.timeRange = timeRange;
        this.location = location;
        this.description = description;
    }

    public String getTitle(){
        return title;
    }

    public String getTimeRange(){
        return timeRange;
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
