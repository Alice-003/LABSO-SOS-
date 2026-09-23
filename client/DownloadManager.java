package client;

import client.protocol.Protocol;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class DownloadManager implements Runnable {

    public int porta;
    ServerSocket server;
    Object lock;

    public DownloadManager(Object lock) {
        try {
            this.lock = lock;
            this.server = new ServerSocket(0);
            this.porta = server.getLocalPort();
            System.out.println("DEBUG: DownloadManager (Server) inizializzato sulla porta " + this.porta);
        } catch (Exception er) {
            System.err.println("Errore nella creazione del server: " + er.getMessage());
        }
    }

    public int getLocalPort() {
        return this.porta;
    }

    @Override
    public void run() {
        if (server == null)
            return;

        while (true) {
            try {
                Socket s = server.accept();
                System.out.println("Un peer si è connesso per richiedere una risorsa.");

                BufferedReader fromClient = new BufferedReader(new InputStreamReader(s.getInputStream()));
                PrintWriter toClient = new PrintWriter(s.getOutputStream(), true);

                // 1. Leggo il nome della rilevazione che il peer vuole da me
                String nomeRisorsaRichiesta = fromClient.readLine();

                // 2. Scansiono tutti i file della cartella client/Files/
                File cartellaFiles = new File("client/Files/");
                File[] listaFile = cartellaFiles.listFiles();

                synchronized(lock) {
                    if (listaFile != null) {
                        for (File fileRilevazioni : listaFile) {
                            // Verifichiamo che sia un file e che finisca per .csv
                            if (fileRilevazioni.isFile() && fileRilevazioni.getName().endsWith(".csv")) {

                                try (BufferedReader fileReader = new BufferedReader(new FileReader(fileRilevazioni))) {
                                    String riga;
                                    while ((riga = fileReader.readLine()) != null) {
                                        // Se la riga corrisponde alla risorsa richiesta, la spedisco via socket
                                        if (nomeRisorsaRichiesta == null
                                                || riga.split(",")[0].equals(nomeRisorsaRichiesta)) {
                                            System.out.println(
                                                    "Invio al peer dal file " + fileRilevazioni.getName() + ": " + riga);
                                            toClient.println(riga);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Segnale di fine trasmissione
                toClient.println(Protocol.FINE_TRASMISSIONE_NODO_NODO);
                s.close();
                System.out.println("Invio completato al peer.");

            } catch (Exception e) {
                System.err.println("Errore nel DownloadManager: " + e.getMessage());
            }
        }
    }
}