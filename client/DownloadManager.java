import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


public class DownloadManager {

    public DownloadManager() {}

    //Tipo di rilevazione = risorsa


    //TODO: AD OGNI METODO MESSAGE, SOSTITUISCI LA SCRITTA NODO COL MITTENTE CORRETTO


    //Metodo per dare al client la scelta dell'attività da eseguire
    public String scelta_azione() {

        //Creo uno scanner per ricevere la preferenza dell'utente
        Scanner userInput = new Scanner(System.in);
        String scelta = null;

        try {
            System.out.println("Cosa vuoi fare? Digita il numero corrispondente");
            System.out.println("1 - Richiedi ad aggregatore la lista dei nodi attivi");
            System.out.println("2 - Richiedi ad aggregatore quale nodo possiede una determinata rilevazione");
            System.out.println("3 - Richiedi l'accesso ad un nodo e scarica la rilevazione in locale");
            System.out.println("4 - Cerca e scarica una rilevazione dalla rete");

            scelta = userInput.nextLine();

        } catch (Exception e){
            System.out.println("Errore nella richiesta: " + e.getMessage());
            }

        userInput.close();
        return scelta;
    }

    //Metodo per dire che tipo di risorsa si voglia cercare di ottenere
    public String cerca_rilevazione_da_rete() {

        String richiesta = null;

        //Creo uno scanner per ricevere la preferenza dell'utente
        Scanner userInput = new Scanner(System.in);

        try {
            System.out.println("Che tipo di risorsa stai cercando? Digita il numero corrispondente");
            System.out.println("1 - Temperatura");
            System.out.println("2 - Pressione atomosferica");
            System.out.println("3 - Concentrazione di CO2");

            String scelta = userInput.nextLine();

            //Genero la richiesta in base al numero selezionato.
            switch (scelta) {
                case "1": richiesta = Protocol.RICHIESTA_TEMPERATURA; break;
                case "2": richiesta = Protocol.RICHIESTA_PRESSIONE; break;
                case "3": richiesta = Protocol.RICHIESTA_CONCENTRAZIONE; break;
                default: System.out.println("Scelta non valida"); richiesta = Protocol.ERRORE; break;
            }

        } catch (Exception e){
            System.out.println("Errore nella richiesta: " + e.getMessage());
        }
        
        userInput.close();
        return richiesta;
    }

    //Metodo per ottenere la lista degli id dei nodi attivi
    public synchronized String ottieni_lista_nodi(String id_nodo) {

        String sms = null;

        try {
            System.out.println("Download manager: Richiedo al server la lista dei nodi attivi..");
            sms = Protocol.RICHIESTA_LISTA_NODI;
        } catch (Exception e){
            System.out.println("Errore nella richiesta: " + e.getMessage());
            }

        return sms;
    }

    //Metodo per chiedere al server quale nodo abbia la risorsa che vogliamo
    public synchronized String richiedi_id_nodo_con_risorsa_voluta() {
        String sms = null;

        try {
            String risorsa = cerca_rilevazione_da_rete();

            if (!risorsa.equals(Protocol.ERRORE)) {
                System.out.println("Download manager: Richiedo al server quale nodo abbia la risorsa: " + risorsa);
                sms = Protocol.RICHIESTA_NODO_CON_RISORSA + " " + risorsa;
            } else {
                System.out.println("Download manager: Errore nella richiesta.");
                }
        } catch (Exception e) {
            System.err.println("Errore nel download delle risorse: " + e.getMessage());
        }
        return sms;
    }

    //Metodo per richiedere il tipo di risorsa da un nodo specifico
    public synchronized String richiesta_accesso_nodo(){

        Scanner userInput = new Scanner(System.in);
        String sms = null;

        try {
            System.out.print("\nInserisci l'ID del nodo a cui vuoi accedere: ");

            String id_nodo = userInput.nextLine();
            
            String risorsa = cerca_rilevazione_da_rete();

            String data = id_nodo + " " + risorsa;
            
            if (!risorsa.equals(Protocol.ERRORE)) {
                System.out.println("Download manager: Invio di richiesta di accesso a nodo: " + id_nodo);
                sms = Protocol.RICHIESTA_DOWNLOAD_DA_NODO + " " + data;
            } else {
                System.out.println("Download manager: Errore nella richiesta.");
                }
            
        } catch (Exception e) {
            System.out.println("Errore nella richiesta di accesso: " + e.getMessage());
        }

        userInput.close();
        return sms;
    }

    //Metodo per richiedere al server il download di una risorsa
    public synchronized String richiesta_download_da_server(String risorsa) {
        String sms = null;

        try {
            System.out.println("Download Manager: Richiedo al server la risorsa: " + risorsa);
            sms = Protocol.RICHIESTA_DOWNLOAD_DA_RETE + " " + risorsa;
        } catch (Exception e) {
            System.out.println("Errore nella richiesta di accesso: " + e.getMessage());
        }
        return sms;
    }

    //Metodo che deve usare local storage per immagazzinare i dati ricevuti da nodo aggregatore (il server)
    public synchronized void salva_dati(HashMap<String, String> risorse, String id_nodo) {
        try {
            
            String file_download_risorse_txt = "Download_risorse_nodo_" + id_nodo + ".txt";
            File file_download_risorse = new File(file_download_risorse_txt);

            //creo il file per scrivere le risorse richieste, se esiste già non faccio nulla
            try {
                if(file_download_risorse.createNewFile()) {
                    System.out.println("Creato File: " + file_download_risorse_txt);
                } else {
                    System.out.println("Il file download risorse esiste gia'.");
                    } 
            } catch (Exception e) {
                System.err.println("Errore nella creazione del file: " + e.getMessage());
            }

            //Scrivo sul file .txt le risorse che ricevo dal server
            BufferedWriter writer_temp = new BufferedWriter(new FileWriter(file_download_risorse_txt, true));

            for (Map.Entry<String, String> elemento : risorse.entrySet()) {
            //Per ogni entry delle risorse mi vado a prendere la chiave e il valore
            String chiave = elemento.getKey();
            String valore = elemento.getValue();

            //Le combino in una stringa unica
            String temp = chiave + " " + valore;
            System.out.println ("Creata stringa da salvare: " + temp);

            //La scrivo nel file .txt
            writer_temp.write(temp);
            System.out.println("Inserito in file txt la risorsa: " + temp);
            writer_temp.newLine();
            }
            
            writer_temp.close();

        } catch (Exception e) {
            System.err.println("Errore nel download delle risorse: " + e.getMessage());
        }
    }

    //Metodo per stampare la lista dei nodi ricevuti dal server che possiedono la rilevazione che si desidera
    public void stampa_lista_nodi_da_server(HashMap<String, String> risorse) {

        try {
            for (Map.Entry<String, String> elemento : risorse.entrySet()) {
            String chiave = elemento.getKey();
            String valore = elemento.getValue();
            System.out.println("Nodo: " + chiave + " Risorsa: " + valore);
            }
        } catch (Exception e) {
            System.err.println("Errore nella stampa dei nodi: " + e.getMessage());
        }
    }

    //Metodo che stampa la lista degli id dei nodi attivi
    public void stampa_lista_nodi_attivi(List<String> lista_nodi) {
        try {
            for(int i=0; i<lista_nodi.size(); i++) {
                System.out.println("Nodo Attivo: " + lista_nodi.get(i));
            }
        } catch (Exception e) {
            System.err.println("Errore nella stampa dei nodi attivi: " + e.getMessage());
        }
    }


}