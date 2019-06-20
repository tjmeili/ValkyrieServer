package valkyrie.server;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.data.DeviceInfo;
import server.data.Employee;
import server.data.EmployeeTimesheet;
import valkyrie.server.local.data.DataHolder;
import valkyrie.server.logging.messages.ClientSocketMessage;
import valkyrie.server.notification.NotifyLateEmployeeRunnable;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ValkyrieServer implements Runnable{
    private static final Logger logger = LogManager.getLogger(ValkyrieServer.class.getName());

    protected int          serverPort   = 9696;
    protected ServerSocket serverSocket = null;
    protected boolean      isStopped    = false;
    protected Thread       runningThread= null;


    //TODO: Reset Late Employee Notification when the current day schedule is updated.

    public ValkyrieServer(int port) {
        this.serverPort = port;
    }

    private static void logi(String s) {
        logger.info(s);
    }

    @Override
    public void run() {
        logger.traceEntry();
        startDayResetExecutor(LocalTime.now());
        synchronized (this){
            this.runningThread = Thread.currentThread();
        }
        try{
            this.serverSocket = new ServerSocket(this.serverPort);
            logi("Server started.");
            while(!isStopped()){
                Socket clientSocket = null;
                // Wait for a client connection
                try{
                    clientSocket = this.serverSocket.accept();
                } catch (IOException e){
                    if(isStopped()){
                        logi("Server stopped.");
                        return;
                    }
                    throw new RuntimeException(
                            "Error accepting client connection", e);
                }
                // Create new worker thread to handle connection
                logger.info(new ClientSocketMessage(clientSocket, System.currentTimeMillis()));
                new Thread(new CWorkerRunnable(clientSocket)).start();
            }
        }catch (IOException e){
            logger.error("Cannot open port " + this.serverPort, e);
            throw new RuntimeException("Cannot open port " + this.serverPort, e);
        } finally {
            if(!isStopped){
                stop();
            }
        }
        logi("Server stopped.");
        logger.traceExit();
    }

    private void startDayResetExecutor(LocalTime now){
        logger.traceEntry();
        long delay = ChronoUnit.MINUTES.between(LocalTime.of(1,0), now);
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                logger.traceEntry();
                //ExcelWriter.getInstance().generateAllExcels(new ArrayList<>(DataHolder.getInstance().getEmployeeTimesheetMap().values()));
                DataHolder.getInstance().saveEmployeesAndSchedulers();
                Calendar c = Calendar.getInstance();
                c.setTime(DataHolder.getInstance().getCurrentDateSchedule().getDate());
                if(c.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY){
                    logger.trace("Reset Week");
                    DataHolder.getInstance().resetWeek();
                } else {
                    logger.trace("Reset Day");
                    DataHolder.getInstance().resetDay();
                }
                scheduleLateEmployeeNotification();
                logger.traceExit();
            }
        }, delay, 24 *60, TimeUnit.MINUTES);
        logger.traceExit();
    }


    private synchronized boolean isStopped() {
        return this.isStopped;
    }

    // Check for late employees and send notification to administrator applications on the network.
    private void scheduleLateEmployeeNotification(){
        logger.traceEntry();
        if(DataHolder.getInstance().getCurrentDateSchedule().isActive()){
            ExecutorService executorService = Executors.newFixedThreadPool(5);
            ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

            long diff = DataHolder.getInstance().getCurrentDateStartTime().getTime() - Calendar.getInstance().getTime().getTime();
            diff = TimeUnit.MILLISECONDS.toMinutes(diff);
            scheduledExecutorService.schedule(new Runnable() {
                @Override
                public void run() {
                    logi("Notifying of late employees...");
                    ArrayList<Employee> lateEmployees = new ArrayList<Employee>();
                    DataHolder dataHolder = DataHolder.getInstance();

                    if(dataHolder.getEmployeeTimesheetMap().values().size() > 0){
                        for (EmployeeTimesheet e : dataHolder.getEmployeeTimesheetMap().values()){
                            Date punchInTime = e.getPunchInTime();
                            if(punchInTime == null || punchInTime.after(dataHolder.getCurrentDateStartTime())){
                                lateEmployees.add(e);
                            }
                        }
                    }

                    if(dataHolder.getSchedulerDevices().values().size() > 0){
                        for(DeviceInfo d : dataHolder.getSchedulerDevices().values()){
                            executorService.execute(new NotifyLateEmployeeRunnable(d.getDeviceIP(), d.getDevicePort(), lateEmployees));
                        }
                    }
                }
            }, diff, TimeUnit.MINUTES);
        }
        logger.traceExit();
    }

    public synchronized void stop(){
        logger.traceEntry();
        this.isStopped = true;
        try{
            this.serverSocket.close();
        }catch (IOException e){
            logger.error("Error closing server", e);
            throw new RuntimeException("Error closing server", e);
        }
        logger.traceExit();
    }

}
