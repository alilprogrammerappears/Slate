package com.slate.jda;

import com.slate.sys.Colors;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.List;

public class UserPreferences {
    private final String userId;
    private final String phoneNumber;
    private final String email;
    private final boolean pingNotifications;
    private final boolean textNotifications;
    private final boolean emailNotifications;

    public UserPreferences (String userId, String phoneNumber, String email,
        boolean pingNotifications, boolean textNotifications, boolean emailNotifications) {
        this.userId = userId;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.pingNotifications = pingNotifications;
        this.textNotifications = textNotifications;
        this.emailNotifications = emailNotifications;
    }

    public MessageCreateData createMessage () {
        MessageCreateBuilder message = new MessageCreateBuilder();
        EmbedBuilder embed = new EmbedBuilder().setColor(Colors.APPROVED)
            .setTitle("Edit your preferences:")
            .addField("Contact Info:",
                "Phone Number: " + ((!phoneNumber.isBlank()) ? phoneNumber : "Not set!") + "\n" +
                "Email Address: " + ((!email.isBlank()) ? email : "Not set!"), false)
            .addField("Notifications:",
                "Ping Notifications: " + ((pingNotifications) ? "Enabled" : "Disabled") + "\n" +
                "Text Notifications: " + ((textNotifications) ? "Enabled" : "Disabled") + "\n" +
                "Email Notifications: " + ((emailNotifications) ? "Enabled" : "Disabled"), false);

        List<ActionRow> components = List.of(
            ActionRow.of(
                Button.primary("up_phoneNum", "Change Phone Number"),
                Button.primary("up_email", "Change Email Address")),
            ActionRow.of(
                Button.of((!pingNotifications) ? ButtonStyle.SUCCESS : ButtonStyle.SECONDARY,
                    "up_toggle:pingNotifications", (!pingNotifications) ? "Enable Pings" : "Disable Pings"),
                Button.of((!textNotifications) ? ButtonStyle.SUCCESS : ButtonStyle.SECONDARY,
                    "up_toggle:textNotifications", (!textNotifications) ? "Enable Texts" : "Disable Texts"),
                Button.of((!emailNotifications) ? ButtonStyle.SUCCESS : ButtonStyle.SECONDARY,
                    "up_toggle:emailNotifications", (!emailNotifications) ? "Enable Emails" : "Disable Emails"))
        );

        return message.setEmbeds(embed.build()).setComponents(components).build();
    }

    public String getPhoneNumber () {
        return phoneNumber;
    }
    public String getEmail () {
        return email;
    }
    public boolean receivePings () {
        return pingNotifications;
    }
    public boolean receiveTexts () {
        return textNotifications;
    }
    public boolean receiveEmails () {
        return emailNotifications;
    }
}