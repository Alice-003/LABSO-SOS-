package client;

import java.util.Scanner;
import java.net.*;

public class Client {
    public static class NodeServer implements Runnable {
        @Override
        public void run() {
            try (ServerSocket serverSocket = new ServerSocket()) {
                serverSocket.accept();

            } catch (Exception e) {
                System.out.println("Errore: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        CommandHandler commandHandler = new CommandHandler();
        Scanner scn = new Scanner(System.in);
        String input;
        int comando;
        Boolean continuaCiclo = true;

        if (args.length != 2) {
            System.out.println("Errore input. Riavviare il programma!");
        } else {

            int porta = Integer.parseInt(args[1]);
            NodeServer nd = new NodeServer();
            Thread th = new Thread(nd);
            th.start();
            try {
                LocalStorage.fileID();
                Socket socket = new Socket();
                InetSocketAddress IpAndPort = new InetSocketAddress("localhost", porta);
                socket.connect(IpAndPort, 3000);
                // se ha successo verifica che il nodo sia in fase di registrazione iniziale
                // oppure abbia già fatto accesso all'aggregatore
                LocalStorage.fileID();
                socket.close();

            } catch (Exception er) {
                System.out.println("Errore: " + er.getMessage());
                scn.close();
                return;
            }
            // all'interno del ciclo i comandi inviati dall'utente vengono tradotti in un
            // ntero per poi eseguire l'azione
            // intero tramite CommandHandler.java

            while (continuaCiclo) {
                System.out.print("> ");
                input = scn.nextLine();
                comando = commandHandler.gestioneComandi(input);
                switch (comando) {
                    case 1:
                        break;
                    case 2:
                        break;
                    case 3:
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
        scn.close();
    }
}
