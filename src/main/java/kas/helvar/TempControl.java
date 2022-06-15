package kas.helvar;

import kas.excel.ExcelParser;
import org.apache.log4j.Logger;

import static kas.helvar.HelvarReceivedObjectList.HELVAR_RECEIVED_OBJECT_LIST;

public class TempControl implements Runnable{
    private final Logger logger;

    public TempControl() {
        this.logger = Logger.getLogger(ExcelParser.class);
    }

    @Override
    public void run() {

        try {
            HELVAR_RECEIVED_OBJECT_LIST.sizeChanged();
        } catch (Exception ignore) {}

    }
}
