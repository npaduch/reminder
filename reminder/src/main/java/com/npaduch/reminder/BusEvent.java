package com.npaduch.reminder;

import java.util.ArrayList;

/**
 * Created by nolanpaduch on 7/10/14.
 *
 * Simple class to hold event messages
 */
public class BusEvent {

    // targeted receiver
    private ArrayList<Integer> targets;
    // hold type of event
    private int type;
    // possible reminder to add/remove
    private Reminder reminder;

    // event receivers
    public static final int TARGET_MAIN             = 0;
    public static final int TARGET_PENDING          = 1;
    public static final int TARGET_COMPLETED        = 2;
    public static final int TARGET_NEW_REMINDER     = 3;

    // event types
    public static final int TYPE_ADD                = 0;
    public static final int TYPE_REFRESH            = 1;


    public BusEvent(int type, int target){
        this.type = type;
        this.targets = new ArrayList<Integer>();
        this.targets.add(target);
    }

    public BusEvent(int type, int target, Reminder reminder){
        this(type, target);
        this.reminder = reminder;
    }

    public ArrayList<Integer> getTargets(){
        return targets;
    }

    public void addTarget(int target){
        targets.add(target);
    }


}
