package com.slate.jda.votepolls;

import com.slate.sys.Colors;
import com.slate.sys.DatabaseHelper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class VotePoll {
    public static ScheduledExecutorService POLL_UPDATER = new ScheduledThreadPoolExecutor(1);

    private final VotePollData data;
    private final List<VotePollOption> options;

    private VotePoll (VotePollData data, List<VotePollOption> options) {
        this.data = data;
        this.options = options;
    }

    public static VotePoll fromId (int pollId) {
        VotePollData data = DatabaseHelper.getVotePollData(pollId)
            .orElseThrow(() -> new IllegalStateException("Failed to get vote poll data for vote poll " + pollId));
        List<VotePollOption> options = DatabaseHelper.getVotePollOptions(pollId);
        if (options.size() < 2) { throw new IllegalStateException("Failed to get all options for vote poll " + pollId); }
        return new VotePoll(data, options);
    }

    public List<ActionRow> createVoteButtons () {
        List<ActionRow> rows = new ArrayList<>();

        List<Button> buttons = new ArrayList<>();
        for (VotePollOption option : options) {
            buttons.add(Button.secondary("vpv:" + option.pollId() + ":" + option.optionId(),
                String.valueOf(option.totalVotes())).withEmoji(Emoji.fromFormatted(option.emoji())));

            if (buttons.size() >= 5 || option == options.get(options.size() - 1)) {
                rows.add(ActionRow.of(buttons));
                buttons.clear();
            }
        }

        return rows;
    }
    public MessageCreateData createMessage () {
        MessageCreateBuilder message = new MessageCreateBuilder();

        EmbedBuilder embed = new EmbedBuilder().setColor(Colors.PENDING)
            .setTitle(data.question()).setDescription(VotePollOption.listOptions(options))
            .addField("End Date", "<t:" + data.endDate().getEpochSecond() + ":f> " +
                "(<t:" + data.endDate().getEpochSecond() + ":R>)", false);

        return message.setEmbeds(embed.build()).setComponents(createVoteButtons()).build();
    }
    public MessageEditData createResultMessage () {
        MessageEditBuilder message = new MessageEditBuilder();

        StringBuilder s  = new StringBuilder();
        options.forEach(o -> s.append(o.emoji()).append(" ").append(o.description()).append(" - ").append(o.totalVotes())
            .append(" votes!").append("\n"));

        EmbedBuilder embed = new EmbedBuilder().setColor(Colors.APPROVED)
            .setTitle(data.question()).setDescription(s.toString().trim());

        return message.setEmbeds(embed.build()).setComponents().build();
    }

    // Thread code
    public static Runnable getEndPollsRunnable (JDA jda) {
        return () -> DatabaseHelper.getDueVotePolls(2).forEach(id -> {
            try {
                VotePoll poll = fromId(id);
                Objects.requireNonNull(Objects.requireNonNull(jda.getGuildById(poll.data.guildId()))
                        .getTextChannelById(poll.data.channelID()))
                    .retrieveMessageById(poll.data.messageId())
                    .complete().editMessage(poll.createResultMessage()).queue();
                DatabaseHelper.removeVotePoll(poll.data.pollId());
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
