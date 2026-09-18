package client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import client.protocol.Protocol;

public class Sender implements Runnable {

    Socket s;

    public Sender(Socket s) {
        this.s = s;
    }

    public static String nomeRilevazioneDownload;

    @Override
    public void run() {
        System.out.println("DEBUG: Thread Sender avviato!");
        Boolean continuaCiclo = true;
        Scanner scn = new Scanner(System.in);
        String input;
        int comando;
        CommandHandler commandHandler = new CommandHandler();
        try {
            PrintWriter to = new PrintWriter(this.s.getOutputStream(), true);

            while (continuaCiclo) {

                input = scn.nextLine();
                String[] strSplit = input.split(" ");
                comando = commandHandler.gestioneComandi(input);
                switch (comando) {
                    case 1:
                        System.out.println("Risorse:");
                        LocalStorage.mostraRilevazioniLocale();
                        break;
                    case 2:
                        to.println("LISTDATA_REMOTE");
                        break;
                    case 3:
                        to.println("QUIT");
                        to.close();
                        continuaCiclo = false;
                        break;
                    case 4:
                        // In questo caso l'utente vuole aggiungere manualmente una rilevazione

                        // la stringa di input deve essere formata da add nomeRisorsa temperatura
                        // pressione LivelloCo2
                        if (strSplit.length != 5) {
                            System.out.println("Errore: numero di parametri non valido");
                        } else {

                            // Se il nome non è presente il seguente codice all'interno dell'if si occupa di
                            // aggiungere la rilevazione dell'utente
                            // e si aggiorna l'indice dell'ultima rilevazione trasmessa al server
                            File fileRilevzioni = new File(
                                    "client/Files/" + LocalStorage.trovaFile("Rilevazioni"));
                            if (!LocalStorage.nomeRilevazionePresente(strSplit[1])) {
                                try {
                                    FileWriter fwFileRilevazioni = new FileWriter(fileRilevzioni, true);
                                    BufferedWriter bwFileRilevazioni = new BufferedWriter(fwFileRilevazioni);
                                    String strScritta = "";

                                    int temperatura = Integer.parseInt(strSplit[2]);
                                    double pressione = Double.parseDouble(strSplit[3]);
                                    int livelloCo2 = Integer.parseInt(strSplit[4]);
                                    strScritta = strSplit[1] + "," + String.valueOf(temperatura) + ","
                                            + String.valueOf(pressione) + "," + String.valueOf(livelloCo2);
                                    to.println(Protocol.AGGIUNGI_RISORSA + Protocol.SEPARATORE + strSplit[1]
                                            + Protocol.SEPARATORE
                                            + temperatura + Protocol.SEPARATORE + pressione
                                            + Protocol.SEPARATORE + livelloCo2 + "\r");
                                    bwFileRilevazioni.write(strScritta + "\r");
                                    bwFileRilevazioni.close();
                                    fwFileRilevazioni.close();
                                    File fileIndice = new File("client/Files/indice.txt");
                                    FileReader frIndice = new FileReader(fileIndice);
                                    BufferedReader brIndice = new BufferedReader(frIndice);
                                    int indice = Integer.parseInt(brIndice.readLine());
                                    brIndice.close();
                                    frIndice.close();
                                    FileWriter fwIndice = new FileWriter(fileIndice, false);
                                    BufferedWriter bwIndice = new BufferedWriter(fwIndice);
                                    bwIndice.write(String.valueOf(indice + 1));
                                    bwIndice.close();
                                    fwIndice.close();
                                } catch (NumberFormatException er) {
                                    System.out.println("Errore: I paramentri devono essere numeri");
                                }

                            } else {
                                System.out.println("Una rilevazione possiede già questo nome");
                            }

                        }
                        break;
                    case 5:
                        to.println("DOWNLOAD_REQUEST" + Protocol.SEPARATORE + input.split(" ")[1]);
                        Sender.nomeRilevazioneDownload = strSplit[1];

                        break;
                    case -1:
                        System.out.println(
                                "Comando non valido: 'listdata local' 'listdata remote' 'download' 'quit'");
                        break;
                }

            }

            System.out.println("Sender closed.");
        } catch (IOException e) {
            System.err.println("IOException caught: " + e);
            e.printStackTrace();
        } finally {
            scn.close();
        }
    }

}
