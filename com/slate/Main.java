package com.slate;

import com.slate.jda.listeners.StartupListener;
import com.slate.jda.votepolls.VotePoll;
import com.slate.sys.ArgumentManager;
import com.slate.sys.ConnectionPool;
import com.slate.sys.DatabaseHelper;
import com.slate.sys.Draft;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.sql.SQLException;
import java.util.List;

public class Main {
    public static final ArgumentManager ARGS = new ArgumentManager("=");
    private static JDA jda;

    public static void main (String[] args) throws SQLException {
        // Process args
        ARGS.setSource(args);

        // Verify database connection
        String url = ARGS.get("databaseUrl").orElseThrow(() ->
            new IllegalArgumentException("Missing argument for database url. Add it with the key \"databaseUrl\""));
        String username = ARGS.get("databaseUsername").orElseThrow(() ->
            new IllegalArgumentException("Missing argument for database username. Add it with the key \"databaseUsername\""));
        String password = ARGS.get("databasePassword").orElseThrow(() ->
            new IllegalArgumentException("Missing argument for database password. Add it with the key \"databasePassword\""));
        DatabaseHelper.setConnectionPool(ConnectionPool.create(url, username, password, 20));

        // Create bot
        List<GatewayIntent> intents = List.of(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.DIRECT_MESSAGE_REACTIONS);
        List<CacheFlag> disabledCaches = List.of(CacheFlag.ACTIVITY, CacheFlag.VOICE_STATE, CacheFlag.EMOJI,
            CacheFlag.STICKER, CacheFlag.CLIENT_STATUS, CacheFlag.ONLINE_STATUS, CacheFlag.SCHEDULED_EVENTS);
        jda = JDABuilder.create(ARGS.get("token").orElseThrow(() ->
                new IllegalArgumentException("Missing argument for bot token. Add it with the key \"token\"")), intents)
            .disableCache(disabledCaches)
            .setMemberCachePolicy(MemberCachePolicy.ONLINE)
            .setChunkingFilter(ChunkingFilter.NONE)
            .setStatus(OnlineStatus.IDLE)
            .addEventListeners(new StartupListener())
            .build();
    }

    public static void shutdown() {
        System.out.println("Stopping threads...");
        StartupListener.CLI_THREAD.shutdown();
        Draft.DRAFT_UPDATER.shutdown();
        VotePoll.POLL_UPDATER.shutdown();

        System.out.println("Disconnecting JDA...");
        jda.getPresence().setStatus(OnlineStatus.INVISIBLE);
        jda.shutdown();

        System.out.println("Closing database connections...");
        try {
            DatabaseHelper.getPool().close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        System.exit(1);
    }
}
