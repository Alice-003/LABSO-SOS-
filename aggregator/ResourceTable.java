package aggregator;

import java.util.*;
import java.util.concurrent.locks.*;

public class ResourceTable {
    /**rilevazione: insieme di nodi che la possiedono
     * uso Set perché garantisce che ogni rilevazione ha un nome univoco
     * per nodo, quindi non si hanno duplicati
     */
    private final Map<String, Set<String>> table=new HashMap<>();
    
    /**nodi connessi all'agregator */
    private final Set<String> activeNodes=new HashSet<>();
    private final ReentrantReadWriteLock rwLock=new ReentrantReadWriteLock();
    private final Lock readLock=rwLock.readLock();
    private final Lock writeLock=rwLock.writeLock();

    /**Metodo che registra un nodo appena si connette e la sua relativa lista
     * delle rilevazini. 
     * viene chiamato all'avvio del sensore dal ClientHandler
     * solo un thread alla volta può scrivere quindi gli altri devono espettare
     * il rilascio del lock.
    */
   public void registerNodes(String nodeId, List<String> resources){
    writeLock.lock(); //acquisisce il lock
    try{
        activeNodes.add(nodeId);
        for(int i=0; i<resources.size(); i++){
            String resource=resources.get(i);
            //se la rilevazione non è ancora nella tabella creo un nuovo Set e poi aggiungo il nodo
            table.computeIfAbsent(resource, k->new HashSet<>()).add(nodeId);
        }
    }finally{
        writeLock.unlock(); //rilascio del lock
    }
   }

   /** Metodo che aggiunge una rilevazione a un nodo già connesso.
    * viene chiamato quando un sensore esegue il comando "add"
   */
   public void addRecouce(String nodeId, String resourceName){
    writeLock.lock();
    try{
        table.computeIfAbsent(resourceName, k->new HashSet<>()).add(nodeId);
    }finally{
        writeLock.unlock();
    }
   }

   /**Metodo che segnala che un sensore si è disconnesso
    * le relative rilevazioni non saranno più accessibili
    * viene chiamato quando viene eseguito il comando "quit"
    */
   public void disconnectNodes(String nodeId){
    writeLock.lock();
    try{
        activeNodes.remove(nodeId);
    }finally{
        writeLock.unlock();
    }
   }

   /**Metodo che rimuove la coppia nodo-rilevazione dalla tabella
    * viene chiamato dall'aggregator quando un tentativo di download fallisce,
    * quindi il nodo non verrà più proposto per quella rilevazione
    */
   public void removeEntity(String nodeId, String resourceName){
    writeLock.lock();
    try{
        Set<String> nodes=table.get(resourceName);
        if(nodes!=null){
            nodes.remove(nodeId);
            if(nodes.isEmpty()){
                table.remove(resourceName);
            }
        }
    }finally{
        writeLock.unlock();
    }
   }
}
