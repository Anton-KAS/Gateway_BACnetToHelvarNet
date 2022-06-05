package kas.helvar;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;

public class Client {
    private final String host;
    private final int port;

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void sendValue(String message) {
        try (Socket clientSocket = new Socket(host, port);
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            out.println((message));
            System.out.printf("To Helvar.net %s, message: %s%n", host, message); // TODO: DEL after test

        } catch (ConnectException ce){
            System.out.println("Helvar sendValue NO connection " + host + ":" + port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
