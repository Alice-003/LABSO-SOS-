package client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import aggregator.protocol.Aggregator_Protocol;
import client.protocol.Protocol;

public class Receiver implements Runnable {

    Socket s;
    Thread sender;

    public Receiver(Socket s, Thread sender) {
        this.s = s;
        this.sender = sender;
    }

    @Override
    public void run() {

        try {
            Scanner from = new Scanner(this.s.getInputStream());
            Boolean stampa = false;
            // Creiamo il PrintWriter qui, usando la stessa socket del Receiver
            // per mantenere aperta la comunicazione con l'aggregatore

            while (from.hasNextLine()) {
                String response = from.nextLine();
                String[] strSPLIT = response.split(Protocol.SEPARATORE);

                if (response.equals(Aggregator_Protocol.DATA)) {
                    stampa = true;

                } else if (response.equals(Aggregator_Protocol.END)) {
                    stampa = false;
                }
                if (stampa == true) {
                    System.out.println(response);
                }
                if (strSPLIT[0].equals(Protocol.comandoDOWNLOAD)) {

                    PrintWriter toAggregator = new PrintWriter(s.getOutputStream(), true);
                    String ip = strSPLIT[2];
                    int porta = Integer.parseInt(strSPLIT[3]);
                    String nomeRilevazione = strSPLIT[4];
                    String nomePeer = strSPLIT[1];

                    UploadManager up = new UploadManager(ip, porta, nomeRilevazione, nomePeer, toAggregator);
                    Thread upThread = new Thread(up);
                    upThread.setDaemon(true);
                    upThread.start();
                }
            }

            from.close();
        } catch (IOException e) {
            synchronized (Client.consoleLock) {
                System.err.println("IOException caught: " + e);
                e.printStackTrace();
            }

        } finally {
            synchronized (Client.consoleLock) {
                this.sender.interrupt();
                System.out.println("Receiver closed.");
            }

        }
    }
}
