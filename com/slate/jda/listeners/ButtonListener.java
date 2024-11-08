package com.slate.jda.listeners;

import com.slate.jda.EventId;
import com.slate.jda.EventPoll;
import com.slate.jda.tasks.Task;
import com.slate.jda.tasks.TaskDraft;
import com.slate.jda.votepolls.VotePoll;
import com.slate.jda.votepolls.VotePollDraft;
import com.slate.jda.votepolls.VotePollOption;
import com.slate.sys.DatabaseHelper;
import com.slate.sys.Draft;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.w3c.dom.Text;

import java.util.Optional;
import java.util.stream.Stream;

public class ButtonListener extends ListenerAdapter {
    @Override
    public void onButtonInteraction (ButtonInteractionEvent event) {
        EventId id = new EventId(event.getComponentId());

        if (id.getComponent(0).equals("up")) {
            switch (id.getComponent(1)) {
                case "phoneNum" -> event.replyModal(Modal.create("up_phoneNum", "Change your phone number")
                    .addComponents(ActionRow.of(TextInput.create("phoneNum", "Enter your phone number:",
                        TextInputStyle.SHORT).setRequiredRange(10, 10).build())).build()).queue();
                case "email" -> event.replyModal(Modal.create("up_emailAddress", "Change your email address")
                    .addComponents(ActionRow.of(TextInput.create("email", "Enter your email address:",
                        TextInputStyle.SHORT).setRequiredRange(1, 200).build())).build()).queue();
                case "toggle" -> {
                    DatabaseHelper.toggleNotification(event.getUser().getId(), id.getOption(0));
                    event.editMessage(MessageEditData.fromCreateData(DatabaseHelper
                        .getUserPreferences(event.getUser().getId()).orElseThrow().createMessage())).queue();
                }
            }
        }
        else if (id.getComponent(0).equals("task")) {
            Optional<TaskDraft> taskDraft = Draft.get(TaskDraft.class, Integer.parseInt(id.getOption(0)));
            taskDraft.ifPresent(Draft::refresh);
            if (taskDraft.isEmpty()) {
                event.editMessage("This draft no longer exists.").setEmbeds().setComponents().queue();
                return;
            }

            switch (id.getComponent(1)) {
                case "title" -> event.replyModal(Modal.create("task_title:" + id.getOption(0), "Change the title of your task")
                    .addComponents(ActionRow.of(TextInput.create("title", "Enter the name of your task:",
                        TextInputStyle.SHORT).setRequiredRange(1, MessageEmbed.TITLE_MAX_LENGTH).build())).build()).queue();
                case "desc" -> event.replyModal(Modal.create("task_desc:" + id.getOption(0), "Change the description of your task")
                    .addComponents(ActionRow.of(TextInput.create("desc", "Enter the description of your task:",
                        TextInputStyle.PARAGRAPH).setRequiredRange(1, MessageEmbed.VALUE_MAX_LENGTH).build())).build()).queue();
                case "due" -> event.replyModal(Modal.create("task_due:" + id.getOption(0), "Set the due date of your task:")
                    .addComponents(ActionRow.of(TextInput.create("date", "Enter date as a UNIX timestamp:",
                        TextInputStyle.SHORT).setRequiredRange(9, 11).build())).build()).queue();
                case "post" -> event.editMessage(taskDraft.get().createPostMessage()).queue();
                case "cancelPost" -> event.editMessage(taskDraft.get().createMessage()).queue();
            }
        }
        else if (id.getComponent(0).equals("completeTask")) {
            Member member = event.getMember();
            Optional<Task> task = DatabaseHelper.getTask(Integer.parseInt(id.getOption(0)));
            if (task.isEmpty() || member == null) {
                event.reply("Oops! Something went wrong marking this task as completed! Please try again later.")
                    .setEphemeral(true).queue();
                return;
            }

            if (member.getRoles().stream().flatMap(r -> Stream.of(r.getId())).toList()
                .contains(task.get().getAssignedRoleId()) && DatabaseHelper.completeTask(Integer.parseInt(id.getOption(0)))) {
                task = DatabaseHelper.getTask(Integer.parseInt(id.getOption(0)));
                event.editMessage(MessageEditData.fromCreateData(task.orElseThrow().createMessage())).queue();
            }
            else {
                event.reply("Only users with the role <@&" + task.get().getAssignedRoleId() + "> can complete this task.")
                    .setEphemeral(true).queue();
            }
        }
        else if (id.getComponent(0).equals("eventChange")) {
            EventPoll tempEvent = EventPoll.options.get(Integer.parseInt(id.getOption(0))); //Not needed here, but good to have

            switch (id.getComponent(1)) {
                case "Name" -> {

                    TextInput nameField = TextInput.create("name",      //This is what the modal mapping checks for
                            "Enter your new event name below", //Small text above textbox
                            TextInputStyle.SHORT     //Length of textbox
                        ).setRequired(true) //Text box must have characters in it
                        .setPlaceholder("Event name here") //Greyed out example text
                        .build();

                    Modal eventName = Modal.create("eventName:" + id.getOption(0), //This is what the modal listener checks for
                            "Your event new name").addActionRow(nameField) //Creates row to store fields
                        .build();

                    event.replyModal(eventName).queue();

                }
                case "Description" -> {
                    TextInput descriptionField = TextInput.create("eventDesc", "Enter your new description below", //Small text above textbox
                            TextInputStyle.PARAGRAPH     //Length of textbox
                        ).setRequired(true) //Text box must have characters in it
                        .setPlaceholder("Description...") //Greyed out example text
                        .setValue("Default") //Default text
                        .build();

                    Modal descriptionName = Modal.create("eventDescription:" + id.getOption(0), "Your event new description").addActionRow(descriptionField) //Creates row to store fields
                        .build();

                    event.replyModal(descriptionName).queue();
                }
                case "VoteTime" -> {

                    TextInput timeField = TextInput.create("time",      //This is what the modal mapping checks for
                            "Enter your new time to vote", //Small text above textbox
                            TextInputStyle.SHORT     //Length of textbox
                        ).setRequired(true) //Text box must have characters in it
                        .setPlaceholder("How many days left to vote?") //Greyed out example text
                        .build();

                    Modal eventVoteTime = Modal.create("eventVoteTime:" + id.getOption(0), //This is what the modal listener checks for
                            "Your event new vote time").addActionRow(timeField) //Creates row to store fields
                        .build();

                    event.replyModal(eventVoteTime).queue();
                }
                case "Reminders" -> {

                    TextInput reminderField = TextInput.create("reminders",      //This is what the modal mapping checks for
                            "Enter your new reminder frequency below", //Small text above textbox
                            TextInputStyle.SHORT     //Length of textbox
                        ).setRequired(true) //Text box must have characters in it
                        .setPlaceholder("1 for every day, 2 for every 2 days, etc.") //Greyed out example text
                        .build();

                    Modal eventName = Modal.create("eventReminders:" + id.getOption(0), //This is what the modal listener checks for
                            "Your event reminder frequency").addActionRow(reminderField) //Creates row to store fields
                        .build();

                    event.replyModal(eventName).queue();
                }
                case "NeededConfirmations" -> {

                    TextInput confirmationsField = TextInput.create("confirmations",      //This is what the modal mapping checks for
                            "Your new number of required confirmations", //Small text above textbox
                            TextInputStyle.SHORT     //Length of textbox
                        ).setRequired(true) //Text box must have characters in it
                        .setPlaceholder("1, 2, etc.") //Greyed out example text
                        .build();

                    Modal eventName = Modal.create("neededConfirmations:" + id.getOption(0), //This is what the modal listener checks for
                            "Your event new confirmations needed").addActionRow(confirmationsField) //Creates row to store fields
                        .build();

                    event.replyModal(eventName).queue();
                }
                case "Weekly" -> {

                    if (tempEvent.getWeekly()) {
                        tempEvent.setWeekly(false);
                        event.reply("Your event is no longer weekly!").setEphemeral(true).queue();
                    } else {
                        tempEvent.setWeekly(true);
                        event.reply("Your event is now weekly!").setEphemeral(true).queue();
                    }

                    DatabaseHelper.updateWeekly(tempEvent.getEventID(), tempEvent.getWeekly());

                }
            }
        }
        else if (id.getComponent(0).equals("eventVote")) {
            EventPoll tempEvent = EventPoll.options.get(Integer.parseInt(id.getOption(0)));
            MessageCreateBuilder message = new MessageCreateBuilder();

            switch (id.getComponent(1)) {
                case "Add" -> {

                    if (tempEvent.getOption2() == null || tempEvent.getOption3() == null || tempEvent.getOption4() == null || tempEvent.getOption5() == null) {
                        TextInput optionField = TextInput.create("addOption",      //This is what the modal mapping checks for
                                "Enter your new option below", //Small text above textbox
                                TextInputStyle.SHORT     //Length of textbox
                            ).setRequired(true) //Text box must have characters in it
                            .setPlaceholder("yyyy-MM-dd HH:mm") //Greyed out example text
                            .build();

                        Modal eventOption = Modal.create("addOption:" + id.getOption(0), //This is what the modal listener checks for
                                "Your event new option").addActionRow(optionField) //Creates row to store fields
                            .build();

                        event.replyModal(eventOption).queue();
                    } else {
                        event.reply("Your event already has five options, please remove an option and try again!").setEphemeral(true).queue();
                    }
                }
                case "Remove" -> {

                    if (tempEvent.getOption2() != null || tempEvent.getOption3() != null || tempEvent.getOption4() != null || tempEvent.getOption5() != null) {
                        TextInput removeField = TextInput.create("removeOption",      //This is what the modal mapping checks for
                                "Enter your option to delete", //Small text above textbox
                                TextInputStyle.SHORT     //Length of textbox
                            ).setRequired(true) //Text box must have characters in it
                            .setPlaceholder("2, 3, 4, or 5") //Greyed out example text
                            .build();

                        Modal eventOption = Modal.create("removeOption:" + id.getOption(0), //This is what the modal listener checks for
                                "Your option to remove").addActionRow(removeField) //Creates row to store fields
                            .build();

                        event.replyModal(eventOption).queue();
                    } else {
                        event.reply("Your first option cannot be deleted, if you would like to change it, please make a new event poll.").setEphemeral(true).queue();
                    }
                }
            }

        }
        else if (id.getComponent(0).equals("publishEventPoll")) {

            EventPoll tempEvent = EventPoll.options.get(Integer.parseInt(id.getOption(0)));
            event.reply(tempEvent.publishEventPoll(event)).queue();

        }
        else if (id.getComponent(0).equals("event")) {
            MessageCreateBuilder message = new MessageCreateBuilder();

            Boolean announced = false;

            switch (id.getComponent(1)) {
                case "confirm" -> {
                    EventPoll tempEvent = EventPoll.options.get(Integer.parseInt(id.getOption(2)));

                    DatabaseHelper.RecordVote(event.getUser().getId(), id.getOption(1), "option" +
                            id.getOption(0) + "Response", true);

                    if(tempEvent.checkEventSuccess() && !tempEvent.getPublished()){
                        tempEvent.setPublished(true);
                        announced = true;
                        event.reply(tempEvent.publishCompleteEvent()).queue();
                        tempEvent.beginEvent((TextChannel)event.getChannel()); //This is sketchy but trust me
                    }
                }
                case "deny" -> {
                    DatabaseHelper.RecordVote(event.getUser().getId(), id.getOption(1), "option" +
                            id.getOption(0) + "Response", false);
                }
            }
            if(!announced){
                message.setContent("Your vote has been recorded");
                event.reply(message.build()).setEphemeral(true).queue();
            }
        }
        else if (id.getComponent(0).equals("vp")) {
            Optional<VotePollDraft> votePollDraft = Draft.get(VotePollDraft.class, Integer.parseInt(id.getOption(0)));
            votePollDraft.ifPresent(Draft::refresh);
            if (votePollDraft.isEmpty()) {
                event.editMessage("This draft no longer exists.").setEmbeds().setComponents().queue();
                return;
            }

            switch (id.getComponent(1)) {
                case "setQuestion" -> event.replyModal(Modal.create("vp_setQuestion:" + id.getOption(0), "Set your poll's question!")
                    .addComponents(ActionRow.of(TextInput.create("question", "Enter your poll's question:",
                        TextInputStyle.SHORT).setRequiredRange(1, VotePollDraft.MAX_QUESTION_LENGTH).build())).build()).queue();
                case "setEndDate" -> event.replyModal(Modal.create("vp_setEndDate:" + id.getOption(0), "Set the end date of your poll!")
                    .addComponents(ActionRow.of(TextInput.create("date", "Enter date as a UNIX timestamp:",
                        TextInputStyle.SHORT).setRequiredRange(9, 11).build())).build()).queue();
                case "addOption" -> event.replyModal(Modal.create("vp_addOption:" + id.getOption(0), "Add an option to your poll!")
                    .addComponents(
                        ActionRow.of(TextInput.create("emoji", "Enter a unicode emoji below", TextInputStyle.SHORT)
                            .setRequiredRange(1, VotePollDraft.MAX_OPTION_EMOJI_LENGTH).build()),
                        ActionRow.of(TextInput.create("desc", "Enter the option below", TextInputStyle.SHORT)
                            .setRequiredRange(1, VotePollDraft.MAX_OPTION_DESCRIPTION_LENGTH).build())).build())
                    .queue();
                case "remOption" -> event.editComponents(votePollDraft.get().createRemoveMenu()).queue();
                case "cancelDel" -> event.editMessage(votePollDraft.get().createDraftMenu()).queue();
                case "post" -> {
                    if (event.getChannel().canTalk()) {
                        try {
                            int pollId = DatabaseHelper.addVotePoll(votePollDraft.get());
                            VotePoll votePoll = VotePoll.fromId(pollId);
                            event.getChannel().sendMessage(votePoll.createMessage()).queue(m ->
                                DatabaseHelper.updateDiscordIds(pollId, m.getChannel().getId(), m.getId()));
                            event.editMessage("Your poll has been posted!").setEmbeds().setComponents().queue();
                            Draft.remove(votePollDraft.get().getId());
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                            event.reply("Oops! Something went wrong posting your poll! Please try again later.")
                                .setEphemeral(true).queue();
                        }
                    }
                    else {
                        event.editMessage("Oops! I don't have permission to send messages in this channel.\n" +
                            "Select a channel from the dropdown below to send this poll to.").setComponents(
                                ActionRow.of(EntitySelectMenu.create("vp_post:" + votePollDraft.get().getId(),
                                    EntitySelectMenu.SelectTarget.CHANNEL).setChannelTypes(ChannelType.TEXT)
                                    .setRequiredRange(1, 1).setPlaceholder("Select a channel to send this poll to")
                                    .build())).queue();
                    }
                }
            }
        }
        else if (id.getComponent(0).equals("vpv")) {
            try {
                VotePollOption option = DatabaseHelper.setUserVotePollVote(event.getUser().getId(),
                    Integer.parseInt(id.getOption(0)), Integer.parseInt(id.getOption(1))).orElseThrow(() ->
                    new IllegalStateException("Failed to get option data for voted option " + id.getOption(1)));
                event.reply("You have set your vote to \"" + option.description() + "\"").setEphemeral(true).queue();

                try {
                    event.getMessage().editMessage(MessageEditData.fromCreateData(VotePoll.fromId(option.pollId())
                        .createMessage())).queue();
                }
                catch (Exception ignored) {}
            }
            catch (Exception e) {
                event.reply("Oops! Something went wrong recording your vote! Please try again later.")
                    .setEphemeral(true).queue();
            }
        }
    }
}
