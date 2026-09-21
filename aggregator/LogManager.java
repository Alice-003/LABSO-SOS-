package aggregator;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.concurrent.locks.*;
import java.util.List;
import java.util.Collections;

public class LogManager {
    private static final DateTimeFormatter TIME_FMT=DateTimeFormatter.ofPattern("HH:mm");

    /**RECORD INTERNO
     * rappresenta un singolo evento di download registrato dall'aggregator
     * usa un record java
     */
    record LogEntry(
        LocalTime time,
        String resource,
        String fromNode,
        String toNode,
        boolean success
    ){
        //formato output: "- HH:mm <risorsa> da: <from> a: <to> [FALLITO]"
        @Override
        public String toString(){
            String status;
            if (success) {
                status = "";
            } else {
                status = " [FALLITO]";
            }
            return String.format("- %s %s da: %s a: %s%s", time.format(TIME_FMT), resource, fromNode, toNode, status);
        }
    }

    //STATO E LOCK
    private final List<LogEntry> entries=new ArrayList<>();
    private final ReentrantReadWriteLock rwLock=new ReentrantReadWriteLock();
    private final Lock readLock=rwLock.readLock();
    private final Lock writeLock=rwLock.writeLock();

    //API 
    /**Metodo che registra un tentativo di download
     * viene chiamato da ClientHandler ogni volta che un nodo richiede una rilevazione
     */
    public void log(String resource, String fromNode, String toNode, boolean success){
        writeLock.lock();
        try{
            entries.add(new LogEntry(LocalTime.now(), resource, fromNode, toNode, success));
        }finally{
            writeLock.unlock();
        }
    }

    /**Metodo che restituisce una stringa formattata con tutti gli eventi registrati
     * pronta per essere stampata come output del comando "log"
     */
    public String getFormattedLog(){
        readLock.lock();
        try{
            if(entries.isEmpty()){
                return "Nessun download registrato.";
            }
            StringBuilder sb=new StringBuilder("Risorse scaricate:\n");
            for (int i = 0; i < entries.size(); i++) {
                LogEntry entry = entries.get(i);
                sb.append(entry);
                if (i < entries.size() - 1)
                    sb.append('\n');
            }
            return sb.toString();
        }finally{
            readLock.unlock();
        }
    }

    /**Metodo che restituisce una copia non modificabile della lista, utile per test
     */
    public List<LogEntry> getEntries(){
        readLock.lock();
        try{
            return Collections.unmodifiableList(new ArrayList<>(entries));
        }finally{
            readLock.unlock();
        }
    }
}
