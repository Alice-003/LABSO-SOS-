package client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

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

            while (from.hasNextLine()) {
                String response = from.nextLine();
                String[] strSPLIT = response.split(Protocol.SEPARATORE);
                System.out.println("cccc: " + response);
                if (strSPLIT[0].equals(Protocol.comandoDOWNLOAD)) {
                    System.out.println(response);
                    System.out.println(Sender.nomeRilevazioneDownload);
                    PrintWriter toAggregator = new PrintWriter(s.getOutputStream(), true);
                    UploadManager up = new UploadManager(strSPLIT[3], Integer.parseInt(strSPLIT[4]), strSPLIT[1],
                            toAggregator, strSPLIT[2]);
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
