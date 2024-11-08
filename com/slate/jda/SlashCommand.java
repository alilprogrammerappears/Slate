package com.slate.jda;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.ArrayList;
import java.util.List;

//Repository of all existing slash commands
public enum SlashCommand {
    SHOW_USER_PREFERENCE_MENU (
        Commands.slash("edit_preferences", "Change your preferences.")
    ),
    TEST_CMD (
        Commands.slash("test_command", "WOW!")
    ),
    CREATE_EVENT_POLL (
            Commands.slash("create_event_poll", "Create a poll for scheduling an event")
                    .addOption(OptionType.STRING,"eventname","Event Name",true)
                    .addOption(OptionType.STRING,"description","Event Description",true)
                    .addOption(OptionType.INTEGER,"vote_time","Time for Votes",true)
                    .addOption(OptionType.INTEGER,"confirmations_needed","Confirmations Needed",true)
                    .addOption(OptionType.STRING,"date_created","Date Created",true)
                    .addOption(OptionType.STRING,"option1","Option One",true)
                    .addOption(OptionType.STRING,"option2","Option Two",false)
                    .addOption(OptionType.STRING,"option3","Option Three",false)
                    .addOption(OptionType.STRING,"option4","Option Four",false)
                    .addOption(OptionType.STRING,"option5","Option Five",false)
                    .addOption(OptionType.BOOLEAN,"weekly","Weekly Event",false)
                    .addOption(OptionType.INTEGER,"reminder_frequency","Days Between Reminders",false)
    ),
    ROLE_MENU (
            Commands.slash("role_menu","Create and show a project role menu")
                    .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                    .addSubcommands(
                            new SubcommandData("show_menu","Show role menu to assign roles"),
                            new SubcommandData("create_menu","Create a role menu")
                                    .addOption(OptionType.STRING,"menu_title","Set menu title",true)
                                    .addOption(OptionType.STRING,"menu_description","Set menu description",true),
                            new SubcommandData("change_menu_title","Change the menu title")
                                    .addOption(OptionType.STRING,"menu_name","Menu Name", true),
                            new SubcommandData("change_menu_description","Change the menu description")
                                    .addOption(OptionType.STRING,"menu_description", "Menu description", true),
                            new SubcommandData("add_option","Add a role option")
                                    .addOption(OptionType.ROLE,"role_id","Role ID", true)
                                    .addOption(OptionType.STRING,"role_name","Role Name", true),
                            new SubcommandData("remove_option","Remove a role option")
                                    .addOption(OptionType.ROLE,"role_id","Role ID", true)
                                    .addOption(OptionType.STRING,"role_name","Role Name", true),
                            new SubcommandData("delete_menu", "Delete the role menu")
                    )
    ),
    TASK (
        Commands.slash("create_task", "Create a task for your project!")
            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
    ),
    VOTE_POLL (
        Commands.slash("create_vote_poll", "Create a poll for everyone to vote on!")
            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
    ),
    DISPLAY_CALENDAR (
            Commands.slash("display_event_calendar","Display upcoming events in the next 30 days")
    )
    ;

    public final CommandData data;

    SlashCommand (CommandData data) {
        this.data = data;
    }

    public static List<CommandData> getAllData () {
        List<CommandData> data = new ArrayList<>();
        for (SlashCommand command : values()) {
            data.add(command.data);
        }
        return data;
    }
}
