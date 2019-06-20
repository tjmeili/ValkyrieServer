package server.data;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;



public class Day implements Serializable{
    private boolean active;
    private Date startTime, endTime, date;

    public Day(Date date) {
        this.date = date;
        this.active = false;
        setDefaultTimes();
    }

    public Day(Date date, boolean active) {
        this.active = active;
        this.date = date;
        setDefaultTimes();
    }

    public void setDefaultTimes(){
        startTime = new Date();
        endTime = new Date();

        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, 6);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        startTime.setTime(c.getTimeInMillis());
        c.set(Calendar.HOUR_OF_DAY, 14);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        endTime.setTime(c.getTimeInMillis());
    }


    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void rollToNextDay(){
        Calendar c = Calendar.getInstance();
        c.setTime(startTime);
        c.add(Calendar.DATE, 1);
        startTime = c.getTime();
        c.setTime(endTime);
        c.add(Calendar.DATE, 1);
        endTime = c.getTime();
        c.setTime(date);
        c.add(Calendar.DATE, 1);
        date = c.getTime();
    }
}

