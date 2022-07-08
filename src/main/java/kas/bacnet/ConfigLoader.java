package kas.bacnet;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigLoader {
    private String fileName;

    public ConfigLoader() {
        this.fileName = "bacnet.config";
    }

    public ConfigLoader (String fileName) {
        this.fileName = fileName;
    }

    public Properties getConfig() {
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream(fileName)) {
            properties.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }



}
