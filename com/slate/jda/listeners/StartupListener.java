package com.slate.jda.listeners;

import com.slate.Main;
import com.slate.jda.SlashCommand;
import com.slate.jda.votepolls.VotePoll;
import com.slate.sys.CLIListener;
import com.slate.sys.Draft;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class StartupListener extends ListenerAdapter {
    public static final ScheduledExecutorService CLI_THREAD = new ScheduledThreadPoolExecutor(1);

    @Override
    public void onReady (ReadyEvent event) {
        JDA jda = event.getJDA();

        System.out.println("========================= STARTING =========================");
        System.out.println("Connected to " + jda.getSelfUser().getName() + "#" + jda.getSelfUser().getDiscriminator());

        // Update slash commands in all guilds
        Optional<String> updateCommands = Main.ARGS.get("updateCommands");
        switch (updateCommands.orElseGet(() ->
            (CLIListener.promptBoolean("Do you want to force update all guild commands?", "Y", "N")) ? "yes" : "no")) {
            case "yes" -> {
                System.out.println("Updating commands in all guilds...");
                Executors.newSingleThreadExecutor().execute(() -> {
                    for (Guild guild : jda.getGuilds()) {
                        guild.updateCommands().addCommands(SlashCommand.getAllData()).complete();
                        try {
                            Thread.sleep(500);
                        }
                        catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    System.out.println("Finished updating slash commands in all guilds!");
                });
            }
            case "no" -> System.out.println("Skipping guild command update!");
            default -> System.out.println("The value you entered isn't recognized. Skipping command update.\n" +
                "Make sure the \"updateCommands\" argument is \"yes\" or \"no\"");
        }

        // Register event listeners
        jda.addEventListener(new SlashCommandListener());
        jda.addEventListener(new ButtonListener());
        jda.addEventListener(new ModalListener());
        jda.addEventListener(new StringSelectMenuListener());
        jda.addEventListener(new JoinGuildListener());
        jda.addEventListener(new EntitySelectMenuListener());

        // Start threads
        Draft.DRAFT_UPDATER.scheduleWithFixedDelay(Draft.REMOVE_EXPIRED_DRAFTS, 1, 1, TimeUnit.HOURS);
        VotePoll.POLL_UPDATER.scheduleAtFixedRate(VotePoll.getEndPollsRunnable(event.getJDA()), 0, 2, TimeUnit.SECONDS);

        jda.getPresence().setStatus(OnlineStatus.ONLINE);
        System.out.println("========================= FINISHED =========================");
        CLI_THREAD.scheduleAtFixedRate(new CLIListener(), 0, 1, TimeUnit.MILLISECONDS);
    }
}
