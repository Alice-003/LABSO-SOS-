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

public class Sender implements Runnable {

    Socket s;

    public Sender(Socket s) {
        this.s = s;
    }

    @Override
    public void run() {

        Boolean continuaCiclo = true;
        Scanner scn = new Scanner(System.in);
        String input;
        int comando;
        CommandHandler commandHandler = new CommandHandler();
        try {
            PrintWriter to = new PrintWriter(this.s.getOutputStream(), true);

            while (continuaCiclo) {
                synchronized (Client.consoleLock) {
                    synchronized (Client.consoleLock) {
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
                                to.close();
                                continuaCiclo = false;
                                break;
                            case 4:
                                String[] strSplit = input.split(" ");
                                Boolean nomeScrittoPrecedenza = false;
                                if (strSplit.length != 5) {
                                    System.out.println("Errore: numero di parametri non valido");
                                } else {
                                    File fileRilevzioni = new File(
                                            "client/Files/" + LocalStorage.trovaFile("Rilevazioni"));

                                    FileReader fr = new FileReader(fileRilevzioni);
                                    BufferedReader br = new BufferedReader(fr);
                                    String str;
                                    br.readLine();
                                    while ((str = br.readLine()) != null) {
                                        if (str.split(",")[0].equals(strSplit[1])) {
                                            nomeScrittoPrecedenza = true;
                                        }
                                    }
                                    br.close();
                                    fr.close();
                                    if (!nomeScrittoPrecedenza) {
                                        FileWriter fwFileRilevazioni = new FileWriter(fileRilevzioni, true);
                                        BufferedWriter bwFileRilevazioni = new BufferedWriter(fwFileRilevazioni);
                                        String strScritta = "";

                                        int temperatura = Integer.parseInt(strSplit[2]);
                                        double pressione = Double.parseDouble(strSplit[3]);
                                        int livelloCo2 = Integer.parseInt(strSplit[4]);
                                        strScritta = strSplit[1] + "," + String.valueOf(temperatura) + ","
                                                + String.valueOf(pressione) + "," + String.valueOf(livelloCo2);

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

                                    } else {
                                        System.out.println("Una rilevazione possiede già questo nome");
                                    }
                                }
                                break;
                            case 5:
                                break;
                            case -1:
                                System.out.println("Comando non valido!");
                                break;
                        }
                    }
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
