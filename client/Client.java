package client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

import client.protocol.Protocol;

public class Client {

    public static void main(String[] args) {
        int lunghezzaID = 5;
        String ID;

        if (args.length != 2) {
            System.out.println("Errore input. Riavviare il programma!");
        } else {

            try {
                File fileID = new File("client/Files/NodoID.txt");
                File fileRilevazioni = new File("client/Files/Rilevazioni_" + LocalStorage.ottieniCodice(2) + ".csv");

                // Verifica che i file che contiene l'id del nodo e il file delle rilevazione
                // esistano
                // Se il file nodo viene cancellato, allora viene ricreato riprendendolo dal
                // file delle rilevazioni
                // Se il file delle rilevazioni viene cancellato, allora viene ricreato
                // Se entrambi i file vengono cancellati, vengono ricreati entrambi i file con
                // un ID diverso

                if (!fileID.exists() && !fileRilevazioni.exists()) {
                    LocalStorage.creaFileID(lunghezzaID);
                    LocalStorage.creaFileRilevazioni();
                } else if (fileID.exists() && !fileRilevazioni.exists()) {
                    LocalStorage.creaFileRilevazioni();
                } else if (!fileID.exists() && fileRilevazioni.exists()) {
                    LocalStorage.creaFileID(lunghezzaID, LocalStorage.ottieniCodice(2));
                }

                ID = LocalStorage.ottieniCodice(1);
                LocalStorage.ScriviNuoviDati(ID);
                String host = args[0];
                int port = Integer.parseInt(args[1]);
                Socket s = new Socket(host, port);

                // registrazione all'aggregatore con l'informazione ID del nodo

                PrintWriter to = new PrintWriter(s.getOutputStream(), true);
                to.println(Protocol.RICHIESTA_REGISTRAZIONE_A_SERVER + Protocol.SEPARATORE
                        + LocalStorage.ottieniCodice(1) + Protocol.SEPARATORE + port);
                Scanner from = new Scanner(s.getInputStream());
                String response = from.nextLine();

                // Nel caso in cui la registrazione abbia avuto successo

                if (response.split(Protocol.SEPARATORE)[0].equals(Protocol.TUTTO_OK)) {

                    FileReader fr = new FileReader(fileRilevazioni);
                    BufferedReader br = new BufferedReader(fr);
                    String str = "";

                    while ((str = br.readLine()) != null) {
                        to.println(Protocol.AGGIUNGI_RISORSA + Protocol.SEPARATORE + str.split(",")[0]
                                + Protocol.SEPARATORE + str.split(",")[1] + Protocol.SEPARATORE + str.split(",")[2]
                                + Protocol.SEPARATORE + str.split(",")[3]);

                    }

                    br.close();
                    fr.close();
                }

                Thread sender = new Thread(new Sender(s));
                Thread receiver = new Thread(new Receiver(s, sender));
                sender.start();
                receiver.start();
                try {
                    /* rimane in attesa che sender e receiver terminino la loro esecuzione */
                    sender.join();
                    receiver.join();
                    from.close();
                    s.close();
                    System.out.println("Socket closed");
                } catch (InterruptedException e) {
                    /*
                     * se qualcuno interrompe questo thread nel frattempo, terminiamo
                     */
                    return;
                }
            } catch (Exception er) {
                System.out.println("Errore: " + er.getMessage());
                return;
            }

        }
    }
}
