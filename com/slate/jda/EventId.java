package com.slate.jda;

public class EventId {
    private final String[] idComponents;
    private final String[] options;

    public EventId (String id) {
        this.idComponents = id.split(":")[0].split("_");
        this.options = (id.contains(":")) ? id.substring(id.indexOf(":") + 1).split(":") : new String[]{};
    }

    public String getComponent (int index) {
        return idComponents[index];
    }
    public String getOption (int index) {
        return options[index];
    }
}
