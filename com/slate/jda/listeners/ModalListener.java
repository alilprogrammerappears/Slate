package com.slate.jda.listeners;

import com.slate.jda.EventId;
import com.slate.jda.EventPoll;
import com.slate.jda.tasks.TaskDraft;
import com.slate.jda.votepolls.VotePollDraft;
import com.slate.sys.DatabaseHelper;
import com.slate.sys.Draft;
import com.vdurmont.emoji.EmojiManager;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.sql.Date;
import java.time.Instant;
import java.util.Optional;

public class ModalListener extends ListenerAdapter {
    @Override
    public void onModalInteraction (ModalInteractionEvent event) {
        EventId id = new EventId(event.getModalId());

        if (id.getComponent(0).equals("up")) {
            switch (id.getComponent(1)) {
                case "phoneNum" -> {
                    try {
                        String phoneNum = Optional.ofNullable(event.getValue("phoneNum")).map(ModalMapping::getAsString)
                            .orElseThrow(() -> new IllegalStateException("Failed to get email from modal."));

                        if (!phoneNum.matches("^[0-9]+$")) {
                            return;
                        }

                        if (!DatabaseHelper.updatePhoneNumber(event.getUser().getId(), phoneNum)) {
                            throw new IllegalStateException("Failed to update phone number");
                        }

                        event.editMessage(MessageEditData.fromCreateData(DatabaseHelper.getUserPreferences(event.getUser()
                            .getId()).orElseThrow().createMessage())).queue();
                    }
                    catch (Exception e) {
                        event.reply("Oops! An error occurred processing your request. Please try again.")
                            .setEphemeral(true).queue();
                    }
                }
                case "emailAddress" -> {
                    try {
                        String email = Optional.ofNullable(event.getValue("email")).map(ModalMapping::getAsString)
                            .orElseThrow(() -> new IllegalStateException("Failed to get email from modal."));

                        if (!email.matches("^.+@.+\\..+$")) {
                            return;
                        }

                        if (!DatabaseHelper.updateEmailAddress(event.getUser().getId(), email)) {
                            throw new IllegalStateException("Failed to update email address.");
                        }

                        event.editMessage(MessageEditData.fromCreateData(DatabaseHelper.getUserPreferences(event.getUser()
                            .getId()).orElseThrow().createMessage())).queue();
                    }
                    catch (Exception e) {
                        event.reply("Oops! An error occurred processing your request. Please try again.")
                            .setEphemeral(true).queue();
                    }
                }
            }
        }
        else if (id.getComponent(0).equals("task")) {
            TaskDraft taskDraft = Draft.get(TaskDraft.class, Integer.parseInt(id.getOption(0)))
                .orElseThrow(() -> new IllegalStateException("Unexpected missing task draft!"));

            switch (id.getComponent(1)) {
                case "title" -> {
                    String title = Optional.ofNullable(event.getValue("title")).map(ModalMapping::getAsString)
                        .orElseThrow(() -> new IllegalStateException("Failed to get title from modal."));
                    taskDraft.setTitle(title);
                    event.editMessage(taskDraft.createMessage()).queue();
                }
                case "desc" -> {
                    String desc = Optional.ofNullable(event.getValue("desc")).map(ModalMapping::getAsString)
                        .orElseThrow(() -> new IllegalStateException("Failed to get description from modal."));
                    taskDraft.setDescription(desc);
                    event.editMessage(taskDraft.createMessage()).queue();
                }
                case "due" -> {
                    String dueDateStr = Optional.ofNullable(event.getValue("date")).map(ModalMapping::getAsString)
                        .orElseThrow(() -> new IllegalStateException("Failed to get due date from modal."));
                    try {
                        taskDraft.setDueDate(Instant.ofEpochSecond(Long.parseLong(dueDateStr)));
                        event.editMessage(taskDraft.createMessage()).queue();
                    }
                    catch (Exception e) {
                        event.reply("Oops! The timestamp you entered isn't valid. Please try again.")
                            .setEphemeral(true).queue();
                    }
                }
            }
        }
        else if (id.getComponent(0).equals("vp")) {
            VotePollDraft pollDraft = Draft.get(VotePollDraft.class, Integer.parseInt(id.getOption(0)))
                .orElseThrow(() -> new IllegalStateException("Unexpected missing vote poll draft."));

            switch (id.getComponent(1)) {
                case "setQuestion" -> {
                    String question = Optional.ofNullable(event.getValue("question")).map(ModalMapping::getAsString)
                        .orElseThrow(() -> new IllegalStateException("Failed to get question from modal."));
                    if (question.isBlank()) { return; }

                    pollDraft.setQuestion(question);
                    event.editMessage(pollDraft.createDraftMenu()).queue();
                }
                case "setEndDate" -> {
                    String endDateStr = Optional.ofNullable(event.getValue("date")).map(ModalMapping::getAsString)
                        .orElseThrow(() -> new IllegalStateException("Failed to get end date from modal."));
                    try {
                        pollDraft.setEndDate(Instant.ofEpochSecond(Long.parseLong(endDateStr)));
                        event.editMessage(pollDraft.createDraftMenu()).queue();
                    }
                    catch (Exception e) {
                        event.reply("Oops! The timestamp you entered isn't valid. Please try again.")
                            .setEphemeral(true).queue();
                    }
                }
                case "addOption" -> {
                    String emoji = Optional.ofNullable(event.getValue("emoji")).map(ModalMapping::getAsString)
                        .orElseThrow(() -> new IllegalStateException("Failed to get emoji from modal."));
                    if (!EmojiManager.isEmoji(emoji)) { return; }

                    String optionDesc = Optional.ofNullable(event.getValue("desc")).map(ModalMapping::getAsString)
                        .orElseThrow(() -> new IllegalStateException("Failed to get option description from modal."));
                    if (optionDesc.isBlank()) { return; }

                    pollDraft.addOption(emoji, optionDesc);
                    event.editMessage(pollDraft.createDraftMenu()).queue();
                }
            }
        }
        else if (id.getComponent(0).equals("eventName")){
            ModalMapping nameField = event.getValue("name");

            EventPoll tempEvent = EventPoll.options.get(Integer.parseInt(id.getOption(0)));
            tempEvent.setEventName(nameField.getAsString());

            DatabaseHelper.updateEventName(tempEvent.getEventID(), tempEvent.getEventName());

            event.editMessage(
                    MessageEditData.fromCreateData( //Converts creating new message to editing previous message
                            tempEvent.createMessage()
                    )
            ).queue();
        }
        else if (id.getComponent(0).equals("eventDescription")){
            ModalMapping descField = event.getValue("eventDesc");

            EventPoll tempEvent = EventPoll.options.get(Integer.parseInt(id.getOption(0)));
            tempEvent.setDescription(descField.getAsString());

            DatabaseHelper.updateEventDescription(tempEvent.getEventID(), tempEvent.getDescription());

            event.editMessage(
                    MessageEditData.fromCreateData( //Converts creating new message to editing previous message
                            tempEvent.createMessage()
                    )
            ).queue();
        }
        else if (id.getComponent(0).equals("eventVoteTime")){
            ModalMapping timeField = event.getValue("time");

            EventPoll tempEvent = EventPoll.options.get(Integer.parseInt(id.getOption(0)));
            tempEvent.setVoteTime(Integer.parseInt(timeField.getAsString()));

            DatabaseHelper.updateEventVoteTime(tempEvent.getEventID(), tempEvent.getVoteTime());

            event.editMessage(
                    MessageEditData.fromCreateData( //Converts creating new message to editing previous message
                            tempEvent.createMessage()
                    )
            ).queue();
        }
        else if (id.getComponent(0).equals("eventReminders")){
            ModalMapping reminderField = event.getValue("reminders");

            EventPoll tempEvent = EventPoll.options.get(Integer.parseInt(id.getOption(0)));
            tempEvent.setDaysBetweenReminders(Integer.parseInt(reminderField.getAsString()));

            DatabaseHelper.updateEventReminders(tempEvent.getEventID(), tempEvent.getDaysBetweenReminders());

            event.editMessage(
                    MessageEditData.fromCreateData( //Converts creating new message to editing previous message
                            tempEvent.createMessage()
                    )
            ).queue();
        }
        else if (id.getComponent(0).equals("neededConfirmations")){
            ModalMapping confirmationsField = event.getValue("confirmations");

            EventPoll tempEvent = EventPoll.options.get(Integer.parseInt(id.getOption(0)));
            tempEvent.setConfirmationsNeeded(Integer.parseInt(confirmationsField.getAsString()));

            DatabaseHelper.updateEventConfirmations(tempEvent.getEventID(), tempEvent.getConfirmationsNeeded());

            event.editMessage(
                    MessageEditData.fromCreateData( //Converts creating new message to editing previous message
                            tempEvent.createMessage()
                    )
            ).queue();
        }
        else if (id.getComponent(0).equals("addOption")){
            ModalMapping confirmationsField = event.getValue("addOption");

            EventPoll tempEvent = EventPoll.options.get(Integer.parseInt(id.getOption(0)));

            tempEvent.addOption(Date.valueOf(confirmationsField.getAsString()));

            event.editMessage(
                    MessageEditData.fromCreateData( //Converts creating new message to editing previous message
                            tempEvent.createMessage()
                    )
            ).queue();
        }
        else if (id.getComponent(0).equals("removeOption")){
            ModalMapping confirmationsField = event.getValue("removeOption");
            EventPoll tempEvent = EventPoll.options.get(Integer.parseInt(id.getOption(0)));

            Boolean success = tempEvent.removeOption(Integer.parseInt(confirmationsField.getAsString()));

            if(success) {
                event.editMessage(
                        MessageEditData.fromCreateData( //Converts creating new message to editing previous message
                                tempEvent.createMessage()
                        )
                ).queue();
            }else{
                event.reply("That option does not exist, please try again.").setEphemeral(true).queue();
            }
        }
    }
}
