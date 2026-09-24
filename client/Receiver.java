package client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import aggregator.protocol.Aggregator_Protocol;
import client.protocol.Protocol;

public class Receiver implements Runnable {

    Socket s;
    Thread sender;

    public Receiver(Socket s, Thread sender) {
        this.s = s;
        this.sender = sender;
    }

    @Override
    public void run() {

        try {
            Scanner from = new Scanner(this.s.getInputStream());

            Boolean stampa = false; // usato per non stampare su terminale i comandi del server: DATA ed END

            // Creiamo il PrintWriter qui, usando la stessa socket del Receiver
            // per mantenere aperta la comunicazione con l'aggregatore

            while (from.hasNextLine()) {
                String response = from.nextLine();
                String[] strSPLIT = response.split(Protocol.SEPARATORE);

                switch (response.split(Aggregator_Protocol.SEP)[0]) {
                    case Aggregator_Protocol.DATA: // inizio dei dati da stampare
                        stampa = true;
                        break;
                    case Aggregator_Protocol.END: // termine dei dati da stampare
                        stampa = false;
                        break;
                    case Aggregator_Protocol.ERROR: // stampa nel caso di errore da parte del Server
                        stampa = true;
                    default:
                        break;
                }
                // controllo per non stampare DATA ed END
                if (stampa == true && !response.split(Aggregator_Protocol.SEP)[0].equals(Aggregator_Protocol.DATA)) {
                    System.out.println(response);
                }
                if (strSPLIT[0].equals(Protocol.comandoDOWNLOAD)) {
                    // 1. viene creata la connessione all'aggregatore
                    // 2. Viene creato il thread UploadManager che si occupa di dialogare con il
                    // nodo che detiene la rilevazione.

                    PrintWriter toAggregator = new PrintWriter(s.getOutputStream(), true);
                    String ip = strSPLIT[2];
                    int porta = Integer.parseInt(strSPLIT[3]);
                    String nomeRilevazione = strSPLIT[4];
                    String nomePeer = strSPLIT[1];
                    // Viene effettuato un controllo per non fare un download di una risorsa che si
                    // detiene già nel file locale delle rilevazioni
                    if (!LocalStorage.ottieniCodice(1).equals(nomePeer)) {
                        UploadManager up = new UploadManager(ip, porta, nomeRilevazione, nomePeer, toAggregator);
                        Thread upThread = new Thread(up);
                        upThread.setDaemon(true);
                        upThread.start();
                    } else {
                        System.out.println("La rilevazione si trova su questo nodo");
                    }

                }
            }

            from.close();
        } catch (IOException e) {
            System.err.println("IOException caught: " + e);
            e.printStackTrace();
        } finally {
            this.sender.interrupt();
            System.out.println("Receiver closed.");
        }
    }
}
