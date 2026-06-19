package client;

import java.io.*;
import java.util.Random;

public class LocalStorage {
    private static String generaCodice(int lunghezaID) {
        String codice = "";
        Random rnd = new Random();

        int min = 48;
        int max = 57;
        int ris = 0;
        for (int i = 0; i < lunghezaID; i++) {
            ris = min + rnd.nextInt(max - min + 1);
            codice = codice + ((char) ris);
        }
        return codice;
    }

    public static void creaFileID(int lunghezzaID) {
        try {
            File fileID = new File("client/Files/NodoID.txt");
            fileID.createNewFile();
            FileWriter fw = new FileWriter(fileID);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(generaCodice(lunghezzaID));
            bw.close();
            fw.close();
        } catch (Exception error) {
            System.out.println("Errore: " + error.getMessage());
        }

    }

    public static String ottineCodice() {
        String codice = "";
        try {
            File fileID = new File("client/Files/NodoID.txt");
            FileReader fw = new FileReader(fileID);
            BufferedReader bw = new BufferedReader(fw);
            codice = bw.readLine();
            System.out.println("codice: " + codice);
            bw.close();
            fw.close();
        } catch (Exception err) {

        }
        return codice;
    }

    public static void creaFileRilevazioni() {
        try {
            File fileRilevazioni = new File("client/Files/Rilevazioni_" + ottineCodice() + ".csv");
            fileRilevazioni.createNewFile();

        } catch (Exception error) {
            System.out.println("Errore: " + error.getMessage());
        }
    }
}