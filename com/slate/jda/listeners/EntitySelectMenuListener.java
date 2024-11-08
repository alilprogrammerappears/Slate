package com.slate.jda.listeners;

import com.slate.jda.EventId;
import com.slate.jda.tasks.Task;
import com.slate.jda.tasks.TaskDraft;
import com.slate.jda.votepolls.VotePoll;
import com.slate.jda.votepolls.VotePollDraft;
import com.slate.sys.DatabaseHelper;
import com.slate.sys.Draft;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Optional;

public class EntitySelectMenuListener extends ListenerAdapter {
    @Override
    public void onEntitySelectInteraction (EntitySelectInteractionEvent event) {
        EventId id = new EventId(event.getComponentId());

        if (id.getComponent(0).equals("task")) {
            Optional<TaskDraft> taskDraft = Draft.get(TaskDraft.class, Integer.parseInt(id.getOption(0)));
            taskDraft.ifPresent(Draft::refresh);
            if (taskDraft.isEmpty()) {
                event.editMessage("This draft no longer exists.").setEmbeds().setComponents().queue();
                return;
            }

            switch (id.getComponent(1)) {
                case "role" -> {
                    taskDraft.get().setAssignedRoleId(event.getMentions().getRoles().get(0).getId());
                    event.editMessage(taskDraft.get().createMessage()).queue();
                }
                case "post" -> {
                    TextChannel channel = (TextChannel) event.getMentions().getChannels().get(0);
                    if (!channel.canTalk()) {
                        event.reply("Oops! I can't send messages in the selected channel. Please select another one.")
                            .setEphemeral(true).queue();
                        return;
                    }

                    int taskId = DatabaseHelper.addTask(taskDraft.get());
                    Optional<Task> task = DatabaseHelper.getTask(taskId);
                    if (taskId == -1 || task.isEmpty()) {
                        event.reply("Oops! An error occurred adding your task to our database. Please try again later.")
                            .setEphemeral(true).queue();
                        return;
                    }

                    Draft.remove(taskDraft.get().getId());
                    channel.sendMessage(task.get().createMessage()).queue();
                    event.editMessage("Your task has been posted!").setEmbeds().setComponents().queue();
                }
            }
        }
        else if (id.getComponent(0).equals("vp") && id.getComponent(1).equals("post")) {
            Optional<VotePollDraft> votePollDraft = Draft.get(VotePollDraft.class, Integer.parseInt(id.getOption(0)));
            votePollDraft.ifPresent(Draft::refresh);
            if (votePollDraft.isEmpty()) {
                event.editMessage("This poll draft no longer exists.").setEmbeds().setComponents().queue();
                return;
            }

            TextChannel channel = (TextChannel) event.getMentions().getChannels().get(0);
            if (channel.canTalk()) {
                try {
                    int pollId = DatabaseHelper.addVotePoll(votePollDraft.get());
                    VotePoll votePoll = VotePoll.fromId(pollId);
                    event.getChannel().sendMessage(votePoll.createMessage()).queue(m ->
                        DatabaseHelper.updateDiscordIds(pollId, m.getChannel().getId(), m.getId()));
                    event.editMessage("Your poll has been posted!").setEmbeds().setComponents().queue();
                    Draft.remove(votePollDraft.get().getId());
                }
                catch (Exception e) {
                    event.reply("Oops! Something went wrong posting your poll! Please try again later.")
                        .setEphemeral(true).queue();
                }
            }
            else {
                event.editMessage("Oops! I don't have permission to send messages in the selected channel.\n" +
                    "Select a channel from the dropdown below to send this poll to.").queue();
            }
        }
    }
}
