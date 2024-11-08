package com.slate.sys;

import com.slate.Main;

import java.util.Scanner;

public class CLIListener implements Runnable {
    public static final Scanner SCANNER = new Scanner(System.in);

    // Methods
    @Override
    public void run() {
        System.out.print("> ");
        String[] cmd = SCANNER.nextLine().trim().split(" ");

        switch (cmd[0].trim()) {
            case "" -> {}
            case "quit" -> Main.shutdown();
            default -> System.out.println("Unrecognized command");
        }
    }

    public static boolean promptBoolean (String prompt, String trueCase, String falseCase) {
        do {
            System.out.print(prompt + "\nEnter \"" + trueCase + "\" or \"" + falseCase + "\" - ");
            String input = SCANNER.nextLine();
            if (input.equalsIgnoreCase(trueCase)) {
                return true;
            }
            else if (input.equalsIgnoreCase(falseCase)) {
                return false;
            }
        } while (true);
    }
}
