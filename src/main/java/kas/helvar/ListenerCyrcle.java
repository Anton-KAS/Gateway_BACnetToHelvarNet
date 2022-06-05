package kas.helvar;

import org.json.simple.JSONObject;

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

            listeners[n] = new Listener(host, port);
            threads[n] = new Thread(listeners[n]);
            n++;
        }
    }

    @Override
    public void run() {
        try {
            running = true;
            while (true) {
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

            }
        } catch (Exception e) {
            running = false;
        }
    }
}

