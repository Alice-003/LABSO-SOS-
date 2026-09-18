package client;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.HashSet;

public class LocalStorage {
    public static HashSet<String> codici = new HashSet<String>();

    // Il metodo genera un codice casuale utilizzato sia per generare l'ID di un
    // nodo sia per generare il codice della rilevazione

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

    public static String trovaFile(String prefisso) {
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

    public static void creaFileID(String codice) {
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

    // Ottieni il codice attraverso sia il file ID oppure attraverso il file
    // Rilevazioni
    // Può essere ottenuto in due modi a seconda di quale file manca per esempio

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

    // Metodo che serve per trasmettere all'aggregatore nuovi dati realistici
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
                String codiceGenerato = generaCodice(5);
                while (true) {
                    if (!codici.contains(codiceGenerato)) {
                        break;
                    } else {
                        codiceGenerato = generaCodice(5);
                    }
                }
                codici.add(codiceGenerato);
                deltaCo2 = -10 + rnd.nextInt(21);
                bw.write(
                        generaCodice(5) + "," + gradoGenerato + ","
                                + String.format(Locale.US, "%.2f", pressioneGenerata) + ","
                                + (livelloCo2 + deltaCo2)
                                + "\r");
            }
            bw.close();
            fw.close();
        } catch (Exception er) {

        }

    }

    // metodo che viene richiamato quando viene creato un nuovo nodo sensore oppure
    // quando viene ricreato
    public static void creaFileRilevazioni() {
        try {

            File fileRilevazioni = new File("client/Files/Rilevazioni_" + ottieniCodice(1) + ".csv");
            fileRilevazioni.createNewFile();
            FileWriter fwRilevazioni = new FileWriter(fileRilevazioni, true);
            BufferedWriter bwRilevazioni = new BufferedWriter(fwRilevazioni);
            bwRilevazioni.write("Nome,Temperatura,Pressione,Livello Co2 \r");
            bwRilevazioni.close();
            fwRilevazioni.close();

        } catch (Exception error) {
            System.out.println("Errore: " + error.getMessage());
        }
    }
    // Il metodo che viene richiamato quando l'utente scrive come input: listdata
    // local

    public static void mostraRilevazioniLocale() {
        try {
            String path = "client/Files/";
            File fileInLettura = new File(path + trovaFile("Rilevazioni"));
            FileReader fr = new FileReader(fileInLettura);
            BufferedReader br = new BufferedReader(fr);
            String linea;
            br.readLine();
            while ((linea = br.readLine()) != null) {
                System.out.println("- " + linea.split(",")[0]);
            }
            br.close();
            fr.close();
        } catch (Exception er) {

        }
    }

    public static Boolean nomeRilevazionePresente(String nome) {
        /*
         * uso try withresources che chiude automaticamente fr e br anche se si fa
         * return nel mezzo e
         * il percorso del file è passato direttamente a FileReader, senza variabile
         * intermedia
         */
        try (FileReader fr = new FileReader("client/Files/" + LocalStorage.trovaFile("Rilevazioni"));
                BufferedReader br = new BufferedReader(fr)) {

            String str;
            br.readLine();
            // Viene fatta una lettura dei nomi delle rilevazioni, dato che i nomi delle
            // rilevazioni deve essere univoca
            while ((str = br.readLine()) != null) {
                if (str.split(",")[0].equals(nome)) {
                    return true; // br e fr vengono chiusi automaticamente
                }
            }
        } catch (Exception er) {
            System.out.println("Errore: " + er.getMessage());
        }

        return false;
    }

    public static void resettaIndice(File fileIndice) {

        try {
            FileWriter fwIndice = new FileWriter(fileIndice);
            BufferedWriter bwIndice = new BufferedWriter(fwIndice);
            bwIndice.write("0");
            bwIndice.close();
            fwIndice.close();
        } catch (Exception er) {

        }
    }

}