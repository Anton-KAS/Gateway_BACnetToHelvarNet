package kas.helvar;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

/**
 * Creates thread for receiving messages from a given host
 * Initialize ArrayBlockingQueue to store strings and to manage the access to these strings
 */
public class MessageReceiver implements Runnable {
    private final static int QUEUE_MAX_STORAGE = 1024;

    private final Socket listeningConnection;
    private final BlockingQueue<String> queue = new ArrayBlockingQueue<String>(QUEUE_MAX_STORAGE);

    /**
     * Initializes objects final variable listeningConnection
     *
     * @param listeningConnection socket value
     */
    public MessageReceiver(final Socket listeningConnection) {
        this.listeningConnection = listeningConnection;
    }

    /**
     * Gets string from the ArrayBlockingQueue
     *
     * @return string message from the queue
     * @throws InterruptedException
     */
    public String getNextReceivedMessage() throws InterruptedException {
        return queue.take();
    }

    public void run() {
        try {
            System.out.println("Start listen");
            final BufferedReader fromRouter = new BufferedReader(new InputStreamReader(listeningConnection.getInputStream(), StandardCharsets.UTF_8), 1600);

            while (true) {
                int value = 0;
                StringBuilder sb = new StringBuilder();
                while ((value = fromRouter.read()) != -1) {
                    System.out.print((char) value);
                    sb.append((char) value);
                    char charValue = (char) value;
                    String stringValue = String.valueOf(charValue);

                    if ((stringValue).equals("#")) break;

                }
                System.out.println("\n------------");
                System.out.println(sb);
                System.out.println("------------\n");
            }

//            int value = 0;
//            while((value = fromRouter.read()) != -1)
//            {
//               char c = (char)value;
//
//               System.out.println(c);
//            }
            //while (true) {
//            String modifiedSentence;
//
//            while ((modifiedSentence = fromRouter.readLine()) != null) {
//                fromRouter.lines().collect(Collectors.joining(StringUtils.LF));
//                //fromRouter.lines().collect(Collectors.joining(String))
//                System.out.println(modifiedSentence);
//                //queue.add(modifiedSentence);
//            }
            //}
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


//    public static void main(String[] args){
//        Socket socket;
//        try {
//            socket = new Socket("10.254.1.1", 50000);
//            MessageReceiver messageReceiver = new MessageReceiver(socket);
//            messageReceiver.start();
//        } catch (UnknownHostException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//    }
}