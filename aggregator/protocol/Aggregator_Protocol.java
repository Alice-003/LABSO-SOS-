package aggregator.protocol;

public class Aggregator_Protocol {
    public static final String REGISTER = "REGISTER";
    public static final String ADD = "ADD";
    public static final String LISTDATA_REMOTE = "LISTDATA_REMOTE";
    public static final String DOWNLOAD_REQUEST = "DOWNLOAD REQUEST";
    public static final String DOWNLOAD_FAILED = "DOWNLOAD_FAILED";
    public static final String DOWNLOAD_OK = "DOWNLOAD_OK";
    public static final String QUIT = "QUIT";

    public static final String OK = "OK";
    public static final String ERROR = "ERROR";
    public static final String DATA = "DATA";
    public static final String END = "END";

    // costanti utilità
    public static final String SEP = " ";

    // numero max di split per i messaggi
    public static final int MAX_SPLIT = 3;

    private Aggregator_Protocol() {
        // costruttore privato per evitare istanziazione
    }


}
