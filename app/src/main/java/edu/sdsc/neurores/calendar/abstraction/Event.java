package edu.sdsc.neurores.calendar.abstraction;


import java.util.Calendar;
import java.util.Date;

/**
 * Created by trevor on 4/21/18.
 */

public class Event implements Comparable<Event>{
    private String title;
    private Date date;
    private String location;
    private String description;
    private String startTime, endTime;
    private int startHour, startMinute;

    public Event(String title, Date date, String startTime, String endTime, String location, String description){
        this.title = title;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.description = description;

        if(startTime != null){
            String[] startTimeSplit = startTime.split(":");
            startHour = Integer.parseInt(startTimeSplit[0]);
            startMinute = Integer.parseInt(startTimeSplit[1]);
        }else{
            startHour = -1;
            startMinute = -1;
        }

    }

    public String getTitle(){
        return title;
    }

    public String getTimeRange(){
        if(startTime == null && endTime == null){
            return "";
        }

        if(hasNoEndTime()){
            return "Starts at " + parseTime(startTime);
        }else{
            return parseTime(startTime) + "-" + parseTime(endTime);
        }
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

    public String getStartTime(){
        return startTime;
    }

    public String getEndTime(){
        return endTime;
    }

    private int getStartHour(){
        return startHour;
    }

    private int getStartMinute(){
        return startMinute;
    }

    private static String parseTime(String time){
        String[] timeArray = time.split(":");

        String hour = timeArray[0];
        String min = timeArray[1];

        int hourAsInt = Integer.parseInt(hour);

        if(hourAsInt < 12) {
            return hourAsInt + ":" + min + "a";
        }else if(hourAsInt == 12){
            return hourAsInt + ":" + min + "p";
        }else{
            return (hourAsInt -12) + ":" + min + "p";
        }

    }

    private boolean hasNoEndTime(){
        return endTime == null;
    }

    @Override
    public int compareTo(Event event) {
        Date date1 = getDate();
        Date date2 = event.getDate();

        int dateCompare = date1.compareTo(date2);
        if(dateCompare == 0){
            return compareStartTimes(event);
        }else{
            return dateCompare;
        }
    }

    private int compareStartTimes(Event event) {
        if(getStartHour() < event.getStartHour()){
            return -1;
        }else if(getStartHour() > event.getStartHour()){
            return 1;
        }else{
            return 0;
        }
    }

}
