package kas.helvar;

import org.json.simple.JSONObject;

import java.io.IOException;

import static kas.helvar.HelvarPointsMap.HELVAR_POINTS_MAP;

public class HelvarServicesStarter implements Runnable {
    private final JSONObject jsonData;
    private Listener[] listeners;
    private CyrcleJobReadPool[] cyrcleJobReadPools;

    public HelvarServicesStarter(JSONObject jsonData) {
        this.jsonData = jsonData;
    }

    public boolean startHelvarServices() throws IOException {
        if (jsonData == null) return false;
        HELVAR_POINTS_MAP.addPointsFromJson(jsonData);
        ListenerCyrcle listenerCyrcle = new ListenerCyrcle(jsonData);
        (new Thread(listenerCyrcle)).start();
        ;

        startCyrcleJobs();

        return true;
    }

    public boolean startCyrcleJobs() {
        if (cyrcleJobReadPools == null) {
            System.out.println("cyrcleJobReadPools == null");
            cyrcleJobReadPools = new CyrcleJobReadPool[jsonData.size()];
        }
        int n = 0;
        for (Object o : jsonData.keySet()) {
            System.out.println("startCyrcleJobs loop by jsonData: " + n + " | " + o.toString());
            String key = (String) o;
            JSONObject controller = (JSONObject) jsonData.get(key);

            String host = (String) controller.get("IP_CONTROLLER");
            cyrcleJobReadPools[n] = new CyrcleJobReadPool(host, null);
            (new Thread(cyrcleJobReadPools[n])).start();
            n++;
        }
        return true;
    }

    @Override
    public void run() {
        try {
            HELVAR_POINTS_MAP.addPointsFromJson(jsonData);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ListenerCyrcle listenerCyrcle = null;
        listenerCyrcle = new ListenerCyrcle(jsonData);

        //listenerCyrcle.run();
        //(new Thread(listenerCyrcle)).start();
        startCyrcleJobs();
    }
}
