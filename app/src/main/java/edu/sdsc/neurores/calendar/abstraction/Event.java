package edu.sdsc.neurores.calendar.abstraction;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by trevor on 4/21/18.
 */

public class Event implements Comparable<Event>{
    private String title;
    private String location;
    private String description;
    private Calendar start, end;

    public Event(String title, Calendar start, Calendar end, String location, String description){
        this.title = title;
        this.start = start;
        this.end = end;
        this.location = location;
        this.description = description;
    }

    public String getTitle(){
        return title;
    }

    public String getTimeRange(){
        if(!hasStartTime()){
            return "";
        }

        if(!hasEndTime()){
            return startTimeOnly();
        }else if(isSingleDayEvent()){
            return singleDayTime();
        }else{
            return multiDayTime();
        }
    }

    private String singleDayTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("h:mma", Locale.getDefault());

        String startTime = simpleDateFormat.format(start.getTime());
        String endTime = simpleDateFormat.format(end.getTime());

        return startTime + " - " + endTime;
    }

    private String multiDayTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("M'/'d h:mma", Locale.getDefault());

        String startTime = simpleDateFormat.format(start.getTime());
        String endTime = simpleDateFormat.format(end.getTime());

        return startTime + " - " + endTime;
    }

    private String startTimeOnly() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("h:mma", Locale.getDefault());
        String startTime = simpleDateFormat.format(start.getTime());


        return "Starts at " + startTime;
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

    public Calendar getStart(){
        return start;
    }

    public Calendar getEnd(){
        return end;
    }

    private boolean hasStartTime(){
        return start != null;
    }

    private boolean hasEndTime(){
        return end != null;
    }

    private boolean isSingleDayEvent() {
        int startYear = start.get(Calendar.YEAR);
        int startDay = start.get(Calendar.DAY_OF_YEAR);

        int endYear = end.get(Calendar.YEAR);
        int endDay = end.get(Calendar.DAY_OF_YEAR);

        boolean sameYear = startYear == endYear;
        boolean sameDay = startDay == endDay;

        return sameDay && sameYear;
    }

    public boolean timeOverlaps(Calendar time){
        boolean sameYear, sameDay, withinStartAndEndTime;

        if(hasEndTime()){
            sameYear = time.get(Calendar.YEAR) == start.get(Calendar.YEAR) ||
                       time.get(Calendar.YEAR) == end.get(Calendar.YEAR);
            sameDay = time.get(Calendar.DAY_OF_YEAR) == start.get(Calendar.DAY_OF_YEAR) ||
                      time.get(Calendar.DAY_OF_YEAR) == end.get(Calendar.DAY_OF_YEAR);
            withinStartAndEndTime = start.getTime().before(time.getTime()) && end.getTime().after(time.getTime());
        }else{
            sameYear = time.get(Calendar.YEAR) == start.get(Calendar.YEAR);
            sameDay = time.get(Calendar.DAY_OF_YEAR) == start.get(Calendar.DAY_OF_YEAR);
            withinStartAndEndTime = false;
        }

        return (sameYear && sameDay) || withinStartAndEndTime;
    }

    @Override
    public int compareTo(Event event) {
        return getStart().compareTo(event.getStart());
    }
}
