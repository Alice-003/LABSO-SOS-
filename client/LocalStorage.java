package client;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public static String trovaFile() {
        Path dir = Paths.get("client/Files/");
        String prefisso = "Rilevazioni";
        String codice = "f";
        try (Stream<Path> stream = Files.list(dir)) {
            List<Path> fileTrovati = stream
                    .filter(Files::isRegularFile) // Assicura che sia un FILE e NON una cartella
                    .filter(path -> path.getFileName().toString().startsWith(prefisso))
                    .collect(Collectors.toList());

            if (!fileTrovati.isEmpty()) {
                return fileTrovati.getFirst().getFileName().toString();
            }

        } catch (Exception er) {
            System.out.println("Errore: " + er.getMessage());
        }
        return "";

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

    public static void creaFileID(int lunghezzaID, String codice) {
        try {
            File fileID = new File("client/Files/NodoID.txt");
            fileID.createNewFile();
            FileWriter fw = new FileWriter(fileID);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(codice);

            bw.close();
            fw.close();
        } catch (Exception error) {
            System.out.println("Errore: " + error.getMessage());
        }

    }

    public static String ottieniCodice(int index) {
        String codice = "";

        try {
            switch (index) {
                case 1:
                    File fileID = new File("client/Files/NodoID.txt");
                    FileReader fw = new FileReader(fileID);
                    BufferedReader bw = new BufferedReader(fw);
                    codice = bw.readLine();

                    bw.close();
                    fw.close();

                    break;

                case 2:
                    codice = trovaFile().split("_")[1].split("\\.")[0];
                    break;

            }
            return codice;

        } catch (Exception err) {

        }
        return codice;
    }

    public static void ScriviNuoviDati() {
        int numeroMisurazioni = 10;

    }

    public static void creaFileRilevazioni() {
        try {
            File fileID = new File("client/Files/NodoID.txt");
            FileReader fw = new FileReader(fileID);
            BufferedReader bw = new BufferedReader(fw);
            String codice = bw.readLine();
            bw.close();
            fw.close();
            File fileRilevazioni = new File("client/Files/Rilevazioni_" + codice + ".csv");
            fileRilevazioni.createNewFile();

        } catch (Exception error) {
            System.out.println("Errore: " + error.getMessage());
        }
    }
}