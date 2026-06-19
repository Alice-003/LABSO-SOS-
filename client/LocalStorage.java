package client;

import java.io.*;
import java.util.Random;

public class LocalStorage {
    public static String generaID(int lunghezza) {
        Random rnd = new Random();
        int numeroGenerato;
        int min = 48;
        int max = 57;
        String ID = "";
        for (int i = 0; i < lunghezza; i++) {
            numeroGenerato = min + rnd.nextInt(max - min) + 1;
            ID = ID + ((char) numeroGenerato);
        }
        return ID;
    }

    public static void fileID() {
        try {
            File fileID = new File("client/Files/fileID.txt");
            if (!fileID.exists()) {
                fileID.createNewFile();
                FileWriter fw = new FileWriter(fileID);
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(generaID(5));
                bw.close();
                fw.close();

            }
        } catch (Exception er) {

        }

    }

}
