package com.slate.jda;

import com.slate.sys.DatabaseHelper;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class EventPoll {

    //Variables
    private String eventID;
    private String eventName;
    private String description;
    private Date dateCreated;
    private int voteTime;
    private int daysBetweenReminders;
    private int confirmationsNeeded;
    private Boolean weekly = false;
    private Date option1;
    private Date option2;
    private Date option3;
    private Date option4;
    private Date option5;

    public static HashMap<Integer,EventPoll> options = new HashMap<>();

    public static int mapCount = -1;
    int staticMapCount;

    Boolean published = false;
    Date selectedOption = null;

    public EventPoll (String eventID, String eventName, String description, Date dateCreated, int voteTime,
                      int confirmationsNeeded, Date option1, Date option2, Date option3, Date option4,
                      Date option5, Boolean weekly, int daysBetweenReminders) {
        this.eventID = eventID;
        this.eventName = eventName;
        this.description = description;
        this.dateCreated = dateCreated;
        this.voteTime = voteTime;
        this.daysBetweenReminders = daysBetweenReminders;
        this.confirmationsNeeded = confirmationsNeeded;
        this.weekly = weekly;
        this.option1 = option1;
        this.option2 = option2;
        this.option3 = option3;
        this.option4 = option4;
        this.option5 = option5;

        mapCount++;
        this.staticMapCount = mapCount;
        options.put(staticMapCount, this);
    }


    public MessageCreateData createMessage () {
        MessageCreateBuilder message = new MessageCreateBuilder();
        message.setContent("\nEvent Name: " + eventName +
                "\nResponse Time Limit: " + voteTime +
                "\nOptions: " + option1  + " " + option2 + " " + option3 + " " + option4 + " " + option5);

        Button changeName = Button.secondary("eventChange_Name:"+staticMapCount, "Change Event Name");
        Button changeDescription = Button.secondary("eventChange_Description:"+staticMapCount, "Change Event Description");
        Button changeVoteTime = Button.secondary("eventChange_VoteTime:"+staticMapCount, "Change Vote Time Limit");
        Button changeReminders = Button.secondary("eventChange_Reminders:"+staticMapCount, "Change Days Between Reminders");
        Button changeNeededConfirmations = Button.secondary("eventChange_NeededConfirmations:"+staticMapCount, "Change Confirmations Needed for Event to be Scheduled");
        Button setWeekly = Button.secondary("eventChange_Weekly:"+staticMapCount, "Reschedule This Event Weekly");
        Button addVoteOption = Button.success("eventVote_Add:"+staticMapCount, "Add Vote Option");
        Button removeVoteOption = Button.danger("eventVote_Remove:"+staticMapCount, "Remove Vote Option");
        Button publishEventPoll = Button.primary("publishEventPoll:"+staticMapCount, "Publish Event Poll");

        message.addComponents(
                ActionRow.of(changeName, changeDescription),
                ActionRow.of(changeVoteTime, changeNeededConfirmations),
                ActionRow.of(changeReminders, setWeekly),
                ActionRow.of(addVoteOption, removeVoteOption),
                ActionRow.of(publishEventPoll)
        ); //Buttons on multiple rows

        return message.build();
    }

    public MessageCreateData publishEventPoll(ButtonInteractionEvent event){
        MessageCreateBuilder message = new MessageCreateBuilder();

        Button yes1 = Button.success("event_confirm:1:"+getEventID()+":"+staticMapCount, "Available");
        Button no1 = Button.danger("event_deny:1:"+getEventID(), "Unavailable");

        message.setContent("Everyone, "+event.getUser().getName() + " has started a new event poll!\n" +
                "Event Name: " + eventName + "\n" +
                "Description: " + description + "\n" +
                "Please indicate your availability for the following time(s):\n"+
                "Option 1: " + getOption1());

        message.addComponents(
                ActionRow.of(yes1, no1)
        );
        if(getOption2() != null){
            Button yes2 = Button.success("event_confirm:2:"+getEventID()+":"+staticMapCount, "Available");
            Button no2 = Button.danger("event_deny:2:"+getEventID(), "Unavailable");

            message.addContent("\nOption 2: " + getOption2());

            message.addComponents(
                    ActionRow.of(yes2, no2)
            );
        }
        if(getOption3() != null){
            Button yes3 = Button.success("event_confirm:3:"+getEventID()+":"+staticMapCount, "Available");
            Button no3 = Button.danger("event_deny:3:"+getEventID(), "Unavailable");

            message.addContent("\nOption 3: " + getOption3());

            message.addComponents(
                    ActionRow.of(yes3, no3)
            );
        }
        if(getOption4() != null){
            Button yes4 = Button.success("event_confirm:4:"+getEventID()+":"+staticMapCount, "Available");
            Button no4 = Button.danger("event_deny:4:"+getEventID(), "Unavailable");

            message.addContent("\nOption 4: " + getOption4());

            message.addComponents(
                    ActionRow.of(yes4, no4)
            );
        }
        if(getOption5() != null){
            Button yes5 = Button.success("event_confirm:5:"+getEventID()+":"+staticMapCount, "Available");
            Button no5 = Button.danger("event_deny:5:"+getEventID(), "Unavailable");

            message.addContent("\nOption 5: " + getOption5());

            message.addComponents(
                    ActionRow.of(yes5, no5)
            );
        }
        return message.build();
    }

    public void addOption(Date newDate){
        if (option2 == null) {
            setOption2(newDate);
            DatabaseHelper.updateOption(getEventID(),"option2", (java.sql.Date)newDate);
        }else if (option3 == null) {
            setOption3(newDate);
            DatabaseHelper.updateOption(getEventID(),"option3", (java.sql.Date)newDate);
        }else if (option4 == null) {
            setOption4(newDate);
            DatabaseHelper.updateOption(getEventID(),"option4", (java.sql.Date)newDate);
        }else if (option5 == null) {
            setOption5(newDate);
            DatabaseHelper.updateOption(getEventID(),"option5", (java.sql.Date)newDate);
        }
    }

    public Boolean removeOption(int option){

        if(option == 2){
            if(option2 == null){
                return false;
            }else{
                setOption2(null);
                DatabaseHelper.removeOption(eventID,"option2");
                return true;
            }
        }else if (option == 3){
            if(option3 == null){
                return false;
            }else{
                setOption3(null);
                DatabaseHelper.removeOption(eventID,"option3");
                return true;
            }
        }else if (option == 4){
            if(option4 == null){
                return false;
            }else{
                setOption4(null);
                DatabaseHelper.removeOption(eventID,"option4");
                return true;
            }
        }else if (option == 5){
            if(option5 == null){
                return false;
            }else{
                setOption5(null);
                DatabaseHelper.removeOption(eventID,"option5");
                return true;
            }
        }else{
            return false;
        }
    }

    public Boolean checkEventSuccess() {
        if (!published) {
            if(DatabaseHelper.checkEventSuccess(eventID, "option1Response")){
                setSelectedOption(getOption1());
                return true;
            }
            else if(DatabaseHelper.checkEventSuccess(eventID, "option2Response")){
                setSelectedOption(getOption2());
                return true;
            }else if(DatabaseHelper.checkEventSuccess(eventID, "option3Response")){
                setSelectedOption(getOption3());
                return true;
            }else if(DatabaseHelper.checkEventSuccess(eventID, "option4Response")){
                setSelectedOption(getOption4());
                return true;
            }else if(DatabaseHelper.checkEventSuccess(eventID, "option5Response")){
                setSelectedOption(getOption5());
                return true;
            }
        }
        return published;
    }

    public MessageCreateData publishCompleteEvent(){
        MessageCreateBuilder message = new MessageCreateBuilder();

        message.setContent("Hey everyone, a new event has been scheduled!\n" +
                "Event Name: " + getEventName()+"\n"+
                "Description: " + getDescription()+"\n"+
                "Date: "+ getSelectedOption());

        Date dateCreated = new Date();
        int diffInDays = (int) (selectedOption.getTime() - dateCreated.getTime() / (24 * 60 * 60 * 1000));

        MonthlyCalendar.addEvent(selectedOption,eventName);

        return message.build();
    }


    public void beginEvent(TextChannel channel){
        MessageCreateBuilder message = new MessageCreateBuilder();

        Date dateCreated = new Date();
        long diffInMillies = selectedOption.getTime() - dateCreated.getTime(); //Milliseconds in a day
        int diffInDays = (int) (diffInMillies / (24 * 60 * 60 * 1000));    //Divide by the number of milliseconds in a day to get the number of days

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        startReminderLoop(channel, diffInDays);

        try {
            Thread.sleep(diffInDays*1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        channel.sendMessage("Hey everyone, "+ eventName + " is starting now!")
                .queue();

        rescheduleWeekly(channel);

    }

    public void startReminderLoop(TextChannel channel, int diffInDays){
        if(daysBetweenReminders == 0){
            //Reminders are set to 0, do nothing else
            return;
        }

        int totalReminders = diffInDays / daysBetweenReminders;
        //If there are 30 days between now and the event, and you want to send a reminder every 2 days,
        // you will send 30 / 2 = 15 reminders

        for(int i = 0; i < totalReminders; i++){
            if (diffInDays == 1) {
                channel.sendMessage("As a reminder, " + eventName + " begins in " + diffInDays + " day!").
                        queueAfter(daysBetweenReminders * i, TimeUnit.SECONDS); //Currently this is seconds, but really it will be days
            }else{
                channel.sendMessage("As a reminder, " + eventName + " begins in " + diffInDays + " days!").
                        queueAfter(daysBetweenReminders * i, TimeUnit.SECONDS); //Currently this is seconds, but really it will be days
            }

                diffInDays = diffInDays - daysBetweenReminders;
            }

    }

    public void rescheduleWeekly(TextChannel channel){
        if(!weekly){
            return;
        }else {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(selectedOption);
            calendar.add(Calendar.DAY_OF_MONTH,7);
            Date nextEventDate = calendar.getTime();

            java.sql.Date sqlFormat1 = new java.sql.Date(selectedOption.getTime());
            java.sql.Date sqlFormat2 = new java.sql.Date(nextEventDate.getTime());

            EventPoll nextWeek = DatabaseHelper.addEvent(eventName,description,sqlFormat1, voteTime, confirmationsNeeded,
                    sqlFormat2,null,null,null,null, weekly, daysBetweenReminders);

            //Make next week's poll

            MessageCreateBuilder message = new MessageCreateBuilder();

            Button yes1 = Button.success("event_confirm:1:"+nextWeek.getEventID()+":"+nextWeek.getStaticMapCount(), "Available");
            Button no1 = Button.danger("event_deny:1:"+getEventID(), "Unavailable");

            message.setContent("This is a weekly recurring event!\n" +
                    "Please indicate your availability for the same event next week!");
                    message.addComponents(
                            ActionRow.of(yes1, no1)
                    );

            channel.sendMessage(message.build()).queue();
        }
    }

    //************Getters and Setters**************

    public String getEventID() {
        return eventID;
    }

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public int getVoteTime() {
        return voteTime;
    }

    public void setVoteTime(int voteTime) {
        this.voteTime = voteTime;
    }

    public int getDaysBetweenReminders() {
        return daysBetweenReminders;
    }

    public void setDaysBetweenReminders(int daysBetweenReminders) {
        this.daysBetweenReminders = daysBetweenReminders;
    }

    public int getConfirmationsNeeded() {
        return confirmationsNeeded;
    }

    public void setConfirmationsNeeded(int confirmationsNeeded) {
        this.confirmationsNeeded = confirmationsNeeded;
    }

    public Boolean getWeekly() {
        return weekly;
    }

    public void setWeekly(Boolean weekly) {
        this.weekly = weekly;
    }

    public Date getOption1() {
        return option1;
    }

    public void setOption1(Date option1) {
        this.option1 = option1;
    }

    public Date getOption2() {
        return option2;
    }

    public void setOption2(Date option2) {
        this.option2 = option2;
    }

    public Date getOption3() {
        return option3;
    }

    public void setOption3(Date option3) {
        this.option3 = option3;
    }

    public Date getOption4() {
        return option4;
    }

    public void setOption4(Date option4) {
        this.option4 = option4;
    }

    public Date getOption5() {
        return option5;
    }

    public void setOption5(Date option5) {
        this.option5 = option5;
    }

    public Boolean getPublished() {
        return published;
    }

    public void setPublished(Boolean published) {
        this.published = published;
    }


    public Date getSelectedOption() {
        return selectedOption;
    }

    public void setSelectedOption(Date selectedOption) {
        this.selectedOption = selectedOption;
    }

    public int getStaticMapCount() {
        return staticMapCount;
    }

    public void setStaticMapCount(int staticMapCount) {
        this.staticMapCount = staticMapCount;
    }
}
