package com.slate.jda.listeners;

import com.slate.jda.EventPoll;
import com.slate.jda.MonthlyCalendar;
import com.slate.jda.ProjectRoleMenu;
import com.slate.jda.SlashCommand;
import com.slate.jda.tasks.TaskDraft;
import com.slate.jda.votepolls.VotePollDraft;
import com.slate.sys.DatabaseHelper;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.sql.Date;

public class SlashCommandListener extends ListenerAdapter {
    ProjectRoleMenu roleMenu = new ProjectRoleMenu();

    @Override
    public void onSlashCommandInteraction (SlashCommandInteractionEvent event) {
        String cmd = event.getName();
        String subCmd = event.getSubcommandName();

        if (cmd.equals(SlashCommand.SHOW_USER_PREFERENCE_MENU.data.getName())) {
            try {
                event.reply(DatabaseHelper.getUserPreferences(event.getUser().getId()).orElseThrow().createMessage())
                        .setEphemeral(true).queue();
            }
            catch (Exception e) {
                event.reply("An error occurred getting your user preferences! Please try again.")
                        .setEphemeral(true).queue();
            }
        }
        else if (cmd.equals(SlashCommand.CREATE_EVENT_POLL.data.getName())) {

            //These are for all optional arguments
            Date option2 = null;
            Date option3 = null;
            Date option4 = null;
            Date option5 = null;
            Boolean weekly = false;
            int reminder_frequency = 0;

            OptionMapping option2Option = event.getOption("option2");
            OptionMapping option3Option = event.getOption("option3");
            OptionMapping option4Option = event.getOption("option4");
            OptionMapping option5Option = event.getOption("option5");
            OptionMapping weeklyOption = event.getOption("weekly");
            OptionMapping reminderFrequencyOption = event.getOption("reminder_frequency");

            if (option2Option != null) {
                option2 = Date.valueOf(option2Option.getAsString());
            }
            if (option3Option != null) {
                option3 = Date.valueOf(option3Option.getAsString());
            }
            if (option4Option != null) {
                option4 = Date.valueOf(option4Option.getAsString());
            }
            if (option5Option != null) {
                option5 = Date.valueOf(option5Option.getAsString());
            }
            if (weeklyOption != null) {
                weekly = weeklyOption.getAsBoolean();
            }
            if (reminderFrequencyOption != null) {
                reminder_frequency = reminderFrequencyOption.getAsInt();
            }

            EventPoll tempEvent = DatabaseHelper.addEvent(
                    event.getOption("eventname").getAsString(),
                    event.getOption("description").getAsString(),
                    Date.valueOf(event.getOption("date_created").getAsString()),
                    event.getOption("vote_time").getAsInt(),
                    event.getOption("confirmations_needed").getAsInt(),
                    Date.valueOf(event.getOption("option1").getAsString()),
                    option2,
                    option3,
                    option4,
                    option5,
                    weekly,
                    reminder_frequency
            );
            if(tempEvent != null) {
                event.reply(tempEvent.createMessage()).setEphemeral(true).queue();
            }
        }
        else if (cmd.equals(SlashCommand.ROLE_MENU.data.getName())){

            //continue if subCmd is not null
            if (subCmd != null) {

                if (subCmd.equals("show_menu")) {
                    try {
                        event.reply(roleMenu.showRoleVotingMenu(roleMenu)).setEphemeral(false).queue();
                    } catch (Exception e) {
                        event.reply("A menu hasn't been made, please create one first!")
                                .setEphemeral(true).queue();
                    }
                }
                else if (subCmd.equals("create_menu")) {
                    String menuTitle = event.getOption("menu_title").getAsString();
                    String menuDescription = event.getOption("menu_description").getAsString();
                    roleMenu.setName(menuTitle);
                    roleMenu.setDescription(menuDescription);
                    event.reply("A menu has been created, please add options").setEphemeral(true).queue();
                }
                else if (subCmd.equals("change_menu_title")) {
                    String menuName = event.getOption("menu_name").getAsString();
                    roleMenu.setName(menuName);
                    event.reply("The menu's name is now " + roleMenu.getName()).setEphemeral(true).queue();
                }
                else if (subCmd.equals("change_menu_description")) {
                    String menuDescription = event.getOption("menu_description").getAsString();
                    roleMenu.setDescription(menuDescription);
                    event.reply("The menu's description is now " + roleMenu.getDescription())
                            .setEphemeral(true).queue();
                }
                else if (subCmd.equals("add_option")) {
                    String name = event.getOption("role_name").getAsString();
                    Role role = event.getOption("role_id").getAsRole();
                    System.out.println(role.getName());

                    if (roleMenu.addOption(name, role)) {
                        event.reply("The role, " + name + " has been added!")
                                .setEphemeral(true)
                                .queue();
                    }
                    else {
                        event.reply("Role was not added because something went wrong :(")
                                .setEphemeral(true).queue();
                    }
                }
                else if (subCmd.equals("remove_option")) {

                    String name = event.getOption("role_name").getAsString();
                    Role role = event.getOption("role_id").getAsRole();

                    if (roleMenu.removeOption(name, role)) {
                        event.reply("Role has been removed!").setEphemeral(true).queue();
                    }
                    else {
                        event.reply("Role was not removed because something went wrong" +
                                        " haha loser get better at programming")
                                .setEphemeral(true)
                                .queue();
                    }
                }
                else if (subCmd.equals("delete_menu")) {

                    if (roleMenu.deleteMenu()) {
                        event.reply("Alright, it's done and there is no going back.")
                                .setEphemeral(true).queue();
                    }
                    else {
                        event.reply("The menu has been deleted. Get your head in the game and make a new one!")
                                .setEphemeral(true)
                                .queue();
                    }
                }
                else {
                    event.reply("This didn't work :(").setEphemeral(true).queue();
                }
            }
        }
        else if (cmd.equals(SlashCommand.TASK.data.getName())) {
            if (event.getGuild() == null) {
                event.reply("Oops! This command can only be used in servers!").setEphemeral(true).queue();
                return;
            }

            event.reply(MessageCreateData.fromEditData(new TaskDraft(event.getGuild().getId()).createMessage()))
                    .setEphemeral(true).queue();
        }
        else if (cmd.equals(SlashCommand.VOTE_POLL.data.getName())) {
            if (event.getGuild() == null) {
                event.reply("Oops! This command can only be used in servers!").setEphemeral(true).queue();
                return;
            }

            event.reply(MessageCreateData.fromEditData(new VotePollDraft(event.getUser().getId(),
                    event.getGuild().getId()).createDraftMenu())).setEphemeral(true).queue();
        }
        else if (cmd.equals(SlashCommand.DISPLAY_CALENDAR.data.getName())) {
            if (event.getGuild() == null) {
                event.reply("Oops! This command can only be used in servers!").setEphemeral(true).queue();
                return;
            }
            event.reply(MessageCreateData.fromContent(MonthlyCalendar.show())).setEphemeral(true).queue();
        }
    }
}