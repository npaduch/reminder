package com.npaduch.reminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

/**
 * Created by nolanpaduch on 5/12/14.
 *
 * Class to store all reminder attributes and
 * required functions
 */
public class Reminder {

    // initialize values
    public static final String STRING_INIT = "STRING_INVALID";
    public static final int INT_INIT = -1;

    private String description;

    // timing
    private int year;
    private int month;
    private int monthDay;
    private int hour;
    private int minute;
    private long msTime;

    private String dateString;
    private String timeString;
    private String dateTimeString;

    private int dateOffset;
    private int timeOffset;

    private boolean completed;

    // output file
    public final static String filename = "reminders.json";

    // JSON tags
    private final static String JSON_DESCRIPTION = "description";
    private final static String JSON_DATETIME = "datetime";
    private final static String JSON_COMPLETED = "completed";
    private final static String JSON_REMINDER_ID = "reminder_id";
    private final static String JSON_TIME_MS = "time_ms";
    private final static String JSON_DATE_OFFSET = "date_offset";
    private final static String JSON_TIME_OFFSET = "time_offset";
    private final static boolean JSON_DEBUG = false;    // toggle debug prints

    // ID
    private int reminderID;
    public final static String INTENT_REMINDER_ID = "intent_reminder_id";
    // reminder ID should be greater than 0
    public static final int BAD_REMINDER_ID = -1;

    // logging
    private final static String TAG = "RaminderClass";

    // time definitions
    public static final int TIME_MORNING_HOUR = 9;
    public static final int TIME_MORNING_MINUTE = 0;
    public static final int TIME_NOON_HOUR = 12;
    public static final int TIME_NOON_MINUTE = 0;
    public static final int TIME_AFTERNOON_HOUR =15;
    public static final int TIME_AFTERNOON_MINUTE = 0;
    public static final int TIME_EVENING_HOUR = 18;
    public static final int TIME_EVENING_MINUTE = 0;
    public static final int TIME_NIGHT_HOUR = 20;
    public static final int TIME_NIGHT_MINUTE = 0;

    // Called when reminder created for the first time
    public Reminder() {
        setDescription(STRING_INIT);
        setDateString(STRING_INIT);
        setTimeString(STRING_INIT);
        setDateTimeString(STRING_INIT);
        setYear(INT_INIT);
        setMonth(INT_INIT);
        setMonthDay(INT_INIT);
        setHour(INT_INIT);
        setMinute(INT_INIT);

        setCompleted(false);

        // random value greater than 0
        // TODO: make sure reminder ID not already in use
        setReminderID(new Random().nextInt(Integer.MAX_VALUE));
    }

