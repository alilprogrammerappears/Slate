package com.slate.jda.votepolls;

import java.util.List;

public record VotePollOption (int optionId, int pollId, String emoji, String description, int totalVotes) {
    public static String listOptions (List<VotePollOption> options) {
        StringBuilder s = new StringBuilder();
        options.forEach(o -> s.append(o.emoji).append(" ").append(o.description).append("\n"));
        return s.toString().trim();
    }
}
