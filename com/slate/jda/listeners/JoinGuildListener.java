package com.slate.jda.listeners;

import com.slate.jda.SlashCommand;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class JoinGuildListener extends ListenerAdapter {
    @Override
    public void onGuildJoin (GuildJoinEvent event) {
        event.getGuild().updateCommands().addCommands(SlashCommand.getAllData()).queue();
    }
}
