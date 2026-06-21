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


public class ClientHandler implements Runnable {
    
    Socket s;
    ResourceTable resourceTable;
    LogManager logManager;

    String nodeName = null;
    String nodeIp = null;
    int nodePort = -1;

    // costruttore
    public ClientHandler(Socket s, ResourceTable resourceTable, LogManager logManager) {
        this.s = s;
        this.resourceTable = resourceTable;
        this.logManager = logManager;
    }

    @Override
    public void run() {
        try{
            Scanner from = new Scanner(s.getInputStream());
            PrintWriter to = new PrintWriter(s.getOutputStream(), true);

            nodeIp = s.getInetAddress().getHostAddress();
            //  chiediamo alla socket l'IP del nodo che si è connesso, e lo salviamo nella variabile nodeIp

            System.out.println("Thread" + Thread.currentThread().getName() + " listening...");

            boolean closed = false;
            while (!closed && from.hasNextLine()){
                String request = from.nextLine();
                String[] parts = request.split(Aggregator_Protocol.SEP, Aggregator_Protocol.MAX_SPLIT);
                
                if(!Thread.interrupted()){
                    System.out.println("Request: " + request);
                    
                    try{
                        Aggregator_Message msg = Aggregator_Message.parse(request);
                        switch (msg.getCommand()){

                            case Aggregator_Protocol.REGISTER:
                                if(!msg.hasAtLeast(2)){
                                    to.println(Aggregator_Protocol.ERROR + " REGISTER richiede : <nomenodo> <portaAscolto>");
                                    break;
                                }
                                nodeName = msg.getArg(0);
                                try {
                                    nodePort = Integer.parseInt(msg.getArg(1));
                                } catch(NumberFormatException e) {
                                    to.println(Aggregator_Protocol.ERROR + " Porta non valida");
                                    nodeName = null;
                                    break;
                                }

                                List<String> resourceList;
                                if (parts.length > 2 && parts[2] != null && !parts[2].trim().isEmpty()) {
                                    String[] resourcesArray = parts[2].split(Aggregator_Protocol.SEP);
                                    resourceList = Arrays.asList(resourcesArray);
                                } else {
                                    resourceList = List.of();
                                }
                            
                                resourceTable.registerNodes(nodeName, resourceList);

                                System.out.println("Nodo registrato: " + nodeName + " @ " + nodeIp + ":" + nodePort);
                                to.println(Aggregator_Protocol.OK + "Benvenuto " + nodeName);
                                break;

                            case Aggregator_Protocol.ADD:
                                if (nodeName == null){
                                    to.println(Aggregator_Protocol.ERROR + "Devi prima inviare REGISTER");
                                    break;

                                }
                                if(!msg.hasAtLeast(2)){
                                    to.println(Aggregator_Protocol.ERROR + " ADD richiede: <nomeRisorsa> <contenuto>");
                                    break;
                                }
                                String nomeRisorsa = msg.getArg(0);
                                String contenuto = msg.getArg(1);
                                resourceTable.addResource(nodeName, nomeRisorsa);
                                System.out.println("Nodo " + nodeName + " ha aggiunto risorsa: " + nomeRisorsa);
                                to.println(Aggregator_Protocol.OK);
                                break;

                            case Aggregator_Protocol.LISTDATA_REMOTE:
                                if(nodeName == null){
                                    to.println(Aggregator_Protocol.ERROR + "Devi prima inviare REGISTER");
                                    break;
                                }

                                Map<String, List<String>> tutteLeRisorse = resourceTable.getAllActiveResource();

                                to.println(Aggregator_Protocol.DATA);
                                for (Map.Entry<String, List<String>> entry : tutteLeRisorse.entrySet()) {
                                    String nomeRisorsaEntry = entry.getKey();
                                    List<String> nodiChePossiedono = entry.getValue();
                                    String riga = nomeRisorsaEntry + Aggregator_Protocol.SEP 
                                    + String.join(Aggregator_Protocol.SEP, nodiChePossiedono);
                                    to.println(riga);
                                }
                                to.println(Aggregator_Protocol.END);
                                break;

                            case Aggregator_Protocol.DOWNLOAD_REQUEST:
                                if(nodeName == null) {
                                    to.println(Aggregator_Protocol.ERROR + "Devi prima inviare REGISTER");
                                    break;
                                }

                                if(!msg.hasAtLeast(1)){
                                    to.println(Aggregator_Protocol.ERROR + " DOWNLOAD REQUEST richiede: <nomeRisorsa>");
                                    break;
                                }

                                String risorsaRichiesta = msg.getArg(0);
                                Optional<String> peer0pt = resourceTable.getActiveNodeForResource(risorsaRichiesta);
                                if(peer0pt.isEmpty()){
                                    to.println(Aggregator_Protocol.ERROR + "Risorsa non disponibile: " + risorsaRichiesta);                            
                                } else {
                                    String nomePeer = peer0pt.get();
                                    ResourceTable.NodeAddress indirizzoPeer = resourceTable.getNodeAddress(nomePeer);
                                    
                                    if (indirizzoPeer == null) {
                                        to.println(Aggregator_Protocol.ERROR + " Indirizzo del nodo non disponibile: " + nomePeer);

                                    } else {
                                        to.println(Aggregator_Protocol.OK 
                                            + Aggregator_Protocol.SEP + nomePeer
                                            + Aggregator_Protocol.SEP + indirizzoPeer.ip()
                                            + Aggregator_Protocol.SEP + indirizzoPeer.port());

                                    }
                                } 
                                break;

                            case Aggregator_Protocol.DOWNLOAD_FAILED:
                                if (nodeName == null) {
                                    to.println(Aggregator_Protocol.ERROR + "Devi prima inviare REGISTER");
                                    break;
                                }
                                if (!msg.hasAtLeast(2)){
                                    to.println(Aggregator_Protocol.ERROR + " DOWNLOAD_FAILED richiede: <nomeRisorsa> <nomeNodoPeer>");
                                    break;
                                }
                                String risorsaFallita = msg.getArg(0);
                                String nodoPeerFallito = msg.getArg(1);
                                resourceTable.removeEntity(nodoPeerFallito, risorsaFallita);
                                
                                logManager.log(risorsaFallita, nodoPeerFallito, nodeName, false);

                                System.out.println("Download fallito: " + nodeName
                                 + " non può scaricare " + risorsaFallita + " da " + nodoPeerFallito);
                                to.println(Aggregator_Protocol.OK);
                                break;

                            case Aggregator_Protocol.DOWNLOAD_OK:
                                if (nodeName == null) {
                                    to.println(Aggregator_Protocol.ERROR + "Devi prima inviare REGISTER");
                                    break;
                                }
                                if (!msg.hasAtLeast(2)){
                                    to.println(Aggregator_Protocol.ERROR + " DOWNLOAD_OK richiede: <nomeRisorsa> <nomeNodoPeer>");
                                    break;
                                }
                                String risorsaOk = msg.getArg(0);
                                String nodoPeerOk = msg.getArg(1);

                                logManager.log(risorsaOk, nodoPeerOk, nodeName, true);

                                System.out.println("Download completato: " + risorsaOk
                                + " scaricata da " + nodoPeerOk + " a " + nodeName);
                                to.println(Aggregator_Protocol.OK);
                                break;

                            case Aggregator_Protocol.QUIT:
                                closed = true;
                                to.println(Aggregator_Protocol.OK + " Arrivederci ");
                                break;

                            default:
                                to.println(Aggregator_Protocol.ERROR + " Comando sconosciuto: " + msg.getCommand());
                                                                    
                        }
                    } catch (IllegalArgumentException e){
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
                cleanup();
            }
    }

    private void cleanup() {
        if (nodeName != null) {
        resourceTable.disconnectNodes(nodeName);
        System.out.println("Nodo" + nodeName + "rimosso dalla rete");
        }
        try {
            if(s != null && !s.isClosed()) {
                s.close();
            }
        } catch (IOException e){
            System.err.println("Errore chiusura socket: " + e.getMessage());

        }
        System.out.println("Connessione chiusa per " + (nodeName != null ? nodeName : nodeIp));
                
    }

}

