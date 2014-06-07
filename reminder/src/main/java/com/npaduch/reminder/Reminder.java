package com.npaduch.reminder;

import android.content.Context;
import android.os.Message;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.JsonWriter;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nolanpaduch on 5/12/14.
 */
public class Reminder {

    // initialize values
    public static final String STRING_INIT = "STRING_INVALID";

    private String description;

    // timing
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

    // logging
    private final static String TAG = "RaminderClass";

    public Reminder() {
        setDescription(STRING_INIT);
        setDateString(STRING_INIT);
        setTimeString(STRING_INIT);
        setDateTimeString(STRING_INIT);
        setCompleted(false);
    }

    public Reminder(String description, String dateTimeString, boolean completed){
        this.description = description;
        this.dateTimeString = dateTimeString;
        this.completed = completed;
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

    public void writeToFile(Context context) {

        Log.d(TAG, "Creating file "+context.getFilesDir()+File.pathSeparator+filename);
        File file = new File(context.getFilesDir(), filename);

        String jsonString;
        JSONArray jsonArray;
        ArrayList reminders = null;

        // 1. Read in JSON file content
        try {
            // Open file
            FileInputStream fileInputStream = new FileInputStream(file);
            reminders = readJsonStream(fileInputStream);
            Log.d(TAG, "Output To file successful.");
        } catch (Exception e) {
            Log.e(TAG, "Exception when writing to file. "+e);
        }

        if(reminders == null){
            reminders = new ArrayList<Reminder>();
        }

        // add new item
        reminders.add(this);

        try {
            // Append to file if it exists
            FileOutputStream fOutputStream = new FileOutputStream(file, true);
            writeJsonStream(fOutputStream, reminders);
            Log.d(TAG, "Output To file successful.");
        } catch (Exception e) {
            Log.e(TAG, "Exception when writing to file. "+e);
        }
    }

    /*
    private  buildJSONString(){

        JSONObject object = new JSONObject();
        try {
            object.put(JSON_DESCRIPTION, getDescription());
            object.put(JSON_DATETIME, getDateTimeString());
            object.put(JSON_COMPLETED, isCompleted());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String s = object.toString();

        return s;
    }
    */

    /** All required for JSON Parsing **/
    public ArrayList readJsonStream(InputStream in) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        try {
            return readRemindersArray(reader);
        }
        finally{
            reader.close();
        }
    }

    public ArrayList readRemindersArray(JsonReader reader) throws IOException {
        ArrayList reminders = new ArrayList();

        reader.beginArray();
        while (reader.hasNext()) {
            reminders.add(readReminder(reader));
        }
        reader.endArray();
        return reminders;
    }

    public Reminder readReminder(JsonReader reader) throws IOException {
        String description = "";
        String dateTimeString = "";
        Boolean completed = false;

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals(JSON_DESCRIPTION)) {
                description = reader.nextString();
            } else if (name.equals(JSON_DATETIME)) {
                dateTimeString = reader.nextString();
            } else if (name.equals(JSON_COMPLETED)) {
                completed = reader.nextBoolean();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return new Reminder(description, dateTimeString, completed);
    }

    /** JSON Writing **/
    public void writeJsonStream(OutputStream out, List reminders) throws IOException {
        JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
        writer.setIndent("  ");
        writeMessagesArray(writer, reminders);
        writer.close();
    }

    public void writeMessagesArray(JsonWriter writer, List reminders) throws IOException {
        writer.beginArray();
        for(int i = 0; i < reminders.size(); i++) {
            writeMessage(writer, (Reminder)reminders.get(i));
        }
        writer.endArray();
    }

    public void writeMessage(JsonWriter writer, Reminder reminder) throws IOException {
        writer.beginObject();
        writer.name(JSON_DESCRIPTION).value(reminder.getDescription());
        writer.name(JSON_DATETIME).value(reminder.getDateTimeString());
        writer.name(JSON_COMPLETED).value(reminder.isCompleted());
        writer.endObject();
    }

}
