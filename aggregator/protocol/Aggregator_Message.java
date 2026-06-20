package aggregator.protocol;

/**
Trasforma una stringa grezza ricevuta dalla socket 
in un oggetto con un comando e una lista di argomenti
*/
 
public class Aggregator_Message {

    private final String command;
    private final String[] args;

    // costruttore privato
    private Aggregator_Message(String command, String[] args) {
        // assegno alla variabile interna command il valore ricevuto come parametro
        this.command = command;
        this.args    = args;
    }

    // factory method, prende la stringa grezza e restituisce l'oggetto Aggregator_Message corrispondente
    public static Aggregator_Message parse(String x) {
        // controllo se la stringa è nulla o vuota
        if (x == null || x.isBlank()) {
            throw new IllegalArgumentException("Messaggio non valido");
        }

        // divido la stringa in parti usando lo spazio come separatore e limitando il numero di split a MAX_SPLIT
        String[] parts = x.trim().split(Aggregator_Protocol.SEP, Aggregator_Protocol.MAX_SPLIT);
        
        //la prima parte è sempre il comando, che converto in maiuscolo per uniformare 
        String command = parts[0].toUpperCase();

        String[] args;
        
        // controllo se ci sono argomenti
        if (parts.length > 1) {
            // creo un array con dimensione pari al numero di argomenti
            // tolgo 1 perchè la prima parte è il comando
            args = new String[parts.length - 1];
            // copio gli argomenti dell'array parts nell'array args partendo dall'indice 1 di parts
            System.arraycopy(parts, 1, args, 0, parts.length - 1);
        } else {
            // se non ci sono argomenti, creo un array vuoto
            args = new String[0];
        }
        // creo e restituisco un nuovo oggetto Aggregator_Message
        return new Aggregator_Message(command, args);
    }

    // getters, restituisce il comando e gli argomenti dell'oggetto Aggregator_Message
    public String getCommand() {
        return command;
    }

    // restituisce l'argomento all'indice specificato
    public String getArg(int index) {
        // controllo se l'indice richiesto esista davvero nell'array
        if (index < 0 || index >= args.length) {
            throw new IndexOutOfBoundsException("Argomento " + index + " non esiste");
        }
        // restituisco l'argomento richiesto
        return args[index];
    }

    // restituisce quanti argomenti ci sono in totale (escluso il comando)
    public int getArgCount() {
        return args.length;
    }

    // controlla se ci sono almeno n argomenti
    public boolean hasAtLeast(int n) {
        return args.length >= n;
    }

    // ricostuisce la stringa originale del messaggio a partire dal comando e dagli argomenti
    @Override
    public String toString() {
        if (args.length == 0) return command;
        return command + " " + String.join(" ", args);
    }
}
