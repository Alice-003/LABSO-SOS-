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

    public static int fileID() {
        String ID;
        try {
            File fileID = new File("client/Files/fileID.txt");
            if (!fileID.exists()) {
                fileID.createNewFile();
                FileWriter fw = new FileWriter(fileID);
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(generaID(5));
                bw.close();
                fw.close();
                return 0;
            } else {
                FileReader fr = new FileReader(fileID);
                BufferedReader br = new BufferedReader(fr);
                ID = br.readLine();
                br.close();
                fr.close();
                return Integer.parseInt(ID);
            }
        } catch (Exception er) {
            return -1;
        }

    }

}
