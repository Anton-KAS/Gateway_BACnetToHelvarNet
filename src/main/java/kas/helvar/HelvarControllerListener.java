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
    private Socket socket;
    private final int SOCKET_TIMEOUT;
    private final int SHORT_SOCKET_TIMEOUT;
    private BufferedReader fromRouter;
    private DataOutputStream toRouter;
    private final Deque<String> sendRepeatingMessageList;
    private final Deque<String> sendOneTimeMessageList;
    private final int MAX_SEND_MESSAGE_LENGTH;
    private volatile boolean running;
    private final int controllerReg;

    public HelvarControllerListener(String host, int port, int controllerReg) throws IOException {
        this.logger = Logger.getLogger(HelvarControllerListener.class);
        this.host = host;
        this.port = port;
        this.SOCKET_TIMEOUT = 2000;
        this.SHORT_SOCKET_TIMEOUT = this.SOCKET_TIMEOUT / 10;
        this.sendRepeatingMessageList = new LinkedList<>();
        this.sendOneTimeMessageList = new LinkedList<>();
        this.MAX_SEND_MESSAGE_LENGTH = 10;
        this.running = false;
        this.controllerReg = controllerReg;
    }

    private void listen() throws IOException {
        long startTime = System.nanoTime();
        StringBuilder sb = new StringBuilder();
        try {
            int value;
            socket.setSoTimeout(SOCKET_TIMEOUT);
            boolean setShortTimeout = false;
            while ((value = fromRouter.read()) != -1) {
                sb.append((char) value);
                char charValue = (char) value;
                String stringValue = String.valueOf(charValue);
                long duration = (System.nanoTime() - startTime) / 1000000;

                if (stringValue.equals("#") && !setShortTimeout) {
                    socket.setSoTimeout(SHORT_SOCKET_TIMEOUT);
                    setShortTimeout = true;
                }

                if ((stringValue).equals("#") && (sb.length() > MAX_SEND_MESSAGE_LENGTH || duration > SHORT_SOCKET_TIMEOUT))
                    break;
            }
        } catch (SocketTimeoutException ignore) {
        }
        String resp = sb.toString();
        if (resp.length() > 0) {
            logger.info(String.format("Listener RECEIVED\tfrom Helvar.net %s:%s : value <--- %s", host, port, resp));
            HelvarReceivedObjectList.HELVAR_RECEIVED_OBJECT_LIST.addValueToTheEnd(host, sb.toString());
        }
    }

    private void send(String messageToSend, boolean responseToBacnet) throws IOException {
        if (messageToSend != null) {
            logger.info(String.format("Listener     SEND\tto Helvar.net %s:%s : value ---> %s", host, port, messageToSend));
            byte[] dataInBytes = messageToSend.getBytes(StandardCharsets.UTF_8);
            toRouter.writeInt(messageToSend.length());
            toRouter.write(dataInBytes, 0, messageToSend.length());
            toRouter.flush();
        }
        if (responseToBacnet) {
            HelvarReceivedObjectList.HELVAR_RECEIVED_OBJECT_LIST.addValueToTheEnd(host, messageToSend);
        }
    }

    private String getFirstRepeatingMessage() {
        String message = sendRepeatingMessageList.pollFirst();
        setRepeatingMessage(message);
        return message;
    }

    public void setRepeatingMessage(String message) {
        sendRepeatingMessageList.addLast(message);
    }

    public void setBacnetSendMessage(String message) {
        sendOneTimeMessageList.addLast(message);
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

    @SuppressWarnings("BusyWait")
    @Override
    public void run() {
        try {
            //noinspection InfiniteLoopStatement
            while (true) {
                try {
                    if (!running) {
                        logger.info("Helvar Listener " + host + ":" + port + " - try to starting up");
                        this.socket = new Socket(host, port);
                        fromRouter = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8), 1600);
                        toRouter = new DataOutputStream(socket.getOutputStream());
                        socket.setSoTimeout(SOCKET_TIMEOUT);
                        setStatusToBacnet(true);
                        logger.info("Helvar Listener " + host + ":" + port + " - running");
                    }

                    while (sendOneTimeMessageList.iterator().hasNext()) {
                        send(sendOneTimeMessageList.pollFirst(), true);
                        Thread.sleep(10);
                    }
                    send(getFirstRepeatingMessage(), false);
                    listen();
                } catch (Exception e) {
                    setStatusToBacnet(false);
                    logger.info("Helvar Listener " + host + ":" + port + " - stopped");
                    logger.error("run() " + e.getMessage());
                    logger.error("run() " + Arrays.toString(e.getStackTrace()));
                    Thread.sleep(5000);
                }
            }
        } catch (InterruptedException e) {
            setStatusToBacnet(false);
            logger.info("Helvar Listener " + host + ":" + port + " - fatal stopped");
            logger.fatal("Listener run() - " + e);
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
