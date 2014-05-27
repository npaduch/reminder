package com.npaduch.reminder;

/**
 * Created by nolanpaduch on 5/12/14.
 */
public class Reminder {

    // color of item
    private int color;

    private String description;

    // timing
    private int dateYear;
    private int dateMonth;
    private int dateDay;
    private int dateTime;
    private int dateWeekday;

    // specific time or "afternoon"
    private boolean isVague;

    public Reminder() {

    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDateYear() {
        return dateYear;
    }

    public void setDateYear(int dateYear) {
        this.dateYear = dateYear;
    }

    public int getDateMonth() {
        return dateMonth;
    }

    public void setDateMonth(int dateMonth) {
        this.dateMonth = dateMonth;
    }

    public int getDateTime() {
        return dateTime;
    }

    public void setDateTime(int dateTime) {
        this.dateTime = dateTime;
    }

    public int getDateDay() {
        return dateDay;
    }

    public void setDateDay(int dateDay) {
        this.dateDay = dateDay;
    }

    public int getDateWeekday() {
        return dateWeekday;
    }

    public void setDateWeekday(int dateWeekday) {
        this.dateWeekday = dateWeekday;
    }

    public boolean isVague() {
        return isVague;
    }

    public void setVague(boolean isVague) {
        this.isVague = isVague;
    }


    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
