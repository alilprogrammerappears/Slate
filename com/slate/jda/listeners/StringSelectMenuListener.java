package com.slate.jda.listeners;

import com.slate.jda.EventId;
import com.slate.jda.votepolls.VotePollDraft;
import com.slate.sys.Draft;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.List;
import java.util.Optional;

public class StringSelectMenuListener extends ListenerAdapter {
    @Override
    public void onStringSelectInteraction (StringSelectInteractionEvent event) {
        EventId id = new EventId(event.getComponentId());

        //role menu interactions
        if ((event.getComponentId().equals("dropdown_roles"))){

            List<String> selectedOptions = event.getValues();
            Guild guild = event.getGuild();
            Member member = event.getMember();

            if (guild != null && member != null) {

                for (String selectedOption : selectedOptions){
                    List<Role> roles = guild.getRolesByName(selectedOption, true);

                    if (!roles.isEmpty()) {
                        Role role = roles.get(0);
                        guild.addRoleToMember(member, role).queue();
                    }
                }
                event.reply("You're role has been assigned, good luck with your work!").setEphemeral(true).queue();
            }
            else {
                event.reply("the member or guild does not exist.").setEphemeral(true).queue();
            }
        }
        else if (id.getComponent(0).equals("vp") && id.getComponent(1).equals("remOption")) {
            Optional<VotePollDraft> votePollDraft = Draft.get(VotePollDraft.class, Integer.parseInt(id.getOption(0)));
            votePollDraft.ifPresent(Draft::refresh);
            if (votePollDraft.isEmpty()) {
                event.editMessage("This draft no longer exists.").setEmbeds().setComponents().queue();
                return;
            }

            votePollDraft.get().removeOption(event.getSelectedOptions().get(0).getValue());
            event.editMessage(votePollDraft.get().createDraftMenu()).queue();
        }
    }
}
