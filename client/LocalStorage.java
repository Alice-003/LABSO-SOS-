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

    private static String trovaFile(String prefisso) {
        Path dir = Paths.get("client/Files/");

        try (Stream<Path> stream = Files.list(dir)) {
            List<Path> fileTrovati = stream
                    .filter(Files::isRegularFile) // Assicura che sia un FILE e NON una cartella
                    .filter(path -> path.getFileName().toString().startsWith(prefisso))
                    .collect(Collectors.toList());

            if (!fileTrovati.isEmpty()) {

                if (prefisso.equals("Rilevazioni")) {
                    return fileTrovati.getFirst().getFileName().toString();
                } else if (prefisso.equals("Download")) {
                    return fileTrovati.toString();
                }
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
        String codice = "none";

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
                    codice = trovaFile("Rilevazioni").split("_")[1].split("\\.")[0];
                    break;

            }
            return codice;

        } catch (Exception err) {

        }
        return codice;
    }

    public static void ScriviNuoviDati(String ID) {
        Random rnd = new Random();
        int numeroMisurazioni = 10;
        int gradiMax = 30;
        int gradiMin = 20;
        int gradoGenerato;
        int pressioneMax = 1040;
        int pressioneMin = 980;
        int livelloCo2 = 450;
        int deltaCo2;

        double pressioneGenerata = pressioneMin + ((double) rnd.nextInt(pressioneMax - pressioneMin + 1));

        try {

            File fileRilevazioni = new File("client/Files/Rilevazioni_" + ID + ".csv");

            FileWriter fw = new FileWriter(fileRilevazioni, true);
            BufferedWriter bw = new BufferedWriter(fw);
            for (int i = 0; i < numeroMisurazioni; i++) {
                gradoGenerato = gradiMin + rnd.nextInt(gradiMax - gradiMin + 1);
                gradiMax = gradoGenerato + 2;
                gradiMin = gradiMin - 1;
                pressioneGenerata = pressioneGenerata + -1 + rnd.nextDouble(1 + 1);
                deltaCo2 = -10 + rnd.nextInt(21);

                bw.write(gradoGenerato + "," + String.format("%.2f", pressioneGenerata) + "," + (livelloCo2 + deltaCo2)
                        + "\r");
            }
            bw.close();
            fw.close();
        } catch (Exception er) {

        }

    }

    public static void creaFileRilevazioni() {
        try {

            File fileRilevazioni = new File("client/Files/Rilevazioni_" + ottieniCodice(1) + ".csv");
            fileRilevazioni.createNewFile();
            FileWriter fwRilevazioni = new FileWriter(fileRilevazioni, true);
            BufferedWriter bwRilevazioni = new BufferedWriter(fwRilevazioni);
            bwRilevazioni.write("Temperatura,Pressione,Livello Co2 \r");
            bwRilevazioni.close();
            fwRilevazioni.close();

        } catch (Exception error) {
            System.out.println("Errore: " + error.getMessage());
        }
    }

    public static void mostraRilevazioniLocale() {
        try {
            String path = "client/Files/";
            File fileInLettura = new File(path + trovaFile("Rilevazioni"));
            FileReader fr = new FileReader(fileInLettura);
            BufferedReader br = new BufferedReader(fr);
            String linea;
            while ((linea = br.readLine()) != null) {
                System.out.println(linea);
            }
            String fileDownload = trovaFile("Download");
            fileDownload = fileDownload.replaceAll("[\\[\\]]", "");
            for (int i = 0; i < fileDownload.length(); i++) {
                System.out.println(fileDownload.split(",")[i]);
                fr = new FileReader(new File(fileDownload.split(",")[i].trim()));
                br = new BufferedReader(fr);
                while ((linea = br.readLine()) != null) {
                    System.out.println(linea);
                }

                br.close();
                fr.close();
            }
        } catch (Exception er) {

        }
    }
}