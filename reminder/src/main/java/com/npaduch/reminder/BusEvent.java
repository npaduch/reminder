package com.npaduch.reminder;

import java.util.ArrayList;

import it.gmariotti.cardslib.library.internal.Card;

/**
 * Created by nolanpaduch on 7/10/14.
 *
 * Simple class to hold event messages
 */
class BusEvent {

    // targeted receiver
    private final ArrayList<Integer> targets;

    // hold type of event
    private int type;

    // possible reminder to add/remove
    private Reminder reminder;

    // list of cards to pass for LoadReminders
    private ArrayList<Card> cardList;

    // fragemnts for fragment transition
    private int toFragment = FRAGMENT_PENDING;
    private int fromFragment = FRAGMENT_PENDING;

    // event receivers
    public static final int TARGET_MAIN             = 0;
    public static final int TARGET_PENDING          = 1;
    public static final int TARGET_COMPLETED        = 2;
    public static final int TARGET_NEW_REMINDER     = 3;

    /** event types **/
    // For completed and pending fragments
    public static final int TYPE_ADD                = 0;
    public static final int TYPE_REMOVE             = 1;
    public static final int TYPE_REFRESH_REMINDERS  = 2;
    public static final int TYPE_LOAD_REMINDERS     = 4;
    public static final int TYPE_ADD_CARD           = 5;
    // For main activity
    public static final int TYPE_CHANGE_FRAG        = 10;
    public static final int TYPE_EDIT_REMINDER      = 11;

    /** Fragment types **/
    public static final int FRAGMENT_PENDING        = 0;
    public static final int FRAGMENT_COMPLETED      = 1;
    public static final int FRAGMENT_NEW_REMINDER   = 2;
    public static final int FRAGMENT_SETTINGS       = 3;



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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Reminder getReminder() {
        return reminder;
    }

    public void setReminder(Reminder reminder) {
        this.reminder = reminder;
    }

    public int getToFragment() {
        return toFragment;
    }

    public void setToFragment(int toFragment) {
        this.toFragment = toFragment;
    }

    public int getFromFragment() {
        return fromFragment;
    }

    public void setFromFragment(int fromFragment) {
        this.fromFragment = fromFragment;
    }

    public ArrayList<Card> getCardList() {
        return cardList;
    }

    public void setCardList(ArrayList<Card> cardList) {
        this.cardList = cardList;
    }

}
