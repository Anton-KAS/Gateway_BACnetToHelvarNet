package kas.helvar;

import kas.excel.ExcelParser;
import org.apache.log4j.Logger;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;

public class Client {
    private final Logger logger;

    private final String host;
    private final int port;

    public Client(String host, int port) {
        this.logger = Logger.getLogger(ExcelParser.class);

        this.host = host;
        this.port = port;
    }

    public void sendValue(String message) {
        //System.out.println("Helvar - try o send: " + message);
        try (Socket clientSocket = new Socket(host, port);
                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream())) {
            out.writeBytes(message);
            clientSocket.close();


                 //PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            //out.println((Arrays.toString(message.getBytes(StandardCharsets.UTF_8))));
            //out.write(message);

            //System.out.printf("To Helvar.net %s, message: %s%n", host, message); // TODO: DEL after test

        } catch (ConnectException ce){
            logger.error("Helvar sendValue NO connection " + host + ":" + port);
        } catch (IOException e) {
            logger.error("Helvar sendValue " + e.toString());
        }
    }
}
