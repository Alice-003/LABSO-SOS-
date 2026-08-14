package client;

import client.protocol.Protocol;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import aggregator.protocol.Aggregator_Protocol;

public class UploadManager implements Runnable {

    String ip;
    int porta;
    String nomeRilevazione, idNodo;
    PrintWriter toAggregator;

    public UploadManager(String ip, int porta, String nomeRilevazione, PrintWriter toAggregator, String idNodo) {
        this.ip = ip;
        this.porta = porta;
        this.nomeRilevazione = nomeRilevazione;
        this.toAggregator = toAggregator;
        this.idNodo = idNodo;
    }

    @Override
    public synchronized void run() {
        try {
            System.out.println("Connessione al peer remoto " + ip + ":" + porta + " per scaricare: " + nomeRilevazione);
            Socket s = new Socket(ip, porta);

            // Strumenti per comunicare con il peer remoto
            PrintWriter toPeer = new PrintWriter(s.getOutputStream(), true);
            BufferedReader fromPeer = new BufferedReader(new InputStreamReader(s.getInputStream()));

            // 1. Dico al peer remoto quale rilevazione voglio scaricare
            toPeer.println(nomeRilevazione);

            // 2. Prendo il mio file locale dove salvare la rilevazione scaricata
            File fileRilevazioniDownload = new File("client/Files/dati_ricevuti.csv");
            System.out.println(fileRilevazioniDownload.exists());
            if (!fileRilevazioniDownload.exists()) {
                fileRilevazioniDownload.createNewFile();
            }
            FileWriter fw = new FileWriter(fileRilevazioniDownload, true);
            BufferedWriter bw = new BufferedWriter(fw);

            String rigaRicevuta;
            // 3. Leggo dal socket i dati che il peer mi sta mandando
            while ((rigaRicevuta = fromPeer.readLine()) != null) {
                if (rigaRicevuta.equals(Protocol.FINE_TRASMISSIONE_NODO_NODO)) {
                    break;
                }
                System.out.println("Scaricato: " + rigaRicevuta);
                bw.write(rigaRicevuta);
                bw.newLine();
                bw.flush();
            }

            bw.close();
            fw.close();
            s.close();
            toAggregator
                    .println(Aggregator_Protocol.DOWNLOAD_OK + Protocol.SEPARATORE + Sender.nomeRilevazioneDownload
                            + Protocol.SEPARATORE + idNodo);
            System.out.println("Download completato con successo.");

        } catch (Exception e) {
            System.err.println("Errore durante il download dal peer: " + e.getMessage());
        }
    }
}