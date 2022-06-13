package kas.helvar;

import kas.excel.ExcelParser;
import org.apache.log4j.Logger;

import java.util.Map;

import static kas.helvar.HelvarPointsMap.HELVAR_POINTS_MAP;

public class CyrcleJobReadPool implements Runnable {
    private final Logger logger;

    private final String host;
    private Map<Integer, HelvarPoint> pointsMap;
    private boolean running;
    private final Listener listener;

    public CyrcleJobReadPool(String host, Listener listener) {
        this.logger = Logger.getLogger(ExcelParser.class);

        this.listener = listener;
        this.host = host;
        this.pointsMap = HELVAR_POINTS_MAP.getPointsMapByHost(host);
        logger.info("CyrcleJobReadPool HELVAR_POINTS_MAP pointsMap: " + pointsMap);
        this.running = false;
    }

    @Override
    public void run() {
        try {
            running = true;
            if (pointsMap == null) {
                logger.info("CyrcleJobReadPool pointsMap == null - " + host);
            }
            for (int n : pointsMap.keySet()) {
                HelvarPoint point = pointsMap.get(n);
                try {
                    listener.setCycleSendMessage(point.getReadSceneQuery());
                    listener.setCycleSendMessage(point.getReadConsumptionQuery());
                } catch (Exception e) {
                    logger.error("CyrcleJobReadPool run()" + e.toString());
                }
            }
        } catch (Exception e) {
            running = false;
            logger.error("CyrcleJobReadPool run(): " + e.toString());
        }
    }
}
