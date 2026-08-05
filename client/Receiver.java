package client;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

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
                synchronized (Client.consoleLock) {

                    if (!response.equals("OK")) {
                        System.out.println(response);
                    }
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
