package com.slate.jda.tasks;

import com.slate.sys.Colors;
import com.slate.sys.Draft;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class TaskDraft extends Draft {
    private final String guildId;
    private String title = "";
    private String description = "";
    private String assignedRoleId = null;
    private Instant dueDate = Instant.now().plus(7, ChronoUnit.DAYS);

    public TaskDraft (String guildId) {
        super();
        this.guildId = guildId;
    }

    public MessageEditData createMessage () {
        MessageEditBuilder message = new MessageEditBuilder();

        EmbedBuilder embed = new EmbedBuilder().setColor(Colors.APPROVED)
            .setTitle("Create a new task!")
            .addField("Title", title, false)
            .addField("Description", description, false)
            .addField("Assigned Project Role", (assignedRoleId != null) ? "<@&" + assignedRoleId + ">" : "", false)
            .addField("Due Date", "<t:" + dueDate.getEpochSecond() + ":f> " +
                "(<t:" + dueDate.getEpochSecond() + ":R>)", false);

        List<ActionRow> rows = List.of(
            ActionRow.of(
                Button.primary("task_title:" + getId(), "Set Title"),
                Button.primary("task_desc:" + getId(), "Set Description"),
                Button.primary("task_due:" + getId(), "Set Due Date"),
                Button.success("task_post:" + getId(), "Post Task").withDisabled(!isValid())
            ),
            ActionRow.of(
                EntitySelectMenu.create("task_role:" + getId(), EntitySelectMenu.SelectTarget.ROLE)
                    .setPlaceholder("Select a role to assign this task to.")
                    .setRequiredRange(1, 1)
                    .build()
            )
        );

        return message.setEmbeds(embed.build()).setComponents(rows).build();
    }

    public MessageEditData createPostMessage () {
        MessageEditBuilder message = new MessageEditBuilder()
            .setContent("Here is a preview of your task! Select a text channel to post the task in or click back to " +
                "continue making edits.");

        List<ActionRow> rows = List.of(
            ActionRow.of(
                EntitySelectMenu.create("task_post:" + getId(), EntitySelectMenu.SelectTarget.CHANNEL)
                    .setChannelTypes(ChannelType.TEXT)
                    .setRequiredRange(1, 1)
                    .setPlaceholder("Select a channel to post the task in!")
                    .build()
            ),
            ActionRow.of(
                Button.danger("task_cancelPost:" + getId(), "Cancel")
            )
        );

        return message.setEmbeds(Task.createEmbed(title, description, assignedRoleId, dueDate, false))
            .setComponents(rows).build();
    }

    @Override
    public void expire () {

    }

    public boolean isValid () {
        return !title.isBlank() && assignedRoleId != null && dueDate.isAfter(Instant.now());
    }
    public String getGuildId () {
        return guildId;
    }
    public String getTitle () {
        return title;
    }
    public String getDescription () {
        return description;
    }
    public String getAssignedRoleId () {
        return assignedRoleId;
    }
    public long getDueDateLong () {
        return dueDate.getEpochSecond();
    }

    public void setTitle (String title) {
        this.title = title;
    }
    public void setDescription (String description) {
        this.description = description;
    }
    public void setAssignedRoleId (String assignedRoleId) {
        this.assignedRoleId = assignedRoleId;
    }
    public void setDueDate (Instant dueDate) {
        this.dueDate = dueDate;
    }
}