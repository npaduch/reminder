package com.npaduch.reminder;

/**
 * Created by nolanpaduch on 5/12/14.
 */
public class Reminder {

    // initialize values
    public static final String STRING_INIT = "STRING_INVALID";

    // color of item
    private int color;

    private String description;

    // timing
    private String dateString;
    private String timeString;
    private String dateTimeString;

    private int dateOffset;
    private int timeOffset;

    public Reminder() {
        setDescription(STRING_INIT);
        setDateString(STRING_INIT);
        setTimeString(STRING_INIT);
        setDateTimeString(STRING_INIT);
    }

    public int getDateOffset() {
        return dateOffset;
    }

    public void setDateOffset(int dateOffset) {
        this.dateOffset = dateOffset;
    }

    public int getTimeOffset() {
        return timeOffset;
    }

    public void setTimeOffset(int timeOffset) {
        this.timeOffset = timeOffset;
    }

    public String getDateString() {
        return dateString;
    }

    public void setDateString(String dayString) {
        this.dateString = dayString;
    }

    public String getTimeString() {
        return timeString;
    }

    public void setTimeString(String timeString) {
        this.timeString = timeString;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDateTimeString() {
        return dateTimeString;
    }

    public void setDateTimeString(String dateTimeString) {
        this.dateTimeString = dateTimeString;
    }



    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

}
