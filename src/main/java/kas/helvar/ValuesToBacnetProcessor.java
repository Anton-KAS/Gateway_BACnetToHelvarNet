package kas.helvar;

import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.Objects;

import static kas.helvar.HelvarReceivedObjectList.HELVAR_RECEIVED_OBJECT_LIST;

public class ValuesToBacnetProcessor implements Runnable {
    private final Logger logger;

    private final String host;

    public ValuesToBacnetProcessor(String host) {
        this.logger = Logger.getLogger(ValuesToBacnetProcessor.class);

        this.host = host;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || !obj.getClass().equals(ValuesToBacnetProcessor.class)) return false;

        ValuesToBacnetProcessor altObject = (ValuesToBacnetProcessor) obj;

        return Objects.equals(host, altObject.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host);
    }

    @Override
    public void run() {
        //noinspection InfiniteLoopStatement
        while (true) {
            try {
                HELVAR_RECEIVED_OBJECT_LIST.processing(host);
            } catch (Exception e) {
                this.logger.error("ValuesToBacnetProcessor: " + e.getMessage());
                this.logger.error("ValuesToBacnetProcessor: " + Arrays.toString(e.getStackTrace()));
            }
        }
    }
}
