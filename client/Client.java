package client;

import java.util.Scanner;
import java.net.*;
import java.io.File;

public class Client {
    public static class NodeServer implements Runnable {
        @Override
        public void run() {
            try (ServerSocket serverSocket = new ServerSocket()) {
                serverSocket.accept();

            } catch (Exception e) {

            }
        }
    }

    public static void main(String[] args) {
        CommandHandler commandHandler = new CommandHandler();
        Scanner scn = new Scanner(System.in);
        String input;
        int comando;
        Boolean continuaCiclo = true;
        int lunghezzaID = 5;

        if (args.length != 2) {
            System.out.println("Errore input. Riavviare il programma!");
        } else {

            int porta = Integer.parseInt(args[1]);
            NodeServer nd = new NodeServer();
            Thread th = new Thread(nd);
            th.start();

            try {

                Socket socket = new Socket();
                InetSocketAddress IpAndPort = new InetSocketAddress("localhost", porta);
                socket.connect(IpAndPort, 3000);
                File fileID = new File("client/Files/NodoID.txt");

                if (!fileID.exists()) {
                    LocalStorage.creaFileID(lunghezzaID);
                }

                File fileRilevazioni = new File("client/Files/Rilevazioni_" + LocalStorage.ottineCodice() + ".csv");
                if (!fileRilevazioni.exists()) {
                    LocalStorage.creaFileRilevazioni();
                }
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
