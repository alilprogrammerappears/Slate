package com.slate.jda;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.LinkedHashMap;


//ProjectRoleMenu lets users create a project role menu to assign users different project roles.
public class ProjectRoleMenu {

    //variables
    private String name;
    private String description;
    private LinkedHashMap<String, Role> roleOptions;


    //constructor
    public ProjectRoleMenu(){
        name = "Project Role Menu";
        description = "Choose your roles for this project!";
        roleOptions = new LinkedHashMap<String, Role>();
    }

    //setter methods
    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description){
        this.description = description;
    }


    //create the message that allows users to vote
    public MessageCreateData showRoleVotingMenu(ProjectRoleMenu roleMenu){

        MessageCreateBuilder message = new MessageCreateBuilder();
        message.setContent(roleMenu.getName() + "\n" + roleMenu.getDescription() +
                            "\n\nReady to choose your project role? Choose at least one of the options below!");

        StringSelectMenu.Builder dropdown = StringSelectMenu.create("dropdown_roles");
        if (!roleOptions.isEmpty()){
        roleOptions.keySet().forEach(key -> {
            dropdown.addOption(key, key);
        });
        }
        else{
            message.setContent("whoops, there are no roles. Please add at least one role to the menu");
        }

        dropdown.setMinValues(0).setMaxValues(roleOptions.size());

        message.setComponents(ActionRow.of(dropdown.build()));

        return message.build();
    }


    //add a role option
    public boolean addOption(String name, Role role){

        if (roleOptions.containsKey(name) || roleOptions.containsValue(role)){
            return false;
        }
        else {
            roleOptions.put(name, role);
            return true;
        }
    }

    //remove a role option
    public boolean removeOption(String name, Role role){

        if (!roleOptions.containsKey(name) || !roleOptions.get(name).equals(role)){
            return false;
        }
        else {
            roleOptions.remove(name, role);
            return true;
        }
    }

    //remove all options
    public boolean deleteMenu(){

        if (roleOptions.isEmpty()) {
            return false;
        }
        else {
            roleOptions.clear();
            return true;
        }
    }

    //getter methods
    public String getName() {
            return name;
    }

    public String getDescription(){
        return description;
    }
}