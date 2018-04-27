package edu.sdsc.neurores.calendar.abstraction;

import android.util.Log;

import java.text.DateFormatSymbols;
import java.util.Calendar;

/**
 * Created by trevor on 4/25/18.
 */

public class CalendarBackedWeek implements Week {
    private Calendar calendar;
    private Day[] days;

    public CalendarBackedWeek(Calendar calendar){
        this.calendar = (Calendar) calendar.clone();

        this.calendar.set(Calendar.DAY_OF_WEEK, 0);

        this.calendar.set(Calendar.HOUR_OF_DAY, 0);
        this.calendar.clear(Calendar.MINUTE);
        this.calendar.clear(Calendar.SECOND);
        this.calendar.clear(Calendar.MILLISECOND);

        this.calendar.set(Calendar.DAY_OF_WEEK, this.calendar.getFirstDayOfWeek());

        days = new Day[7];
        for(int i = 0; i < 7; i++){
            Calendar temp = (Calendar) calendar.clone();
            temp.add(Calendar.DAY_OF_MONTH, i);
            days[i] =  new CalendarBackedDay(temp);
        }
    }

    @Override
    public Day getDay(int position) {
        return days[position];
    }

    @Override
    public String getMonthName() {
        return intMonthToString(calendar.get(Calendar.MONTH));
    }

    @Override
    public int getYear() {
        return calendar.get(Calendar.YEAR);
    }

    @Override
    public long getStartOfWeek() {
        return calendar.getTimeInMillis();
    }

    @Override
    public int getNumWeekInYear() {
        return calendar.get(Calendar.WEEK_OF_YEAR);
    }

    private String intMonthToString(int month){
        return new DateFormatSymbols().getMonths()[month];
    }
}
