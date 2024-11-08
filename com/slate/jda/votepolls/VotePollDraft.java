package com.slate.jda.votepolls;

import com.slate.sys.Colors;
import com.slate.sys.Draft;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;

public class VotePollDraft extends Draft {
    public static final int MAX_QUESTION_LENGTH = MessageEmbed.TITLE_MAX_LENGTH;
    public static final int MAX_OPTIONS = 20;
    public static final int MAX_OPTION_DESCRIPTION_LENGTH = 200;
    public static final int MAX_OPTION_EMOJI_LENGTH = 4;

    private final String authorId;
    private final String guildId;
    private String question = null;
    private final LinkedHashMap<String, String> options = new LinkedHashMap<>();
    private Instant endDate = Instant.now().plus(7, ChronoUnit.DAYS);

    public VotePollDraft (String authorId, String guildId) {
        this.authorId = authorId;
        this.guildId = guildId;
    }

    public String listOptions () {
        StringBuilder s = new StringBuilder();
        options.forEach((e, d) -> s.append(e).append(" ").append(d).append("\n"));
        return s.toString().trim();
    }

    public MessageEditData createDraftMenu () {
        MessageEditBuilder message = new MessageEditBuilder();

        EmbedBuilder embed = new EmbedBuilder().setColor(Colors.APPROVED)
            .setTitle((question != null) ? question : "Set your poll's question!")
            .setDescription(options.size() > 0 ? listOptions() : "Add options to your poll!")
            .addField("End Date", "<t:" + endDate.getEpochSecond() + ":f> (<t:" + endDate.getEpochSecond() + ":R>)", false);

        List<ActionRow> rows = List.of(
            ActionRow.of(
                Button.primary("vp_setQuestion:" + getId(), "Set Question"),
                Button.primary("vp_setEndDate:" + getId(), "Set End Date")
            ),
            ActionRow.of(
                Button.primary("vp_addOption:" + getId(), "Add Option").withDisabled( options.size() >= MAX_OPTIONS),
                Button.danger("vp_remOption:" + getId(), "Remove Option").withDisabled(options.size() == 0)
            ),
            ActionRow.of(
                Button.success("vp_post:" + getId(), "Post").withDisabled(!isValid())
            )
        );

        return message.setEmbeds(embed.build()).setComponents(rows).build();
    }
    public List<ActionRow> createRemoveMenu () {
        StringSelectMenu.Builder removeMenu = StringSelectMenu.create("vp_remOption:" + getId()).setRequiredRange(1, 1)
            .setPlaceholder("Select an option to remove");
        options.forEach((e, d) -> removeMenu.addOption(d, e, Emoji.fromFormatted(e)));
        return List.of(ActionRow.of(removeMenu.build()), ActionRow.of(Button.danger("vp_cancelDel:" + getId(), "Back")));
    }

    public void addOption (String emoji, String description) {
        options.put(emoji, description);
    }
    public void removeOption (String emoji) {
        options.remove(emoji);
    }

    @Override
    public void expire () {

    }

    public boolean isValid () {
        return question != null && options.size() >= 2 && endDate.isAfter(Instant.now());
    }
    public String getAuthorId () {
        return authorId;
    }
    public String getGuildId () {
        return guildId;
    }
    public String getQuestion () {
        return question;
    }
    public LinkedHashMap<String, String> getOptions () {
        return options;
    }
    public Instant getEndDate () {
        return endDate;
    }

    public void setQuestion (String question) {
        this.question = question;
    }
    public void setEndDate (Instant endDate) {
        this.endDate = endDate;
    }
}
