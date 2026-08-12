package aggregator;

import aggregator.protocol.Aggregator_Message;
import aggregator.protocol.Aggregator_Protocol;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

/**
 * Gestisce la comunicazione con un singolo client connesso all'aggregatore
 * Ogni client connesso all'aggregatore viene gestito da un thread separato,
 * che esegue il metodo run() di questa classe
 * 
 * Legge i comandi dal nodo, li interpreta con Aggregator_Message,
 * delega la logica a ResourceTable e LogManager, e risponde seguendo
 * le costanti definite in Aggregator_Protocol 
 * 
 */


public class ClientHandler implements Runnable {
    
    // la socket della connessione con questo specifico nodo sensore
    Socket s;

    // tabella delle rilevazioni, condivisa tra tutti i ClientHandler
    ResourceTable resourceTable;
    
    // log dei download, condiviso tra tutti i ClientHandler
    LogManager logManager;

    // dati del nodo connesso: restano null/-1 finchè il nodo non manda REGISTER
    String nodeName = null;
    String nodeIp = null;
    int nodePort = -1;

    // costruttore: riceve la socket e le risorse condivise dall'aggregatore
    public ClientHandler(Socket s, ResourceTable resourceTable, LogManager logManager) {
        this.s = s;
        this.resourceTable = resourceTable;
        this.logManager = logManager;
    }

