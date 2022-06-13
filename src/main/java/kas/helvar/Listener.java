package kas.helvar;

import kas.excel.ExcelParser;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Objects;

public class Listener implements Runnable {
    private final Logger logger;
    private final String host;
    private final int port;
    private boolean running;
    private final Socket socket;
    private final int SOCKET_TIMEOUT;
    private BufferedReader fromRouter;
    private DataOutputStream toRouter;
    private final LinkedList<String> sendMessage;
    private final int MAX_SEND_MESSAGE_LENGTH;

    public Listener(String host, int port, final Socket socket) {
        this.logger = Logger.getLogger(ExcelParser.class);
        this.host = host;
        this.port = port;
        this.running = false;
        this.socket = socket;
        this.SOCKET_TIMEOUT = 500;
        this.sendMessage = new LinkedList<>();
        this.MAX_SEND_MESSAGE_LENGTH = 300;
    }

    public boolean getRunning() {
        return running;
    }

    private void listen() throws IOException {
        StringBuilder sb = new StringBuilder();
        try {
            int value;

            while ((value = fromRouter.read()) != -1) {
                sb.append((char) value);
                char charValue = (char) value;
                String stringValue = String.valueOf(charValue);
                if ((stringValue).equals("#") & sb.length() > 300) break;
            }
        } catch (SocketTimeoutException ignored) {
        }
        String resp = sb.toString();
        if (resp.length() > 0) {
            logger.info(String.format("Listener RECEIVED from Helvar.net %s:%s : value <--- %s", host, port, resp));
            HelvarReceivedObjectList.HELVAR_RECEIVED_OBJECT_LIST.addValueInTheEnd(host, sb.toString());
        }
    }

    private void send() throws IOException {
        String toSend = sendMessage.pollFirst();
        if (toSend != null) {
            logger.info(String.format("Listener SEND to Helvar.net %s:%s : value ---> %s", host, port, toSend));
            byte[] dataInBytes = toSend.getBytes(StandardCharsets.UTF_8);
            toRouter.writeInt(toSend.length());
            toRouter.write(dataInBytes, 0, toSend.length());
            toRouter.flush();
        }
    }

    public void setCycleSendMessage(String message) {
        if (sendMessage.size() > MAX_SEND_MESSAGE_LENGTH) {
            return;
        }
        sendMessage.addLast(message);
    }

    public void setBacnetSendMessage(String message) {
        sendMessage.addFirst(message);
    }

    @Override
    public void run() {
        running = true;
        try {
            logger.info("Helvar Listener " + host + ":" + port + " - running");

            fromRouter = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8), 1600);
            toRouter = new DataOutputStream(socket.getOutputStream());
            socket.setSoTimeout(SOCKET_TIMEOUT);

            while (running) {
                send();
                listen();
            }
        } catch (IOException e) {
            running = false;
            logger.error("Listener run() - " + e.toString());
        }
    }

    public void stop() {
        running = false;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || !obj.getClass().equals(Listener.class)) return false;

        Listener altObject = (Listener) obj;

        return Objects.equals(host, altObject.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host);
    }
}
