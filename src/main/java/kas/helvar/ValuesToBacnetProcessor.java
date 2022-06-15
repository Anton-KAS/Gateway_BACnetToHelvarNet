package kas.helvar;

import kas.excel.ExcelParser;
import org.apache.log4j.Logger;

import static kas.helvar.HelvarReceivedObjectList.HELVAR_RECEIVED_OBJECT_LIST;

public class ValuesToBacnetProcessor implements Runnable{
    private final Logger logger;

    private final String host;

    public ValuesToBacnetProcessor(String host) {
        this.logger = Logger.getLogger(ExcelParser.class);

        this.host = host;
    }

    @Override
    public void run() {
        try {
            HELVAR_RECEIVED_OBJECT_LIST.processing(host);
        } catch (Exception e) {
            this.logger.error("ValuesToBacnetProcessor: " + e);
        }
    }
}
