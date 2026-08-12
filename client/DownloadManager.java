import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class DownloadManager implements Runnable {

    public int porta;     

    public DownloadManager(int porta) {
        this.porta = porta;
    }

    @Override
    public synchronized void run() {

       try {

        //creo una connessione stile server per far connettere il nodo che deve inviare i dati
        ServerSocket server = new ServerSocket(porta);
        Socket s = server.accept();

        String file_download_risorse_txt = "dati_ricevuti.txt";
        File file_download_risorse = new File(file_download_risorse_txt);

        //creo il file per scrivere le risorse ricevute, se esiste già non faccio nulla
        try {
            if(file_download_risorse.createNewFile()) {
                System.out.println("Creato File: " + file_download_risorse_txt);
            } else {
                System.out.println("Il file download risorse esiste gia'.");
                } 
        } catch (Exception e) {
            System.err.println("Errore nella creazione del file: " + e.getMessage());
            }

        System.out.println("Connessione stabilita.");


        //mi creo gli strumenti per leggere i dati che ricevo dal nodo che invia
        //per inviare la conferma di fine lavoro al nodo e per scrivere sul file .txt i dati 
        BufferedReader leggi_dati_ricevuti = new BufferedReader(new InputStreamReader(s.getInputStream()));
        PrintWriter rispondi_al_client = new PrintWriter(s.getOutputStream(), true);
        BufferedWriter scrivi_dati = new BufferedWriter(new FileWriter(file_download_risorse_txt, true));
        
        String dati;
        boolean continua = true;

        //per ogni elemento di tipo string che ricevo dal nodo 
        //(che corrisponde ad una riga sul file che mi sta inviando)
        //scrivo una riga sul mio file .txt
        //se ricevo la stringa END so che il nodo che mi invia i dati ha finito di inviare, 
        //invio così a mia volta END per dire che ho finito di ricevere

        while((dati = leggi_dati_ricevuti.readLine()) != null && continua == true) {
            if(dati.equals(Protocol.FINE_TRASMISSIONE_NODO_NODO)) {
                continua = false;
                rispondi_al_client.println(Protocol.FINE_TRASMISSIONE_NODO_NODO);
                System.out.println("Invio comando chiusura al nodo client");
            } else {
                scrivi_dati.write(dati);
                scrivi_dati.newLine();
            }
        }

        //chiudo lo "scrittore" su file
        scrivi_dati.close();

        //chiudo tutte le connessioni rimaste attive
        if (s != null && !s.isClosed()) {
            s.close(); // Chiudiamo la sessione con questo specifico client
        }

        if (server != null && !server.isClosed()) {
            server.close(); // Liberiamo definitivamente la porta del PC
        }

        System.out.println("Connessione con nodo mittende terminata.");

    } catch (Exception e) {
        System.err.println("Errore nel download delle risorse: " + e.getMessage());
        } 
                    
    }
    
}
