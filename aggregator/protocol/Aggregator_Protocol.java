package aggregator.protocol;

/** Dizionario condiviso del protocollo Aggregator, 
 * definisce i comandi e le costanti utilizzate per la 
 * comunicazione tra Aggregator e i suoi client
 * In questo modo se si vuole cambiare un comando, 
 * basta cambiare il valore della costante qui e non in
 * tutti i file che lo usano
 * 
 * Formato dei messaggi: 
 * COMANDO [arg1] [arg2]...

*/

public class Aggregator_Protocol {
   // comando con cui il nodo si registra all'aggregatore
    public static final String REGISTER = "REGISTER";
    
    //comando con cui il nodo nottifica una nuova rilevazione
    public static final String ADD = "ADD";

    // comando con cui il nodo richiede la lista di tutte le rilevazioni in rete
    public static final String LISTDATA_REMOTE = "LISTDATA_REMOTE";

    //comando con cui il nodo chiede da quale nodo scaricare una rilevazione
    public static final String DOWNLOAD_REQUEST = "DOWNLOAD_REQUEST";
    
    // comando con cui il nodo segnala che il download da un peer è fallito
    public static final String DOWNLOAD_FAILED = "DOWNLOAD_FAILED";
    
    // comando con cui il nodo segnala che il download è andato a buon fine
    public static final String DOWNLOAD_OK = "DOWNLOAD_OK";
    
    
    // comando con cui il nodo chiede di uscire dall'aggregatore
    public static final String QUIT = "QUIT";

    //risposta dell'aggregatore quando l'operazione è andata a buon fine
    public static final String OK = "OK";
    
    // risposta dell'aggregatore quando si verifica un errore
    public static final String ERROR = "ERROR";
   
    // segna l'inizio di una risposta su più righe
    public static final String DATA = "DATA";
    
    // segna la fine di una risposta su più righe
    public static final String END = "END";

    // costanti utilità
    // separatore tra i campi di un messaggio
    public static final String SEP = " ";

    // numero max di split per i messaggi
    // 3 perchè un messaggio ha al massimo COMANDO | nomeRisorsa | contenuto con spazi
    public static final int MAX_SPLIT = 3;

    
    private Aggregator_Protocol() {
        // costruttore privato per evitare istanziazione
        // contiene solo costanti statiche
    }


}
