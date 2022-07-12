package kas.helvar;

import org.apache.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static kas.helvar.ValuesToBacnet.VALUES_TO_BACNET;

public class HelvarControllerListener implements Runnable {
    private final Logger logger;
    private final String host;
    private final int port;
    private final Socket socket;
    private final int SOCKET_TIMEOUT;
    private BufferedReader fromRouter;
    private DataOutputStream toRouter;
    private final LinkedList<String[]> sendMessageList;
    private final int MAX_SEND_MESSAGE_LENGTH;
    private volatile boolean running;
    private final int controllerReg;

    public HelvarControllerListener(String host, int port, final Socket socket, int controllerReg) {
        this.logger = Logger.getLogger(HelvarControllerListener.class);
        this.host = host;
        this.port = port;
        this.socket = socket;
        this.SOCKET_TIMEOUT = 250;
        this.sendMessageList = new LinkedList<>();
        this.MAX_SEND_MESSAGE_LENGTH = 300;
        this.running = false;
        this.controllerReg = controllerReg;
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
        } catch (SocketTimeoutException e) {
            //logger.error(e);
        }
        String resp = sb.toString();
        if (resp.length() > 0) {
            logger.info(String.format("Listener RECEIVED\tfrom Helvar.net %s:%s : value <--- %s", host, port, resp));
            HelvarReceivedObjectList.HELVAR_RECEIVED_OBJECT_LIST.addValueToTheEnd(host, sb.toString());
        }
    }

    private synchronized void send() throws IOException {
        String[] toSendPoint = sendMessageList.pollFirst();
        assert toSendPoint != null;
        String type = toSendPoint[0];
        String toSend = toSendPoint[1];
        if (type.equals("repeating")) {
            setCycleSendMessage(toSend);
        }
        if (toSend != null) {
            logger.info(String.format("Listener     SEND\tto Helvar.net %s:%s : value ---> %s", host, port, toSend));
            byte[] dataInBytes = toSend.getBytes(StandardCharsets.UTF_8);
            toRouter.writeInt(toSend.length());
            toRouter.write(dataInBytes, 0, toSend.length());
            toRouter.flush();
            HelvarReceivedObjectList.HELVAR_RECEIVED_OBJECT_LIST.addValueToTheEnd(host, toSend);
        }
    }

    public synchronized void setCycleSendMessage(String message) {
        /*
        if (sendMessageList.size() > MAX_SEND_MESSAGE_LENGTH) {
            return;
        }
         */
        String[] toSendPoint = {"repeating", message};
        sendMessageList.addLast(toSendPoint);
    }

    public synchronized void setBacnetSendMessage(String message) {
        String[] toSendPoint = {"oneTime", message};
        sendMessageList.addFirst(toSendPoint);
    }

    private void setStatusToBacnet(boolean running) {
        float floatValue;
        if (running) {
            floatValue = 1f;
        } else {
            floatValue = 0f;
        }
        this.running = running;
        VALUES_TO_BACNET.setValue("bi", controllerReg, floatValue);
    }

    @Override
    public void run() {
        try {
            while (true) {
                if (!running) {
                    logger.info("Helvar Listener " + host + ":" + port + " - running");
                    fromRouter = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8), 1600);
                    toRouter = new DataOutputStream(socket.getOutputStream());
                    socket.setSoTimeout(SOCKET_TIMEOUT);
                    setStatusToBacnet(true);
                }
                try {
                    send();
                    listen();
                } catch (Exception e) {
                    setStatusToBacnet(false);
                    logger.info("Helvar Listener " + host + ":" + port + " - stopped");
                    logger.error("run() " + e.getMessage());
                    logger.error("run() " + Arrays.toString(e.getStackTrace()));
                    Thread.sleep(5000);
                }
            }
        } catch (IOException | InterruptedException e) {
            setStatusToBacnet(false);
            logger.info("Helvar Listener " + host + ":" + port + " - stopped");
            logger.error("Listener run() - " + e);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || !obj.getClass().equals(HelvarControllerListener.class)) return false;

        HelvarControllerListener altObject = (HelvarControllerListener) obj;

        return Objects.equals(host, altObject.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host);
    }
}
