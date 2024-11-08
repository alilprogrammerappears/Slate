package com.slate.jda.tasks;

import com.slate.sys.Colors;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.time.Instant;

public class Task {
    private final int id;
    private final String guildId;
    private final String title;
    private final String description;
    private final String assignedRoleId;
    private final Instant dueDate;
    private final boolean complete;

    public Task (int id, String guildId, String title, String description, String assignedRoleId, long dueDate,
        boolean complete) {
        this.id = id;
        this.guildId = guildId;
        this.title = title;
        this.description = description;
        this.assignedRoleId = assignedRoleId;
        this.dueDate = Instant.ofEpochSecond(dueDate);
        this.complete = complete;
    }

    public static MessageEmbed createEmbed (String title, String description, String assignedRoleId, Instant dueDate,
        boolean complete) {
        EmbedBuilder embed = new EmbedBuilder().setColor(Colors.APPROVED)
            .setTitle(title)
            .setDescription(description)
            .addField("Assigned Project Role", "<@&" + assignedRoleId + ">", false)
            .addField("Due Date", "<t:" + dueDate.getEpochSecond() + ":f> " +
                "(<t:" + dueDate.getEpochSecond() + ":R>)", false)
            .addField("Completed", (complete) ? "Yes" : "No", false);

        return embed.build();
    }
    public MessageCreateData createMessage () {
        return new MessageCreateBuilder().setEmbeds(createEmbed(title, description, assignedRoleId, dueDate, complete))
            .setComponents(ActionRow.of(Button.success("completeTask:" + id, "Complete Task").withDisabled(complete)))
            .build();
    }

    public String getAssignedRoleId () {
        return assignedRoleId;
    }
}