    @Override
    public void run() {
        try{
            // apro gli stream per leggere e scrivere sulla socket
            Scanner from = new Scanner(s.getInputStream());
            PrintWriter to = new PrintWriter(s.getOutputStream(), true);

            // chiediamo alla socket l'IP del nodo che si è connesso, e lo salviamo nella variabile nodeIP
            nodeIp = s.getInetAddress().getHostAddress();

            System.out.println("Thread" + Thread.currentThread().getName() + " listening...");

            // flag che controlla il loop principale: diventa true solo con QUIT
            boolean closed = false;
            
            // loop continua finchè il nodo non manda QUIT
            // o finchè ci sono ancora righe da leggere
            while (!closed && from.hasNextLine()){
                
                // leggo la prossima riga inviata dal nodo
                String request = from.nextLine();
                
                // divido la riga in al max 3 parti (comando, nome risorsa, contenuto/risorse)
                // usata più sotto solo per estrarre le risorse iniziali nel REGISTER
                String[] parts = request.split(Aggregator_Protocol.SEP, Aggregator_Protocol.MAX_SPLIT);
                

                // controllo se il thread ha ricevuto un interrupt dall'esterno
                if(!Thread.interrupted()){
                    System.out.println("Request: " + request);
                    
                    try{
                        // trasformo la riga grezza in un oggetto Aggregator_Message
                        // più comodo da leggere (comando + argomenti)

                        Aggregator_Message msg = Aggregator_Message.parse(request);
                        
                        // smisto il comando ricevuto
                        switch (msg.getCommand()){

                            case Aggregator_Protocol.REGISTER:
                                if (nodeName != null) {
                                    to.println(Aggregator_Protocol.ERROR + " Nodo già registrato come " + nodeName);
                                    break;
                                }
                                // un nodo si registra: deve fornire almeno nome e porta
                                if(!msg.hasAtLeast(2)){
                                    to.println(Aggregator_Protocol.ERROR + " Formato errato, serve: REGISTER nomeNodo porta");
                                    break;
                                }
                                
                                String nomeNodoCandidato = msg.getArg(0);
                                if (!nomeNodoCandidato.matches("[a-zA-Z0-9_]+")) {
                                    to.println(Aggregator_Protocol.ERROR + "Il nome del nodo può contenere solo lettere, numeri e underscore");
                                    break;
                                }
                                
                                nodeName = nomeNodoCandidato;
                                
                                // salvo la porta del nodo, convertendola in numero
                                try {
                                    nodePort = Integer.parseInt(msg.getArg(1));
                                } catch(NumberFormatException e) {
                                    to.println(Aggregator_Protocol.ERROR + " La porta deve essere un numero");
                                    nodeName = null;
                                    break;
                                }

                                // costruisco la lista delle rilevazioni iniziali del nodo
                                List<String> resourceList;
                                if (parts.length > 2 && parts[2] != null && !parts[2].trim().isEmpty()) {
                                    
                                    // se ci sono rilevazioni elencate, le divido sullo spazio
                                    String[] resourcesArray = parts[2].split(Aggregator_Protocol.SEP);
                                    
                                    // le trasformo in una List, perchè registerNodes la richiede così
                                    resourceList = Arrays.asList(resourcesArray);
                                } else {
                                    // se non ci sono rilevazioni elencate, passo una lista vuota
                                    resourceList = List.of();
                                }
                                
                                // registro il nodo e le sue rilevazioni iniziali nella ResourceTable
                                resourceTable.registerNodes(nodeName, resourceList, nodeIp, nodePort);

                                System.out.println("Nodo registrato: " + nodeName + " @ " + nodeIp + ":" + nodePort);
                                to.println(Aggregator_Protocol.OK + " Benvenuto " + nodeName);
                                break;

                            case Aggregator_Protocol.ADD:
                                // un nodo deve essersi registrato prima di poter aggiungere rilevazioni
                                if (nodeName == null){
                                    to.println(Aggregator_Protocol.ERROR + " Nodo non registrato, invia REGISTER");
                                    break;

                                }
                                
                                // ADD richiede sia il nome della risorsa che il contenuto
                                if(!msg.hasAtLeast(1)){
                                    to.println(Aggregator_Protocol.ERROR + " Formato errato, serve: ADD nomeRisorsa");
                                    break;
                                }
                                // nome della rilevazione è il primo argomento
                                String nomeRisorsa = msg.getArg(0);
                                
                                // non leggo il contenuto, l'aggregatore traccia solo chi possiede la risorsa
                                
                                // registro nella ResourceTable che questo nodo possiede questa risorsa
                                resourceTable.addResource(nodeName, nomeRisorsa);

                                System.out.println("Nodo " + nodeName + " ha aggiunto risorsa: " + nomeRisorsa);
                                to.println(Aggregator_Protocol.OK);
                                break;

                            case Aggregator_Protocol.LISTDATA_REMOTE:
                                if(nodeName == null){
                                    to.println(Aggregator_Protocol.ERROR + " Nodo non registrato, invia REGISTER");
                                    break;
                                }

                                // chiedo alla ResourceTable tutte le rilevazioni attive,
                                // con la lista dei nodi che le possiedono:
                                // Map<nomeRisorsa, listaNodiChePossiedono>
                                Map<String, List<String>> tutteLeRisorse = resourceTable.getAllActiveResource();

                                // inizio la risposta multi-riga
                                to.println(Aggregator_Protocol.DATA);
                                
                                // per ogni rilevazione presente in rete, costruiscono e invio una riga
                                for (Map.Entry<String, List<String>> entry : tutteLeRisorse.entrySet()) {
                                    String nomeRisorsaEntry = entry.getKey();
                                    List<String> nodiChePossiedono = entry.getValue();
                                    
                                    // formatto la riga
                                    String riga = nomeRisorsaEntry + Aggregator_Protocol.SEP 
                                    + String.join(Aggregator_Protocol.SEP, nodiChePossiedono);
                                    to.println(riga);
                                }
                                to.println(Aggregator_Protocol.END);
                                break;

                            case Aggregator_Protocol.DOWNLOAD_REQUEST:
                                // il nodo deve essersi registrato prima di chiedere un download
                                if(nodeName == null) {
                                    to.println(Aggregator_Protocol.ERROR + " Nodo non registrato, invia REGISTER");
                                    break;
                                }

                                // serve il nome della risorsa da scaricare
                                if(!msg.hasAtLeast(1)){
                                    to.println(Aggregator_Protocol.ERROR + " Formato errato, serve: DOWNLOAD_REQUEST nomeRisorsa");
                                    break;
                                }

                                String risorsaRichiesta = msg.getArg(0);
                                
                                // cerco un nodo attivo che possiede questa rilevazione
                                                                
                                Optional<String> peer0pt = resourceTable.getActiveNodeForResource(risorsaRichiesta);
                                if(peer0pt.isEmpty()){
                                    to.println(Aggregator_Protocol.ERROR + " Nessun nodo ha questa rilevazione: " + risorsaRichiesta);                            
                                } else {
                                    
                                    // ho trovato un nodo che possiede la risorsa
                                    String nomePeer = peer0pt.get();
                                    
                                    // chiedo alla ResourceTable l'indirizzo di rete di quel nodo
                                    ResourceTable.NodeAddress indirizzoPeer = resourceTable.getNodeAddress(nomePeer);
                                    
                                    if (indirizzoPeer == null) {
                                        // caso limite: il nodo è nella tabella delle risorse
                                        // ma non ha un indirizzo registrato
                                        to.println(Aggregator_Protocol.ERROR + " Indirizzo del nodo non disponibile: " + nomePeer);

                                    } else {
                                        // rispondo al nodo richiedente con nome, IP e porta del peer
                                        // da cui può scaricare direttamente la rilevazione
                                        to.println(Aggregator_Protocol.OK 
                                            + Aggregator_Protocol.SEP + nomePeer
                                            + Aggregator_Protocol.SEP + indirizzoPeer.ip()
                                            + Aggregator_Protocol.SEP + indirizzoPeer.port());

                                    }
                                } 
                                break;

                            case Aggregator_Protocol.DOWNLOAD_FAILED:
                                if (nodeName == null) {
                                    to.println(Aggregator_Protocol.ERROR + " Nodo non registrato, invia REGISTER");
                                    break;
                                }
                                
                                // servono sia la risorsa che il nodo peer che ha fallito
                                if (!msg.hasAtLeast(2)){
                                    to.println(Aggregator_Protocol.ERROR + " Formato errato, serve: DOWNLOAD_FAILED nomeRIsorsa nomeNodoPeer");
                                    break;
                                }
                                String risorsaFallita = msg.getArg(0);
                                String nodoPeerFallito = msg.getArg(1);
                                
                                // rimuovo l'associazione nodo-risorsa dalla tabella
                                resourceTable.removeEntity(nodoPeerFallito, risorsaFallita);
                                
                                // registro nel log il tentativo fallito
                                logManager.log(risorsaFallita, nodoPeerFallito, nodeName, false);

                                System.out.println("Download fallito: " + nodeName
                                 + " non può scaricare " + risorsaFallita + " da " + nodoPeerFallito);
                                to.println(Aggregator_Protocol.OK);
                                break;

                            case Aggregator_Protocol.DOWNLOAD_OK:
                                if (nodeName == null) {
                                    to.println(Aggregator_Protocol.ERROR + " Nodo non registrato, invia REGISTER");
                                    break;
                                }
                                // servono sia la risorsa che il nodo peer da cui ha scaricato
                                if (!msg.hasAtLeast(2)){
                                    to.println(Aggregator_Protocol.ERROR + " Formato errato, serve: DOWNLOAD_OK nomeRisorsa nomeNodoPeer");
                                    break;
                                }
                                String risorsaOk = msg.getArg(0);
                                String nodoPeerOk = msg.getArg(1);

                                // registro nel log il download riuscito
                                logManager.log(risorsaOk, nodoPeerOk, nodeName, true);

                                System.out.println("Download completato: " + risorsaOk
                                + " scaricata da " + nodoPeerOk + " a " + nodeName);
                                to.println(Aggregator_Protocol.OK);
                                break;

                            case Aggregator_Protocol.QUIT:
                                // il nodo vuole disconnettersi: imposto closed a true
                                // così il while si ferma al prossimo controllo                                
                                closed = true;
                                to.println(Aggregator_Protocol.OK + " Sessione terminata ");
                                break;

                            default:
                                // comando non riconosciuto tra quelli previsti dal protocollo
                                to.println(Aggregator_Protocol.ERROR + " Comando non riconosciuto: " + msg.getCommand());
                                                                    
                        }
                    } catch (IllegalArgumentException e){
                        // catturo eventuali errori di parsing del messaggio e li segnalo al nodo
                        to.println(Aggregator_Protocol.ERROR + " " + e.getMessage());
                    }
                                         
                }
            }            
                // inviamo il quit e chiudiamo la socket una volta usciti dal ciclo
                to.println(Aggregator_Protocol.QUIT);
                s.close();
                System.out.println("Closed");

            } catch (IOException e){
                System.err.println("ClientHandler IOException: " + e);
                e.printStackTrace();
            } finally {
                // eseguito sempre, sia in caso di chiusura normale che di errore:
                // garantisce che il nodo venga rimosso e la socket chiusa
                cleanup();
            }
    }

    // pulizia finale eseguita quando la connessione termina
    private void cleanup() {
        
        // se il nodo si era registrato, lo rimuovo della lista dei nodi attivi
        if (nodeName != null) {
        resourceTable.disconnectNodes(nodeName);
        System.out.println("Nodo" + nodeName + "rimosso dalla rete");
        }
        
        // chiudo la socket se non è già chiusa, per evitare di richiuderla
        try {
            if(s != null && !s.isClosed()) {
                s.close();
            }
        } catch (IOException e){
            System.err.println("Errore chiusura socket: " + e.getMessage());

        }

        // messaggio finale di debug
        System.out.println("Connessione chiusa per " + (nodeName != null ? nodeName : nodeIp));
                
    }
}

