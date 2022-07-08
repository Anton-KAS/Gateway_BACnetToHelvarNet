package kas.bacnet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConfigLoaderTest {
    ConfigLoader configLoader;

    @BeforeEach
    public void beforeEach() {
        String configFilePath = "./src/test/resources/kas/bacnet/bacnetTest.config";
        configLoader = new ConfigLoader(configFilePath);
    }

    @Test
    public void PropertiesTest() {
        //arrange
        int expectedDeviceId = 777;
        int expectedVendorId = 13;
        String expectedVendorName = "KAS";
        String expectedLocation = "BMS server";
        String expectedIpBroadcast = "192.168.1.255";
        String expectedIpSubnet = "192.168.1.0";
        String expectedIpLocal = "192.168.1.6";
        int expectedNetworkLength = 24;
        int expectedNetworkPort = 47808;


        //act
        Properties config = configLoader.getConfig();
        int resultDeviceId = Integer.parseInt(config.getProperty("device.id"));
        int resultVendorId = Integer.parseInt(config.getProperty("vendor.id"));
        String resultVendorName = config.getProperty("vendor.name");
        String resultLocation = config.getProperty("location");
        String resultIpBroadcast = config.getProperty("ip.broadcast");
        String resultIpSubnet = config.getProperty("ip.subnet");
        String resultIpLocal = config.getProperty("ip.local");
        int resultNetworkLength = Integer.parseInt(config.getProperty("network.length"));
        int resultNetworkPort = Integer.parseInt(config.getProperty("network.port"));

        //assert
        assertEquals(expectedDeviceId, resultDeviceId);
        assertEquals(expectedVendorId, resultVendorId);
        assertEquals(expectedVendorName, resultVendorName);
        assertEquals(expectedLocation, resultLocation);
        assertEquals(expectedIpBroadcast, resultIpBroadcast);
        assertEquals(expectedIpSubnet, resultIpSubnet);
        assertEquals(expectedIpLocal, resultIpLocal);
        assertEquals(expectedNetworkLength, resultNetworkLength);
        assertEquals(expectedNetworkPort, resultNetworkPort);
    }
}
