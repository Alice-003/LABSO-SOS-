package aggregator;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

public class Master {
    
    public static void main(String[] args) {
        /**1) legge la porta da linea di comando
        *se non viene passata la porta, stampa le istruzioni di utilizzo 
        */
        if(args.length!=1){
            System.err.println("Uso: java Master <porta>");
            return;
        }

        int port;
        try{
            port=Integer.parseInt(args[0]);
        }catch(NumberFormatException e){
            System.err.println("Porta non valida: "+args[0]);
            return;
        }

        /**2) crea istanze condivise tra tutti i thread */
        ResourceTable resourceTable=new ResourceTable();
        LogManager logManager=new LogManager();

        /**3) apre ServerSocket sulla porta e gestisco la chiusura dello Scanner*/
        try(ServerSocket serverSocket=new ServerSocket(port); 
            Scanner in=new Scanner(System.in);){
            System.out.println("Aggregatore avviato sulla porta "+ port);

            /**4) creo un thread separato che accetta connessioni in loop */
            Thread acceptThread=new Thread(()->{
                while(!serverSocket.isClosed()){
                    try{
                        Socket socket=serverSocket.accept();
                        System.out.println("Nuovo nodo connesso: "+socket.getInetAddress());

                        new Thread(new ClientHandler(socket, resourceTable, logManager)).start();

                    }catch(IOException e){
                        //se il socket è chiuso l'eccezione attende "quit" altrimenti viene segnalata
                        if(!serverSocket.isClosed())
                            System.err.println("Errore accettazione connessione: "+ e.getMessage());
                    }
                }
            });

            //thread daemon: il thread termina automaticamente quando termina il thread principale
            acceptThread.setDaemon(true);
            acceptThread.start();

            /**5) loop interattivo nel thread principale */
            while(true){
                String input=in.nextLine().trim();
                switch(input){
                    case "listdata":
                        Map<String, java.util.List<String>> resources=resourceTable.getAllActiveResource();
                        if(resources.isEmpty()){
                            System.out.println("Nessuna rilevazione disponibile sulla rete.");
                        }else{
                            System.out.println("Risorse:");
                            
                            List<Map.Entry<String, List<String>>> entryList = new ArrayList<>(resources.entrySet());
                            for (int i=0; i<entryList.size(); i++) {
                                Map.Entry<String, List<String>> entry=entryList.get(i);
                                System.out.println("- " + entry.getKey() + ": " + String.join(", ", entry.getValue()));
                            }
                        }
                    break;
                    case "log":
                        System.out.println(logManager.getFormattedLog());
                    break;
                    case "quit":
                        System.out.println("Aggregator in chiusura...");
                        serverSocket.close(); //causa l'uscita dal loop del thread accettatore
                        return;
                    default:
                        System.out.println("Comando non riconosciuto. Scegli tra: 'listdata', 'log', 'quit'");
                }
            }

        } catch (IOException e) {
            System.err.println("Errore avvio dell'aggregatore: "+ e.getMessage());
        }

    }
}
