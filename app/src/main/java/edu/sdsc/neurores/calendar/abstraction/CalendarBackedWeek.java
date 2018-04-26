package edu.sdsc.neurores.calendar.abstraction;

import java.text.DateFormatSymbols;
import java.util.Calendar;

/**
 * Created by trevor on 4/25/18.
 */

public class CalendarBackedWeek implements Week {
    private Calendar calendar;

    CalendarBackedWeek(Calendar calendar){
        this.calendar = calendar;
    }

    @Override
    public Day getDay(int position) {
        Calendar temp = (Calendar) calendar.clone();
        temp.add(Calendar.DAY_OF_MONTH, position);
        return new CalendarBackedDay(temp);
    }

    @Override
    public String getMonthName() {
        return intMonthToString(calendar.get(Calendar.MONTH));
    }

    @Override
    public int getYear() {
        return calendar.get(Calendar.YEAR);
    }

    private String intMonthToString(int month){
        return new DateFormatSymbols().getMonths()[month];
    }
}
