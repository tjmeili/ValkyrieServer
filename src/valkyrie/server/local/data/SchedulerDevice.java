package valkyrie.server.local.data;

import server.data.DeviceInfo;

public class SchedulerDevice {
    private DeviceInfo deviceInfo = null;
    private boolean recieveSafeWorkDayNotificationEnabled = false;
    private String tag = "";

    public SchedulerDevice() {
    }

    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public boolean isRecieveSafeWorkDayNotificationEnabled() {
        return recieveSafeWorkDayNotificationEnabled;
    }

    public void setRecieveSafeWorkDayNotificationEnabled(boolean recieveSafeWorkDayNotificationEnabled) {
        this.recieveSafeWorkDayNotificationEnabled = recieveSafeWorkDayNotificationEnabled;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
