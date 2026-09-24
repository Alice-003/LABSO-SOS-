package client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

import client.protocol.Protocol;

public class Client {

    public static final Object lock = new Object();

    public static void main(String[] args) {

        int lunghezzaID = 5;
        String ID;
        int indiceUltimoValoreLetto = 0;
        Boolean fileRilevazioniCancellato = false;

        // Verifica che gli input siano due: ip porta
        if (args.length != 2) {
            System.out.println("Errore input. Riavviare il programma!");
        } else {

            try {
                File fileID = new File("client/Files/NodoID.txt");
                String nomeRilevTrovato = LocalStorage.trovaFile("Rilevazioni");
                File fileRilevazioni = nomeRilevTrovato.isEmpty()
                        ? new File("client/Files/Rilevazioni_temp.csv") // Oggetto fittizio solo per far partire i
                                                                        // controlli
                        : new File("client/Files/" + nomeRilevTrovato);

                // Verifica che il file che contiene l'id del nodo e il file delle rilevazione
                // esistano
                // Se il file nodo viene cancellato, allora viene ricreato riprendendolo dal
                // file delle rilevazioni
                // Se il file delle rilevazioni viene cancellato, allora viene ricreato
                // Se entrambi i file vengono cancellati, vengono ricreati entrambi con
                // un ID diverso
                // Inoltre viene fatta una verifica per un controllo riguardante l'indice
                // dell'ultimo dato trasmesso. Se il file delle rilevazioni
                // viene cancellato allora l'indice viene resettato a 0

                if (!fileID.exists() && !fileRilevazioni.exists()) {
                    LocalStorage.creaFileID(lunghezzaID);
                    LocalStorage.creaFileRilevazioni();
                    fileRilevazioniCancellato = true;
                } else if (fileID.exists() && !fileRilevazioni.exists()) {
                    LocalStorage.creaFileRilevazioni();
                    fileRilevazioniCancellato = true;

                } else if (!fileID.exists() && fileRilevazioni.exists()) {
                    LocalStorage.creaFileID(LocalStorage.ottieniCodice(2));
                }
                fileRilevazioni = new File("client/Files/" + LocalStorage.trovaFile("Rilevazioni"));
                ID = LocalStorage.ottieniCodice(1);
                LocalStorage.ScriviNuoviDati(ID);
                String host = args[0];
                int port = Integer.parseInt(args[1]);
                Socket s = new Socket(host, port);

                // creo la connessione per il nodo sensore - nodo sensore istanziando
                // DownloadManager
                // durante la creazione del socket viene creata la porta di connessione
                // randomicamente
                // mi registro all'aggregatore con le informazioni del nodo
                // (info: nome del nodo, porta di ascolto del nodo [mentre fa da "server" in
                // connessione sensore-sensore])

                PrintWriter to = new PrintWriter(s.getOutputStream(), true);
                DownloadManager dw = new DownloadManager(lock);
                Thread thDownloadManager = new Thread(dw);
                thDownloadManager.setDaemon(true);
                thDownloadManager.start();
                System.out.println("Porta lato sever nodo: " + dw.getLocalPort());
                to.println(Protocol.RICHIESTA_REGISTRAZIONE_A_SERVER + Protocol.SEPARATORE
                        + LocalStorage.ottieniCodice(1) + Protocol.SEPARATORE + dw.getLocalPort());

                Scanner from = new Scanner(s.getInputStream());
                String response = from.nextLine();

                // viene fatta una richiesta di collegamento all'aggregatore sia per la prima
                // volta che per le successive. Dopo l'esito positivo vengono trasmesse le
                // rilevazioni non ancora trasmesse.

                File fileIndice = new File("client/Files/indice.txt");
                if (fileRilevazioniCancellato) {
                    LocalStorage.resettaIndice(fileIndice);
                }
                System.out.println(response);
                if (response.split(Protocol.SEPARATORE)[0].equals(Protocol.TUTTO_OK)) {

                    FileReader fr = new FileReader(fileRilevazioni);
                    BufferedReader br = new BufferedReader(fr);
                    String str = "";
                    // Se il file delle rilevazioni è stato cancellato, viene resettato l'indice.
                    if (!fileIndice.exists()) {
                        fileIndice.createNewFile();
                        LocalStorage.resettaIndice(fileIndice);
                    } else {
                        // Viene letto l'indice dell'ultimo valore trasmesso. Così il programma
                        // riconsoce se le ultime rilevazioni non sono state trasmesse
                        FileReader frIndice = new FileReader(fileIndice);
                        BufferedReader brIndice = new BufferedReader(frIndice);
                        indiceUltimoValoreLetto = Integer.parseInt(brIndice.readLine());
                        brIndice.close();
                        frIndice.close();
                    }
                    // Viene spostato il puntatore del file fino all'indice dell'ultima riga
                    // trasmessa. Altrimenti verrebbero ritrasmesse sempre.
                    for (int i = 0; i < indiceUltimoValoreLetto; i++) {
                        br.readLine();
                    }

                    // metto questo if per far si che la prima riga non venga inviata, altriementi
                    // ritroverei una rilevazione col nome corrispondente a Nome
                    if (indiceUltimoValoreLetto == 0) {
                        str = br.readLine();
                    }
                    // Vengono trasmesse le nuove rilevazioni all'aggregatore con il
                    // formato:nome,temperatura,pressione,livelloCo2
                    // Viene incrementato l'indice
                    while ((str = br.readLine()) != null) {
                        to.println(Protocol.AGGIUNGI_RISORSA + Protocol.SEPARATORE + str.split(",")[0]
                                + Protocol.SEPARATORE + str.split(",")[1] + Protocol.SEPARATORE + str.split(",")[2]
                                + Protocol.SEPARATORE + str.split(",")[3]);
                        indiceUltimoValoreLetto++;
                    }
                    // L'indice aggiornato viene sovrascritto sul file dedicato
                    FileWriter fwIndice = new FileWriter(fileIndice);
                    BufferedWriter bwIndice = new BufferedWriter(fwIndice);
                    bwIndice.write(String.valueOf(indiceUltimoValoreLetto));
                    bwIndice.close();
                    fwIndice.close();
                    br.close();
                    fr.close();

                }
                // Vengono stanziati i thread sender per le trasmissioni dei comandi e receiver
                // per le risposte da parte del server
                Thread sender = new Thread(new Sender(s, lock));
                Thread receiver = new Thread(new Receiver(s, sender));
                sender.start();
                receiver.start();
                try {
                    sender.join();
                    receiver.join();

                    s.close();
                    from.close();
                    System.out.println("Socket closed");
                } catch (InterruptedException e) {
                    System.out.println("ERRORE INTERCETTATO NEL CATCH:");
                    e.printStackTrace();
                    from.close();
                    return;
                }
            } catch (Exception er) {
                System.out.println(er.getMessage());
                return;
            }

        }
    }
}
