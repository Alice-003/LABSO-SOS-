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
    // nodo sia per generare il nome del file: Rilevazioni_[ID].csv

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

    // Il metodo serve per cercare un file basandosi sul prefisso del suo nome dato
    // che, quello completo,
    // non è conosciuto a priori in quanto composto da due elementi (prefisso e id
    // del nodo)

    public static String trovaFile(String prefisso) {
        Path dir = Paths.get("client/Files/");

        try (Stream<Path> stream = Files.list(dir)) {
            List<Path> fileTrovati = stream
                    .filter(Files::isRegularFile) // Assicura che sia un FILE e NON una cartella
                    .filter(path -> path.getFileName().toString().startsWith(prefisso))
                    .collect(Collectors.toList());

            if (!fileTrovati.isEmpty() && prefisso.equals("Rilevazioni")) {
                return fileTrovati.getFirst().getFileName().toString();
            }

        } catch (Exception er) {
            System.out.println("Errore: " + er.getMessage());
        }
        return "";

    }

    // Il metodo genera il file NodoID.txt contente l'id del nodo
    // Il codice id generato è nuovo e creato casualmente, in quanto al momento del
    // richiamo del metodo, non esiste il
    // file rilevazioni.
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

    // Il metodo rigenera il file NodoID.txt prendendo in input il codice da
    // scrivere.

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

    // Ottieni il codice o tramite il file ID o attraverso il file
    // Rilevazioni a seconda di quale manca dei due

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

    // Metodo che serve per creare dati realistici, generati randomicamente, entro
    // un range prestabilito per poi scriverli nel file rilevazioni

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
            System.out.println("Errore: " + er.getMessage());
        }

    }

    // metodo che serve a creare il file Rilevazioni o quando viene avviato un nuovo
    // nodo sensore oppure
    // quando il file è stato cancellato e deve essere rigenerato.
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

    // Il metodo serve per stampare sul terminale i nomi delle rilevazioni locali
    // quando viene lanciato il comando listdata local

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

    // Metodo che serve a garantire l'unicità del nome della rilevazione nel file
    // locale

    public static Boolean nomeRilevazionePresente(String nome) {

        try (FileReader fr = new FileReader("client/Files/" + LocalStorage.trovaFile("Rilevazioni"));
                BufferedReader br = new BufferedReader(fr)) {
            String str;
            br.readLine();
            while ((str = br.readLine()) != null) {
                if (str.split(",")[0].equals(nome)) {
                    return true;
                }
            }
        } catch (Exception er) {
            System.out.println("Errore: " + er.getMessage());
        }

        return false;
    }

    // Metodo che serve a resettare l'indice nel caso in cui il file Rilevazioni
    // venga cancellato
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