package valkyrie.server.notification;

import server.data.DeviceInfo;
import server.data.Employee;
import server.data.EmployeeTimesheet;
import valkyrie.server.local.data.DataHolder;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Send notification to all employee Devices
public class DeviceNotificationHandler {

    private static ExecutorService executor;

    public DeviceNotificationHandler() {
        if(executor == null){
            executor = Executors.newFixedThreadPool(10);
        }
    }

    public void notifyScheduleUpdated(){
        DataHolder holder = DataHolder.getInstance();
        if(holder.getEmployeeDevices().size() != 0){
            for(DeviceInfo di : holder.getEmployeeDevices()){
                if(di != null){
                    executor.execute(new NotifyScheduleUpdateRunnable(di.getDeviceIP(), di.getDevicePort()));
                }
            }
        } else {
            log("NotifyScheduleUpdated: No employee devices registered.");
        }
    }

    public void sendServerInfo(){
        DataHolder holder = DataHolder.getInstance();
        if(holder.getAllDevices().size() > 0){
            for(DeviceInfo di : holder.getAllDevices()){
                executor.execute(new NotifyServerInfoRunnable(di.getDeviceIP(), di.getDevicePort()));
            }
        } else{
            log("SendServerInfo: No devices registered.");
        }
    }

    public void notifyLateEmployees(){
        log("Notifying of late employees...");
        ArrayList<Employee> lateEmployees = new ArrayList<Employee>();
        DataHolder holder = DataHolder.getInstance();
        if(holder.getEmployeeTimesheetMap().values().size() > 0){
            for (EmployeeTimesheet e : holder.getEmployeeTimesheetMap().values()){
                Date punchInTime = e.getPunchInTime();
                if(punchInTime == null || punchInTime.after(holder.getCurrentDateStartTime())){
                    lateEmployees.add(e);
                }
            }
        }
        log("Number of late employees: " + lateEmployees.size());
        if(holder.getSchedulerDevices().values().size() > 0){
            for(DeviceInfo d : holder.getSchedulerDevices().values()){
                executor.execute(new NotifyLateEmployeeRunnable(d.getDeviceIP(), d.getDevicePort(), lateEmployees));
            }
        }
    }

    private static void log(String log){
        System.out.println(log);
    }

}
