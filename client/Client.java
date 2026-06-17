package client;

import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        CommandHandler commandHandler = new CommandHandler();
        Scanner scn = new Scanner(System.in);
        String input;

        if (args.length != 2) {
            System.out.println("Errore input. Riavviare il programma!");
        } else {
            while (true) {
                System.out.print("> ");
                input = scn.nextLine();
                if (commandHandler.gestioneComandi(input) == 3) {
                    break;
                }
            }
            scn.close();
        }
    }
}
