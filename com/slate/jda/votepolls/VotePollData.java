package com.slate.jda.votepolls;

import java.time.Instant;

public record VotePollData (int pollId, String authorId, String guildId, String question, Instant endDate,
    String channelID, String messageId) {
}
