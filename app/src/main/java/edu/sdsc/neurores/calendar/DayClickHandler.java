package edu.sdsc.neurores.calendar;

import java.util.ArrayList;
import java.util.List;

import edu.sdsc.neurores.calendar.abstraction.Day;

/**
 * Created by trevor on 4/27/18.
 */

public class DayClickHandler {

    List<DayClickListener> dayClickListeners;

    DayClickHandler(){
        dayClickListeners = new ArrayList<>();
    }

    public void registerDayClickListener(DayClickListener dayClickListener){
        dayClickListeners.add(dayClickListener);
    }

    public void unregisterDayClickListener(DayClickListener dayClickListener){
        dayClickListeners.remove(dayClickListener);
    }

    public void notifyListenersOfDayClicked(Day day){
        for(DayClickListener dayClickListener : dayClickListeners){
            if(dayClickListener != null){
                dayClickListener.onDayClicked(day);
            }
        }
    }
}
