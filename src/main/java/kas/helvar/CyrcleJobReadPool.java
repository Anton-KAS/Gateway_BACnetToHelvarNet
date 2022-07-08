package kas.helvar;

import kas.excel.ExcelParser;
import org.apache.log4j.Logger;

import java.util.Map;

import static kas.helvar.HelvarPointsMap.HELVAR_POINTS_MAP;

public class CyrcleJobReadPool implements Runnable {
    private final Logger logger;

    private final String host;
    private final Map<Integer, HelvarPoint> pointsMap;
    private volatile HelvarControllerListener listener;

    public CyrcleJobReadPool(String host, HelvarControllerListener listener) {
        this.logger = Logger.getLogger(ExcelParser.class);

        this.listener = listener;
        this.host = host;
        this.pointsMap = HELVAR_POINTS_MAP.getPointsMapByHost(host);
        logger.info("CyrcleJobReadPool HELVAR_POINTS_MAP pointsMap: " + pointsMap);
    }

    @Override
    public void run() {
        try {
            if (pointsMap == null) {
                logger.info("CyrcleJobReadPool pointsMap == null - " + host);
            }
            assert pointsMap != null;
            for (int n : pointsMap.keySet()) {
                synchronized (this) {
                    HelvarPoint point = pointsMap.get(n);
                    try {
                        listener.setCycleSendMessage(point.getReadSceneQuery());
                        listener.setCycleSendMessage(point.getReadConsumptionQuery());
                    } catch (Exception e) {
                        logger.error("CyrcleJobReadPool run()" + e);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("CyrcleJobReadPool run(): " + e);
        }
    }
}
