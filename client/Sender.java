package client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Sender implements Runnable {

    Socket s;

    public Sender(Socket s) {
        this.s = s;
    }

    @Override
    public void run() {

        Boolean continuaCiclo = true;
        Scanner scn = new Scanner(System.in);
        String input;
        int comando;
        CommandHandler commandHandler = new CommandHandler();
        try {
            PrintWriter to = new PrintWriter(this.s.getOutputStream(), true);

            while (continuaCiclo) {
                synchronized (Client.consoleLock) {
                    synchronized (Client.consoleLock) {
                        System.out.print("> ");
                        input = scn.nextLine();
                        comando = commandHandler.gestioneComandi(input);
                        switch (comando) {
                            case 1:
                                System.out.println("Risorse:");
                                LocalStorage.mostraRilevazioniLocale();
                                break;
                            case 2:
                                break;
                            case 3:
                                to.close();
                                continuaCiclo = false;
                                break;
                            case 4:
                                break;
                            case 5:
                                break;
                            case -1:
                                System.out.println("Comando non valido!");
                                break;
                        }
                    }
                }

            }
            System.out.println("Sender closed.");
        } catch (IOException e) {
            System.err.println("IOException caught: " + e);
            e.printStackTrace();
        } finally {
            scn.close();
        }
    }

}
