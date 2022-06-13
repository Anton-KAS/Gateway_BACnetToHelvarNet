package kas.helvar;

import static kas.helvar.HelvarReceivedObjectList.HELVAR_RECEIVED_OBJECT_LIST;

public class TempProcessing implements Runnable{
    private final String host;
    private boolean canRun;

    public TempProcessing(String host) {
        this.host = host;
    }

    public void stop() {
        canRun = false;
    }

    @Override
    public void run() {
        canRun = true;
        //while (canRun) {
        HELVAR_RECEIVED_OBJECT_LIST.processing(host);
        //}
    }
}
