package kas.helvar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.Socket;

public class Listener implements Runnable {
    private final String host;
    private final int port;
    private boolean running;

    public Listener(String host, int port) {
        this.host = host;
        this.port = port;
        this.running = false;
    }

    public boolean getRunning() {
        return running;
    }

    @Override
    public void run() {
        try (Socket clientSSocket = new Socket(host, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(clientSSocket.getInputStream()))) {
            running = true;
            System.out.println("Helvar Listener " + host + "running");
            HelvarReceivedObjectList.HELVAR_RECEIVED_OBJECT_LIST.addValueInTheEnd(host, in);
            String resp = String.format("from Helvar.net %s: value: %s", host, in.readLine()); // TODO: DEL after test
            System.out.println(resp); // TODO: DEL after test
        } catch (ConnectException ce) {
            running = false;
            System.out.println("Helvar Listener NO connection " + host + ":" + port);
        } catch (IOException e) {
            running = false;
            System.out.println("Helvar Listener " + host + " stopped");
            e.printStackTrace();
        }
    }
}
