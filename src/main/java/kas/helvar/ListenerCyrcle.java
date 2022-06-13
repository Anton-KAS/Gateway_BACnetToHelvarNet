package kas.helvar;

import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class ListenerCyrcle implements Runnable {
    private Listener[] listeners;
    private Thread[] threads;
    private boolean running;

    public ListenerCyrcle(JSONObject jsonData) {
        this.running = false;
        this.listeners = new Listener[jsonData.size()];
        this.threads = new Thread[jsonData.size()];
        int n = 0;
        for (Object o : jsonData.keySet()) {
            String key = (String) o;
            JSONObject controller = (JSONObject) jsonData.get(key);

            String host = (String) controller.get("IP_CONTROLLER");
            int port = (int) controller.get("PORT_CONTROLLER");

            try {
                Socket socket = new Socket(host, port);
                System.out.println("new Socket " + host + ":" + port);
                Listener listener = new Listener(host, port, socket);
                System.out.println("new listener " + host + ":" + port);
                //messageReceiver.start();
                (new Thread(listener)).start();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            //listeners[n] = new Listener(host, port, socket);
            //threads[n] = new Thread(listeners[n]);
            (new Thread(listeners[n])).start();
            n++;
        }
    }

    @Override
    public void run() {
        try {
            System.out.println("ListenerCyrcle running");
            running = true;
            for (Thread thread : threads) {
                System.out.println("THREAD isAlive: " + thread.isAlive());
                System.out.println("THREAD getState: " + thread.getState());
                if (!thread.isAlive()) {
                    try {
                        thread.start();
                    } catch (Exception e) {
                        System.out.println("THREAD Can NOT start: " + e);
                        continue;
                    }
                }
            }
            Thread.sleep(30_000);
        } catch (Exception e) {
            System.out.println("ListenerCyrcle stopped");
            running = false;
        }
    }
}

