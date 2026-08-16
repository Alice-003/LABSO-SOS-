package client;

import client.protocol.Protocol;
import aggregator.protocol.Aggregator_Protocol;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class UploadManager implements Runnable {

    String ip;
    int porta;
    String nomeRilevazione;
    String nomePeer;
    PrintWriter toAggregator;

    public UploadManager(String ip, int porta, String nomeRilevazione, String nomePeer, PrintWriter toAggregator) {
        this.ip = ip;
        this.porta = porta;
        this.nomeRilevazione = nomeRilevazione;
        this.nomePeer = nomePeer;
        this.toAggregator = toAggregator;
    }

    @Override
    public synchronized void run() {
        Socket s = null;
        try {
            System.out.println("Connessione al peer " + ip + ":" + porta + " per: " + nomeRilevazione);
            s = new Socket(ip, porta);

            PrintWriter toPeer = new PrintWriter(s.getOutputStream(), true);
            BufferedReader fromPeer = new BufferedReader(new InputStreamReader(s.getInputStream()));

            toPeer.println(nomeRilevazione);

            File fileRilevazioniDownload = new File("client/Files/dati_ricevuti.csv");
            if (!fileRilevazioniDownload.exists()) {
                fileRilevazioniDownload.createNewFile();
            }

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileRilevazioniDownload, true))) {
                String rigaRicevuta;
                while ((rigaRicevuta = fromPeer.readLine()) != null) {
                    if (rigaRicevuta.equals(Protocol.FINE_TRASMISSIONE_NODO_NODO)) {
                        break;
                    }
                    bw.write(rigaRicevuta);
                    bw.newLine();
                }
                bw.flush();
            }

            s.close();
            System.out.println("Download completato.");

            // Notifica di successo all'aggregatore
            toAggregator.println(Aggregator_Protocol.DOWNLOAD_OK + Aggregator_Protocol.SEP + nomeRilevazione
                    + Aggregator_Protocol.SEP + nomePeer);

        } catch (Exception e) {
            System.err.println("Errore durante il download: " + e.getMessage());
            // Notifica di fallimento all'aggregatore
            toAggregator.println(Aggregator_Protocol.DOWNLOAD_FAILED + Aggregator_Protocol.SEP + nomeRilevazione
                    + Aggregator_Protocol.SEP + nomePeer);
        }
    }
}