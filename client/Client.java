package client;

import java.util.Scanner;

import java.io.*;

public class Client {

    public static void main(String[] args) {
        CommandHandler commandHandler = new CommandHandler();
        Scanner scn = new Scanner(System.in);
        String input;
        int comando;
        Boolean continuaCiclo = true;
        int lunghezzaID = 5;
        String ID;

        if (args.length != 2) {
            System.out.println("Errore input. Riavviare il programma!");
        } else {

            try {
                File fileID = new File("client/Files/NodoID.txt");
                File fileRilevazioni = new File("client/Files/Rilevazioni_" + LocalStorage.ottieniCodice(2) + ".csv");

                if (!fileID.exists() && !fileRilevazioni.exists()) {
                    LocalStorage.creaFileID(lunghezzaID);
                    LocalStorage.creaFileRilevazioni();
                } else if (fileID.exists() && !fileRilevazioni.exists()) {
                    LocalStorage.creaFileRilevazioni();
                } else if (!fileID.exists() && fileRilevazioni.exists()) {
                    LocalStorage.creaFileID(lunghezzaID, LocalStorage.ottieniCodice(2));
                }

                ID = LocalStorage.ottieniCodice(1);
                LocalStorage.ScriviNuoviDati(ID);

            } catch (Exception er) {
                System.out.println("Errore: " + er.getMessage());
                scn.close();
                return;
            }

            while (continuaCiclo) {
                System.out.print("> ");
                input = scn.nextLine();
                comando = commandHandler.gestioneComandi(input);
                switch (comando) {
                    case 1:
                        System.out.println("Risorse:");
                        LocalStorage.mostraRilevazioniLocale();
                        break;
                    case 2:
                        break;
                    case 3:
                        continuaCiclo = false;
                        break;
                    case 4:
                        break;
                    case 5:
                        break;
                    case -1:
                        System.out.println("Comando non valido!");
                        break;
                }

            }
        }
        scn.close();
    }
}
