package com.npaduch.reminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;

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

    private int dateOffset;
    private int timeOffset;

    private boolean completed;

    // output file
    public final static String filename = "reminders.json";

    // JSON tags
    private final static String JSON_DESCRIPTION = "description";
    private final static String JSON_COMPLETED = "completed";
    private final static String JSON_REMINDER_ID = "reminder_id";
    private final static String JSON_TIME_MS = "time_ms";
    private final static String JSON_DATE_OFFSET = "date_offset";
    private final static String JSON_TIME_OFFSET = "time_offset";
    private final static String JSON_TIME_HOUR = "time_hour";
    private final static String JSON_TIME_MINUTE = "time_minute";
    private final static String JSON_DATE_YEAR = "date_year";
    private final static String JSON_DATE_MONTH = "date_month";
    private final static String JSON_DATE_MONTHDAY = "date_monthday";
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
        setYear(INT_INIT);
        setMonth(INT_INIT);
        setMonthDay(INT_INIT);
        setHour(INT_INIT);
        setMinute(INT_INIT);
        setMsTime(INT_INIT);

        setCompleted(false);

        // random value greater than 0
        // TODO: make sure reminder ID not already in use
        setReminderID(new Random().nextInt(Integer.MAX_VALUE));
    }

    // Called when read from file
    public Reminder(String description, boolean completed,
                    int reminderID, long msTime, int dateOffset, int timeOffset){
        this.description = description;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

        // set seconds to 0
        reminderCal.set(Calendar.SECOND, 0);

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

    public void cancelAlarm(Context context){
        Log.d(TAG, "Cencelling alarm for reminder.");

        // set time for a minute from now
        Long time = getMsTime();


        // create an Intent and set the class which will execute when Alarm triggers
        Intent intentAlarm = new Intent(context, AlarmReceiver.class);
        intentAlarm.putExtra(INTENT_REMINDER_ID, getReminderID());


        // create the object
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        //set the alarm for particular time
        alarmManager.cancel(PendingIntent.getBroadcast(context, getReminderID(), intentAlarm, 0));
        Log.d(TAG, "Alarm cancelled");
    }

    /**
     * Use the MS time to calculate a user-friendly
     * string for the time the reminder is due.
     *
     * Examples:
     *  This Morning
     *  Today at Noon
     *  This Afternoon
     *  This Evening
     *  Tonight
     *  Today at 5:27 PM
     *  Tomorrow Morning
     *  Tomorrow at Noon
     *  Tomorrow Afternoon
     *  Tomorrow Evening
     *  Tomorrow Night
     *  June 5, 2014 in the Morning
     *  June 27, 2014 at 5:32 AM
     *
     * @return Time in string format
     */
    public String getDateTimeString(Context context){
        String s = "";
        boolean isToday = false;
        boolean isTomorrow = false;

        // Check if the time was never set
        long msTime = getMsTime();
        if(msTime == INT_INIT){
            Log.e(TAG, "MS Time not set. Can't build string.");
            return STRING_INIT;
        }

        // We need three calendar entries, reminder, today, tomorrow
        // This will allow us to calculate if it's for today or tomorrow
        // reminder
        Calendar calReminder = Calendar.getInstance();
        calReminder.setTimeInMillis(msTime);
        // today
        Calendar calToday = Calendar.getInstance();
        // tomorrow
        Calendar calTomorrow = Calendar.getInstance();
        calTomorrow.add(Calendar.DAY_OF_MONTH, 1);

        // find out if reminder is today or tomorrow
        if((calReminder.get(Calendar.YEAR) == calToday.get(Calendar.YEAR)) &&
                (calReminder.get(Calendar.DAY_OF_YEAR) == calToday.get(Calendar.DAY_OF_YEAR))){
            isToday = true;
        } else if((calReminder.get(Calendar.YEAR) == calTomorrow.get(Calendar.YEAR)) &&
                (calReminder.get(Calendar.DAY_OF_YEAR) == calTomorrow.get(Calendar.DAY_OF_YEAR))){
            isTomorrow = true;
        }

        if(isToday){
            switch(getTimeOffset()){
                case NewReminderFragment.TIME_MORNING:
                case NewReminderFragment.TIME_AFTERNOON:
                case NewReminderFragment.TIME_EVENING:
                    s += context.getResources().getString(R.string.time_this);
                    break;
                case NewReminderFragment.TIME_NIGHT:
                    s += context.getResources().getString(R.string.time_tonight);
                    break;
                case NewReminderFragment.TIME_NOON:
                default:
                    s += context.getResources().getString(R.string.time_today);
                    s += ' ';
                    s += context.getResources().getString(R.string.time_at);
                    break;
            }
        } else if (isTomorrow){
            switch(getTimeOffset()){
                case NewReminderFragment.TIME_MORNING:
                case NewReminderFragment.TIME_AFTERNOON:
                case NewReminderFragment.TIME_EVENING:
                case NewReminderFragment.TIME_NIGHT:
                    s += context.getResources().getString(R.string.time_tomorrow);
                    break;
                case NewReminderFragment.TIME_NOON:
                default:
                    s += context.getResources().getString(R.string.time_tomorrow);
                    s += ' ';
                    s += context.getResources().getString(R.string.time_at);
                    break;
            }

        } else {
            // we have a specific day
            switch (getTimeOffset()) {
                case NewReminderFragment.TIME_MORNING:
                case NewReminderFragment.TIME_AFTERNOON:
                case NewReminderFragment.TIME_EVENING:
                    s += buildDateString(context, calReminder);
                    s += ' ';
                    s += context.getResources().getString(R.string.time_in_the);
                    break;
                case NewReminderFragment.TIME_NOON:
                case NewReminderFragment.TIME_NIGHT:
                default:
                    s += buildDateString(context, calReminder);
                    s += ' ';
                    s += context.getResources().getString(R.string.time_at);
                    break;
            }
        }

        // space between date and time
        s += ' ';

        switch(getTimeOffset()){
            case NewReminderFragment.TIME_MORNING:
                s += context.getResources().getString(R.string.time_morning);
                break;
            case NewReminderFragment.TIME_NOON:
                s += context.getResources().getString(R.string.time_noon);
                break;
            case NewReminderFragment.TIME_AFTERNOON:
                s += context.getResources().getString(R.string.time_afternoon);
                break;
            case NewReminderFragment.TIME_EVENING:
                s += context.getResources().getString(R.string.time_evening);
                break;
            case NewReminderFragment.TIME_NIGHT:
                // if it's also today, we use "tonight"
                if(!isToday) {
                    s += context.getResources().getString(R.string.time_night);
                }
                break;
            default:
                s += buildTimeString(context, calReminder);
                break;
        }

        return s;
    }

    public static String buildDateString(Context context, Calendar cal){
        String dateString = "";
        // Format = Month Day, Year
        dateString += cal.getDisplayName(Calendar.MONTH, Calendar.LONG, context.getResources().getConfiguration().locale);
        dateString += ' ';
        dateString += cal.get(Calendar.DAY_OF_MONTH);
        dateString += ',';
        dateString += ' ';
        dateString += cal.get(Calendar.YEAR);
        return dateString;
    }
    public static  String buildTimeString(Context context, Calendar cal){
        String timeString = "";
        // format = Hour:Minute (AM/PM)
        // handle 12/24 hour
        if(DateFormat.is24HourFormat(context))
            timeString += cal.get(Calendar.HOUR_OF_DAY);
        else {
            int hourString = cal.get(Calendar.HOUR);
            if(hourString == 0) {
                timeString += "12";
            } else {
                timeString += hourString;
            }
        }
        timeString += ':';
        timeString += String.format("%02d",cal.get(Calendar.MINUTE));
        // Add AM/PM if 12 hour
        if(!DateFormat.is24HourFormat(context)) {
            timeString += " ";
            if(cal.get(Calendar.AM_PM) == Calendar.AM)
                timeString += context.getResources().getString(R.string.time_suffix_AM);
            else if(cal.get(Calendar.AM_PM) == Calendar.PM)
                timeString += context.getResources().getString(R.string.time_suffix_PM);
        }
        return timeString;
    }
    /************************************************************
     *
     *          FILE HANDLING
     *
     ************************************************************/



    public static ArrayList<Reminder> getJSONFileContents(Context context){
        Log.d(TAG, "Looking for file " + context.getFilesDir() + File.pathSeparator + Reminder.filename);

        // Check for file
        File file = new File(context.getFilesDir(), Reminder.filename);
        if(!file.exists()){
            return null;
        }
        Log.d(TAG,"JSON file found. Loading in reminders.");

        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            return Reminder.readJsonStream(fileInputStream);
        } catch (Exception e){
            Log.e(TAG, "Error reading existing JSON file: "+e);
        }
        Log.e(TAG, "Could not read input stream to get existing reminders.");
        return null;
    }

    public static Reminder findReminder(int reminderId, ArrayList<Reminder> reminders){

        // Get reminder ID of reminder
        if(reminderId == Reminder.BAD_REMINDER_ID) {
            Log.e(TAG, "Intent data did not contain reminder ID. Cannot throw notification");
            return null;
        }

        Log.d(TAG,"Reminder ID: " + reminderId);

        for(Reminder r : reminders){
            if(r.getReminderID() == reminderId){
                Log.d(TAG, "Found reminder.");
                r.outputReminderToLog();
                return r;
            }
        }

        // if we're here, we don't have a matching reminder
        Log.e(TAG, "Reminder ID not in reminder list. Cannot throw notification");
        return null;
    }

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
        reminderList = insertReminder(reminderList);

        try {
            // Append to file if it exists
            FileOutputStream fOutputStream = new FileOutputStream(file, false);
            writeJsonStream(fOutputStream, reminderList);
            Log.d(TAG, "Output To file successful.");
        } catch (Exception e) {
            Log.e(TAG, "Exception when writing to file. "+e);
        }
    }

    public ArrayList<Reminder> insertReminder(ArrayList<Reminder> rList){
        boolean found = false;
        // first, check if the reminder already exists
        for(int i = 0; i < rList.size(); i++){
           if(rList.get(i).getReminderID() == this.getReminderID()){
               rList.set(i, this);
               found = true;
               break;
           }
        }
        // if not found, insert at the beginning for now
        if(!found){
            rList.add(0, this);
        }
        return rList;
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

        Reminder r = new Reminder();

        if(JSON_DEBUG) Log.d(TAG, "Begin to read reminder");
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals(JSON_DESCRIPTION)) {
                r.setDescription(reader.nextString());
            } else if (name.equals(JSON_COMPLETED)) {
                r.setCompleted(reader.nextBoolean());
            } else if (name.equals(JSON_REMINDER_ID)) {
                r.setReminderID(reader.nextInt());
            } else if (name.equals(JSON_TIME_MS)) {
                r.setMsTime(reader.nextLong());
            } else if (name.equals(JSON_TIME_OFFSET)) {
                r.setTimeOffset(reader.nextInt());
            } else if (name.equals(JSON_DATE_OFFSET)) {
                r.setDateOffset(reader.nextInt());
            } else if (name.equals(JSON_DATE_YEAR)) {
                r.setYear(reader.nextInt());
            } else if (name.equals(JSON_DATE_MONTH)) {
                r.setMonth(reader.nextInt());
            } else if (name.equals(JSON_DATE_MONTHDAY)) {
                r.setMonthDay(reader.nextInt());
            } else if (name.equals(JSON_TIME_HOUR)) {
                r.setHour(reader.nextInt());
            } else if (name.equals(JSON_TIME_MINUTE)) {
                r.setMinute(reader.nextInt());
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();

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
        writer.name(JSON_COMPLETED).value(reminder.isCompleted());
        writer.name(JSON_REMINDER_ID).value(reminder.getReminderID());
        writer.name(JSON_TIME_MS).value(reminder.getMsTime());
        writer.name(JSON_TIME_OFFSET).value(reminder.getTimeOffset());
        writer.name(JSON_DATE_OFFSET).value(reminder.getDateOffset());
        writer.name(JSON_DATE_YEAR).value(reminder.getYear());
        writer.name(JSON_DATE_MONTH).value(reminder.getMonth());
        writer.name(JSON_DATE_MONTHDAY).value(reminder.getMonthDay());
        writer.name(JSON_TIME_HOUR).value(reminder.getHour());
        writer.name(JSON_TIME_MINUTE).value(reminder.getMinute());
        writer.endObject();
    }

    public void outputReminderToLog(){
        Log.d(TAG,"Reminder: START");
        Log.d(TAG,"Reminder: Description: "+getDescription());
        Log.d(TAG,"Reminder: Completed: "+isCompleted());
        Log.d(TAG,"Reminder: ID: "+getReminderID());
        Log.d(TAG, "Reminder: Time (ms): " + getMsTime());
        Log.d(TAG,"Reminder: END");
    }

}