    // Called when read from file
    public Reminder(String description, String dateTimeString, boolean completed,
                    int reminderID, long msTime, int dateOffset, int timeOffset){
        this.description = description;
        this.dateTimeString = dateTimeString;
        this.completed = completed;
        this.reminderID = reminderID;
        this.msTime = msTime;
        this.dateOffset = dateOffset;
        this.timeOffset = timeOffset;

        setYear(INT_INIT);
        setMonth(INT_INIT);
        setMonthDay(INT_INIT);
        setHour(INT_INIT);
        setMinute(INT_INIT);

        setCompleted(false);
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

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMonthDay() {
        return monthDay;
    }

    public void setMonthDay(int monthDay) {
        this.monthDay = monthDay;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getReminderID() {
        return reminderID;
    }

    public void setReminderID(int reminderID) {
        this.reminderID = reminderID;
    }

    public long getMsTime() {
        return msTime;
    }

    public void setMsTime(long msTime) {
        this.msTime = msTime;
    }

    public void calculateMsTime(int year, int month, int day, int hour, int minute){
        // This initializes the class with the time RIGHT NOW
        Calendar reminderCal = Calendar.getInstance();

        // handle day
        if(getDateOffset() == NewReminderFragment.DATE_TODAY){
            // we don't have to change anything
            Log.d(TAG,"Date is today");
        } else if(getDateOffset() == NewReminderFragment.DATE_TOMORROW) {
            // add time for 1 day
            // this will increment across months/years accordingly
            reminderCal.add(Calendar.DAY_OF_MONTH, 1);
        } else {
            // Custom date was given
            reminderCal.set(year, month, day);
        }


        // handle time
        // TODO: Make these settings
        switch(getTimeOffset()){
            case NewReminderFragment.TIME_MORNING:
                reminderCal.set(Calendar.HOUR_OF_DAY, TIME_MORNING_HOUR);
                reminderCal.set(Calendar.MINUTE, TIME_MORNING_MINUTE);
                break;
            case NewReminderFragment.TIME_NOON:
                reminderCal.set(Calendar.HOUR_OF_DAY, TIME_NOON_HOUR);
                reminderCal.set(Calendar.MINUTE, TIME_NOON_MINUTE);
                break;
            case NewReminderFragment.TIME_AFTERNOON:
                reminderCal.set(Calendar.HOUR_OF_DAY, TIME_AFTERNOON_HOUR);
                reminderCal.set(Calendar.MINUTE, TIME_AFTERNOON_MINUTE);
                break;
            case NewReminderFragment.TIME_EVENING:
                reminderCal.set(Calendar.HOUR_OF_DAY, TIME_EVENING_HOUR);
                reminderCal.set(Calendar.MINUTE, TIME_EVENING_MINUTE);
                break;
            case NewReminderFragment.TIME_NIGHT:
                reminderCal.set(Calendar.HOUR_OF_DAY, TIME_NIGHT_HOUR);
                reminderCal.set(Calendar.MINUTE, TIME_NIGHT_MINUTE);
                break;
            case NewReminderFragment.TIME_OTHER:
                reminderCal.set(Calendar.HOUR_OF_DAY, hour);
                reminderCal.set(Calendar.MINUTE, minute);
                break;
        }

        // set values
        long ms = reminderCal.getTimeInMillis();
        setMsTime(ms);
    }

    public void setAlarm(Context context){
        Log.d(TAG, "Setting alarm for reminder.");

        // set time for a minute from now
        Long time = getMsTime();


        // create an Intent and set the class which will execute when Alarm triggers
        Intent intentAlarm = new Intent(context, AlarmReceiver.class);
        intentAlarm.putExtra(INTENT_REMINDER_ID, getReminderID());


        // create the object
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        //set the alarm for particular time
        alarmManager.set(AlarmManager.RTC_WAKEUP, time, PendingIntent.getBroadcast(context, getReminderID(), intentAlarm, 0));
        Log.d(TAG, "Alarm scheduled");
    }

    /************************************************************
     *
     *          FILE HANDLING
     *
     ************************************************************/

    public static void initFile(Context context) {

        Log.d(TAG, "Initializing file "+context.getFilesDir()+File.pathSeparator+filename);
        File file = new File(context.getFilesDir(), filename);

        ArrayList<Reminder> reminders = new ArrayList<Reminder>();

        try {
            // Append to file if it exists
            FileOutputStream fOutputStream = new FileOutputStream(file, false);
            writeJsonStream(fOutputStream, reminders);
            Log.d(TAG, "Output To file successful.");
        } catch (Exception e) {
            Log.e(TAG, "Exception when writing to file. "+e);
        }
    }

    public void writeToFile(Context context) {

        Log.d(TAG, "Adding to file "+context.getFilesDir()+File.pathSeparator+filename);
        File file = new File(context.getFilesDir(), filename);

        ArrayList<Reminder> reminderList = null;

        // 1. Read in JSON file content
        try {
            // Open file
            FileInputStream fileInputStream = new FileInputStream(file);
            reminderList = readJsonStream(fileInputStream);
            Log.d(TAG, "File read successful.");
        } catch (Exception e) {
            Log.e(TAG, "Exception when writing to file."+e);
        }

        if(reminderList == null){
            Log.d(TAG, "Reminder list null. Creating a new list.");
            reminderList = new ArrayList<Reminder>();
        }

        // add new item to the beginning of the list
        reminderList.add(0, this);

        try {
            // Append to file if it exists
            FileOutputStream fOutputStream = new FileOutputStream(file, false);
            writeJsonStream(fOutputStream, reminderList);
            Log.d(TAG, "Output To file successful.");
        } catch (Exception e) {
            Log.e(TAG, "Exception when writing to file. "+e);
        }
    }

    /** All required for JSON Parsing **/
    public static ArrayList<Reminder> readJsonStream(InputStream in) throws IOException {
        if(JSON_DEBUG) Log.d(TAG, "Begin readJsonStream");
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        try {
            return readRemindersArray(reader);
        }
        finally{
            reader.close();
        }
    }

    public static ArrayList<Reminder> readRemindersArray(JsonReader reader) throws IOException {
        if(JSON_DEBUG)Log.d(TAG,"Begin readReminderArray");
        ArrayList<Reminder> reminderList = new ArrayList<Reminder>();

        reader.beginArray();
        if(JSON_DEBUG) Log.d(TAG,"beginArray");
        while (reader.hasNext()) {
            reminderList.add(readReminder(reader));
        }
        if(JSON_DEBUG) Log.d(TAG,"endArray");
        reader.endArray();
        return reminderList;
    }

    public static Reminder readReminder(JsonReader reader) throws IOException {
        String description = "";
        String dateTimeString = "";
        Boolean completed = false;
        int reminderId = Reminder.BAD_REMINDER_ID;
        long timeMs = Reminder.INT_INIT;
        int dateOffset = Reminder.INT_INIT;
        int timeOffset = Reminder.INT_INIT;

        if(JSON_DEBUG) Log.d(TAG, "Begin to read reminder");
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals(JSON_DESCRIPTION)) {
                description = reader.nextString();
            } else if (name.equals(JSON_DATETIME)) {
                dateTimeString = reader.nextString();
            } else if (name.equals(JSON_COMPLETED)) {
                completed = reader.nextBoolean();
            } else if (name.equals(JSON_REMINDER_ID)) {
                reminderId = reader.nextInt();
            } else if (name.equals(JSON_TIME_MS)) {
                timeMs = reader.nextLong();
            } else if (name.equals(JSON_TIME_OFFSET)) {
                timeOffset = reader.nextInt();
            } else if (name.equals(JSON_DATE_OFFSET)) {
                dateOffset = reader.nextInt();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        Reminder r = new Reminder(description, dateTimeString, completed, reminderId, timeMs,
                dateOffset, timeOffset);
        if(JSON_DEBUG) r.outputReminderToLog();

        if(JSON_DEBUG) Log.d(TAG, "End read reminder");
        return r;
    }

    /** JSON Writing **/
    public static void writeJsonStream(OutputStream out, ArrayList<Reminder> reminders) throws IOException {
        JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
        writer.setIndent("  ");
        writeMessagesArray(writer, reminders);
        writer.close();
    }

    public static void writeMessagesArray(JsonWriter writer, ArrayList<Reminder> reminders) throws IOException {
        writer.beginArray();

        for (Reminder r : reminders){
            writeMessage(writer, r);
        }
        writer.endArray();
    }

    public static void writeMessage(JsonWriter writer, Reminder reminder) throws IOException {
        writer.beginObject();
        writer.name(JSON_DESCRIPTION).value(reminder.getDescription());
        writer.name(JSON_DATETIME).value(reminder.getDateTimeString());
        writer.name(JSON_COMPLETED).value(reminder.isCompleted());
        writer.name(JSON_REMINDER_ID).value(reminder.getReminderID());
        writer.name(JSON_TIME_MS).value(reminder.getMsTime());
        writer.name(JSON_TIME_OFFSET).value(reminder.getTimeOffset());
        writer.name(JSON_DATE_OFFSET).value(reminder.getDateOffset());
        writer.endObject();
    }

    public void outputReminderToLog(){
        Log.d(TAG,"Reminder: START");
        Log.d(TAG,"Reminder: Description: "+getDescription());
        Log.d(TAG,"Reminder: Date/Time: "+getDateTimeString());
        Log.d(TAG,"Reminder: Completed: "+isCompleted());
        Log.d(TAG,"Reminder: ID: "+getReminderID());
        Log.d(TAG,"Reminder: Time (ms): "+getMsTime());
        Log.d(TAG,"Reminder: END");
    }

}
