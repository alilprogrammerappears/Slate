package com.slate.sys;

import java.util.HashMap;
import java.util.Optional;

public class ArgumentManager {
    private String separator;
    private final HashMap<String, String> args = new HashMap<>();

    public ArgumentManager (String separator) {
        this.separator = separator;
    }

    public void setSeparator (String separator) {
        this.separator = separator;
    }
    public void setSource (String[] args) {
        this.args.clear();

        for (String arg : args) {
            String[] argPair = arg.split(separator, 2);
            if (arg.length() == 0) {
                throw new IllegalArgumentException("Argument \"" + arg + "\" is missing a value.\nAssign a value " +
                    "to the argument by using the format: key" + separator + "value");
            }
            this.args.put(argPair[0], argPair[1]);
        }
    }

    @Override
    public String toString () {
        StringBuilder output = new StringBuilder();
        args.forEach((arg, value) -> output.append(arg).append(" - ").append(value).append(System.lineSeparator()));
        return output.toString();
    }

    public Optional<String> get (String key) {
        return Optional.ofNullable(args.get(key));
    }
}
