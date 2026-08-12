import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class UploadManager implements Runnable{

    int porta;
    String id_nodo;

    public UploadManager(int porta) {
        this.porta = porta;
    }

    @Override
    public synchronized void run() {

        try {

            //Creo la connessione con il nodo che deve ricevere i dati
            Socket s = new Socket("localhost", porta);

            //Vado a leggere il file con i dati da inviare
            File dati_da_inviare = new File("client/Files/" + LocalStorage.trovaFile("Rilevazioni"));
            
            //Controllo che il file esista
            if(dati_da_inviare.exists() != true) {
                System.out.println("Non trovo il file dove leggere i dati da inviare");
                return;
            }

            //Mi creo i miei strumenti per inviare i dati al nodo, leggere il file e ricevere dal nodo l'ok di fine lavoro
            PrintWriter invia_dati = new PrintWriter(s.getOutputStream(), true);
            BufferedReader leggi_file = new BufferedReader(new FileReader(dati_da_inviare));
            BufferedReader ricevi_conferma_da_server = new BufferedReader(new InputStreamReader(s.getInputStream()));

            System.out.println("Connessione nodo-nodo stabilita: mi preparo da inviare i dati..");

            String riga_da_inviare;

            //Leggo il file: per ogni riga scritta nel file, invio una stringa al nodo ricevente
            //Se il thread si interrompe, mando la stringa END per chiudere la connessione
            while((riga_da_inviare = leggi_file.readLine()) != null) {

                if (Thread.interrupted()) {
                    invia_dati.println(Protocol.FINE_TRASMISSIONE_NODO_NODO);
                    break;
                }

                invia_dati.println(riga_da_inviare);
                System.out.println("Inviata riga: " + riga_da_inviare);
            }

            //Finito di inviare i dati, chiudo il lettore ed invio la stringa END
            leggi_file.close();
            System.out.println("Fine dei dati da inviare..");
            invia_dati.println(Protocol.FINE_TRASMISSIONE_NODO_NODO);

            //Attendo la ricezione della stringa END dal nodo ricevente
            try {
                String conferma = ricevi_conferma_da_server.readLine();
                if (Protocol.FINE_TRASMISSIONE_NODO_NODO.equals(conferma)) {
                    System.out.println("Il server ha confermato la fine della ricezione dei dati.");
                }
            } catch (Exception e) {
                System.err.println("Errore nella ricezione della conferma dal server: " + e.getMessage());
            }

            System.out.println("Chisura connessione effettuata.");

            //chiudo la connessione al nodo ricevente
            if (s != null && !s.isClosed()) {
                s.close(); 
            }

        } catch (Exception e) {
            System.err.println("Errore nella procedura: " + e.getMessage());
        }
        
    }

}
