package com.slate.jda;

import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MonthlyCalendar {

    private static Date dayLastUpdated = new Date();
    private static Date today;

    public static ArrayList<Date> upcomingEventDates = new ArrayList<>();
    public static ArrayList<String> upcomingEventNames = new ArrayList<>();

    public static void addEvent(Date eventDate, String eventName){
        upcomingEventDates.add(eventDate);
        upcomingEventNames.add(eventName);
    }

    public static void upddateCalender() {
        today = new Date();

        if (dayLastUpdated.compareTo(today) < 0) {
            int daysSinceLastUpdate = (int) (today.getTime() - dayLastUpdated.getTime()) / (24 * 60 * 60 * 1000);
            dayLastUpdated = today;
            for (int i = 0; i < daysSinceLastUpdate; i++) {
                upcomingEventDates.remove(0);
                upcomingEventNames.remove(0);
            }
        }
    }

    public static String displayCalendar(){

        String display = "Upcoming events in the next 30 days:";

        if(upcomingEventDates.size() != 0) {
            for (int i = 0; i < 30; i++) {
                if (i < upcomingEventDates.size()) {
                    display = display + "\n" + upcomingEventDates.get(i) + " - " + upcomingEventNames.get(i);
                }
            }
        }else{
            display = "There are no events happening in the next 30 days :(";
        }
        return display;
    }

    public static String show() {
        upddateCalender();
        return displayCalendar();
    }
}
