package server.data;

import java.io.Serializable;

public class DeviceInfo implements Serializable{
    private String deviceIP;
    private int devicePort;

    public DeviceInfo(String deviceIP, int devicePort) {
        this.deviceIP = deviceIP;
        this.devicePort = devicePort;
    }

    public String getDeviceIP() {
        return deviceIP;
    }

    public void setDeviceIP(String deviceIP) {
        this.deviceIP = deviceIP;
    }

    public int getDevicePort() {
        return devicePort;
    }

    public void setDevicePort(int devicePort) {
        this.devicePort = devicePort;
    }
}
