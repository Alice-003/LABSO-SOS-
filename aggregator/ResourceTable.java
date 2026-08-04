package aggregator;

import java.util.*;
import java.util.concurrent.locks.*;

public class ResourceTable {

    // record che rappresenta l'indirizzo di rete di un nodo: ip e porta
    public record NodeAddress(String ip, int port) {
    }

    // OPERAZIONI DI SCRITTURA
    /**
     * rilevazione: insieme di nodi che la possiedono
     * uso Set perché garantisce che ogni rilevazione ha un nome univoco
     * per nodo, quindi non si hanno duplicati
     */
    private final Map<String, Set<String>> table = new HashMap<>();

    /** nodi connessi all'agregator */
    private final Set<String> activeNodes = new HashSet<>();
    private final Map<String, NodeAddress> addresses = new HashMap<>();
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock = rwLock.readLock();
    private final Lock writeLock = rwLock.writeLock();

    /**
     * Metodo che registra un nodo appena si connette e la sua relativa lista
     * delle rilevazini.
     * viene chiamato all'avvio del sensore dal ClientHandler
     * solo un thread alla volta può scrivere quindi gli altri devono espettare
     * il rilascio del lock.
     */
    public void registerNodes(String nodeId, List<String> resources, String ip, int port) {
        writeLock.lock(); // acquisisce il lock
        try {
            activeNodes.add(nodeId);
            addresses.put(nodeId, new NodeAddress(ip, port));
            for (int i = 0; i < resources.size(); i++) {
                String resource = resources.get(i);
                // se la rilevazione non è ancora nella tabella creo un nuovo Set e poi aggiungo
                // il nodo
                table.computeIfAbsent(resource, k -> new HashSet<>()).add(nodeId);
            }
        } finally {
            writeLock.unlock(); // rilascio del lock
        }
    }

    /**
     * Metodo che aggiunge una rilevazione a un nodo già connesso.
     * viene chiamato quando un sensore esegue il comando "add"
     */
    public void addResource(String nodeId, String resourceName) {
        writeLock.lock();
        try {
            table.computeIfAbsent(resourceName, k -> new HashSet<>()).add(nodeId);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Metodo che segnala che un sensore si è disconnesso
     * le relative rilevazioni non saranno più accessibili
     * viene chiamato quando viene eseguito il comando "quit"
     */
    public void disconnectNodes(String nodeId) {
        writeLock.lock();
        try {
            activeNodes.remove(nodeId);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Metodo che rimuove la coppia nodo-rilevazione dalla tabella
     * viene chiamato dall'aggregator quando un tentativo di download fallisce,
     * quindi il nodo non verrà più proposto per quella rilevazione
     */
    public void removeEntity(String nodeId, String resourceName) {
        writeLock.lock();
        try {
            Set<String> nodes = table.get(resourceName);
            if (nodes != null) {
                nodes.remove(nodeId);
                if (nodes.isEmpty()) {
                    table.remove(resourceName);
                }
            }
        } finally {
            writeLock.unlock();
        }
    }

    // OPERAZIONI DI LETTURA
    /**
     * Metodo che restituisce un nodo attivo che possiede "resourceName"
     * viene usato dall'aggregatore per rispondere a una richiesta di download
     */
    public Optional<String> getActiveNodeForResource(String resourceName) {
        readLock.lock();
        try {
            Set<String> nodes = table.get(resourceName);
            if (nodes == null)
                return Optional.empty();
            // scorre i nodi che hanno la rilevazione, filtra quelli attivi e restituisce il
            // primo trovato
            return nodes.stream().filter(node -> activeNodes.contains(node)).findFirst();
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Metodo che restituisce le rilevazioni disponibili, con la lista dei nodi
     * attivi che le possiedono.
     * viene usato per il comando "listdata" dell'aggregator e "listdata remote" del
     * client
     */
    public Map<String, List<String>> getAllActiveResource() {
        readLock.lock();
        try {
            Map<String, List<String>> result = new LinkedHashMap<>();
            List<Map.Entry<String, Set<String>>> entries = new ArrayList<>(table.entrySet());
            for (int i = 0; i < entries.size(); i++) {
                Map.Entry<String, Set<String>> entry = entries.get(i);
                List<String> activeForResource = new ArrayList<>();

                List<String> nodes = new ArrayList<>(entry.getValue());
                for (int j = 0; j < nodes.size(); j++) {
                    String node = nodes.get(j);
                    if (activeNodes.contains(node)) {
                        activeForResource.add(node);
                    }
                }
                if (!activeForResource.isEmpty()) {
                    result.put(entry.getKey(), activeForResource);
                }
            }
            return result;
        } finally {
            readLock.unlock();
        }
    }

    /** Metodo che restituisce l'indirizzo ip e porta di un nodo dato il suo nome */
    public NodeAddress getNodeAddress(String nodeId) {
        readLock.lock();
        try {
            return addresses.get(nodeId);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Metodo che restituisce la lista di tutti i dodi attivi connessi al momento
     * viene usato dal comando "listdata remote" del client per mostrare i peer
     * presenti sulla rete
     */
    public List<String> getActiveNode() {
        readLock.lock();
        try {
            return new ArrayList<>(activeNodes);
        } finally {
            readLock.unlock();
        }
    }
}