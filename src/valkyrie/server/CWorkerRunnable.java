package valkyrie.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.data.Day;
import server.data.EmployeeTimesheet;
import valkyrie.server.local.data.DataExecutor;
import valkyrie.server.local.data.DataHolder;
import valkyrie.server.local.data.config.ServerConfig;
import valkyrie.server.notification.DeviceNotificationHandler;

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

// Handle the connection between client and server
public class CWorkerRunnable implements Runnable{
    private static final Logger logger = LogManager.getLogger(CWorkerRunnable.class.getName());
    protected Socket clientSocket = null;

    //private static final int NEW_EMPLOYEE_DEVICE = 0;
    //private static final int NEW_SCHEDULER_DEVICE = 4;
    //private static final int SCHEDULER = 1;
    //private static final int EMPLOYEE = 2;

    private static final int CONNECTION_TEST = 0;
    private static final int REGISTER_EMPLOYEE_DEVICE   = 103;
    private static final int PUNCH_IN                   = 104;
    private static final int PUNCH_OUT                  = 105;
    private static final int NEW_JOB                    = 106;
    private static final int REQUEST_SCHEDULE           = 107;
    private static final int REQUEST_TIMESHEET          = 108;
    private static final int REQUEST_EMPLOYEES          = 109;
    private static final int UNSAFE_WORK_DAY            = 111;

    private static final int REGISTER_SCHEDULER_DEVICE  = 102;
    private static final int UPDATE_SCHEDULE            = 201;
    private static final int ADD_EMPLOYEE               = 202;


    private static final String TIME_FORMAT = "h:m.ss";

    public CWorkerRunnable(Socket clientSocket){
        this.clientSocket = clientSocket;
    }

    private static void logi(String log) {
        logger.info(log);
    }

    @Override
    public void run() {
        ObjectOutputStream output = null;
        ObjectInputStream input = null;

        try {
            output = new ObjectOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));
            output.flush();
            input = new ObjectInputStream(new BufferedInputStream(clientSocket.getInputStream()));
            long time = System.currentTimeMillis();
            final String clientIP = clientSocket.getInetAddress().getHostAddress();
            logi("\nConnected to: " + clientIP);

            // Client sends request type ID so it can be handled accordingly
            int request =  input.readInt();
            switch (request) {
                case CONNECTION_TEST:
                    logger.info("Request: CONNECTION_TEST");
                    break;
                case REQUEST_TIMESHEET:
                    logger.info("Request: REQUEST_TIMESHEET");
                    try {
                        UUID uuid = (UUID) input.readObject();
                        checkUUID(uuid, clientIP);
                        logi("Sending timesheet...");
                        //System.out.println("\n" + DataHolder.getInstance().getEmployeeTimesheetMap().get(uuid) + "\n");
                        output.writeObject(DataHolder.getInstance().getEmployeeTimesheetMap().get(uuid));
                        output.flush();
                        logi("Timesheet sent.");
                    } catch (ClassNotFoundException e) {
                        logger.error(e);
                        e.printStackTrace();
                    }
                    break;
                case PUNCH_IN:
                    logger.info("Request: PUNCH_IN");
                    try {
                        UUID uuid = (UUID) input.readObject();
                        Date punchInTime = (Date) input.readObject();
                        DataExecutor.getInstance().execute(new Runnable() {
                            @Override
                            public void run() {
                                checkUUID(uuid, clientIP);
                                DataHolder.getInstance().setEmployeeTimesheetPunchIn(uuid, punchInTime);
                                if (DataHolder.getInstance().getEmployeeTimesheetMap().get(uuid).getPunchOutTime() != null) {
                                    DataHolder.getInstance().setEmployeeTimesheetPunchOut(uuid, null);
                                }
                            }
                        });
                        EmployeeTimesheet employeeTimesheet = DataHolder.getInstance().getEmployeeTimesheetMap().get(uuid);
                        logi(employeeTimesheet.getFirstName() + " " + employeeTimesheet.getLastName() + " punched in.");
                    } catch (ClassNotFoundException e) {
                        logger.error(e);
                        e.printStackTrace();
                    }
                    break;
                case NEW_JOB:
                    logger.info("Request: NEW_JOB");
                    try {
                        UUID uuid = (UUID) input.readObject();
                        logger.debug(uuid);
                        int jobNumber = input.readInt();
                        long jobStartTime = input.readLong();

                        DataExecutor.getInstance().execute(new Runnable() {
                            @Override
                            public void run() {
                                checkUUID(uuid, clientIP);
                                DataHolder.getInstance().addJobToEmployeeTimesheet(uuid, jobNumber, new Date(jobStartTime));
                            }
                        });
                        EmployeeTimesheet employeeTimesheet = DataHolder.getInstance().getEmployeeTimesheetMap().get(uuid);
                        logi(employeeTimesheet.getFirstName() + " " + employeeTimesheet.getLastName() + " started job: " + jobNumber);
                    } catch (ClassNotFoundException e) {
                        logger.error(e);
                        e.printStackTrace();
                    }
                    break;
                case PUNCH_OUT:
                    logger.info("Request: PUNCH_OUT");
                    try {
                        UUID uuid = (UUID) input.readObject();
                        Date punchOutTime = (Date) input.readObject();
                        DataExecutor.getInstance().execute(new Runnable() {
                            @Override
                            public void run() {
                                checkUUID(uuid, clientIP);
                                DataHolder.getInstance().setEmployeeTimesheetPunchOut(uuid, punchOutTime);
                            }
                        });
                        EmployeeTimesheet employeeTimesheet = DataHolder.getInstance().getEmployeeTimesheetMap().get(uuid);
                        logi(employeeTimesheet.getFirstName() + " " + employeeTimesheet.getLastName() + " punched out.");
                    } catch (ClassNotFoundException e) {
                        logger.error(e);
                        e.printStackTrace();
                    }
                    break;
                case REQUEST_SCHEDULE:
                    logger.info("Request: REQUEST_SCHEDULE");
                    logi("Sending schedule...");
                    output.writeObject(DataHolder.getInstance().getSchedule());
                    output.flush();
                    logi("Schedule sent.");
                    break;
                case REGISTER_EMPLOYEE_DEVICE:
                    logger.info("Request: REGISTER_EMPLOYEE_DEVICE");
                    try {
                        UUID uuid = (UUID) input.readObject();
                        int devicePort = input.readInt();
                        DataExecutor.getInstance().execute(new Runnable() {
                            @Override
                            public void run() {
                                checkUUID(uuid, clientIP);
                                DataHolder.getInstance().registerEmployeeDevice(uuid, clientIP, devicePort);
                            }
                        });
                        logi("Employee device " + clientIP + " registered.");
                    } catch (ClassNotFoundException e) {
                        logger.error(e);
                        e.printStackTrace();
                    }
                    break;
                case REQUEST_EMPLOYEES:
                    logger.info("Request: REQUEST_EMPLOYEES");
                    logi("Sending employees...");
                    output.writeObject(DataHolder.getInstance().getEmployees());
                    output.flush();
                    logi("Employees sent.");
                    break;
                case UPDATE_SCHEDULE:
                    logger.info("Request: UPDATE_SCHEDULE");
                    try {
                        logi("Updating Schedule...");
                        ArrayList<Day> updatedSchedule = (ArrayList<Day>) input.readObject();

                        DataExecutor.getInstance().execute(new Runnable() {
                            @Override
                            public void run() {
                                DataHolder.getInstance().updateSchedule(updatedSchedule);
                                new DeviceNotificationHandler().notifyScheduleUpdated();
                                DataHolder.getInstance().printSchedule();
                            }
                        });
                        logi("Schedule Updated.");
                    } catch (ClassNotFoundException e) {
                        logger.error(e);
                        e.printStackTrace();
                    }
                    break;
                case ADD_EMPLOYEE:
                    logger.info("Request: ADD_EMPLOYEE");
                    String firstName = input.readUTF();
                    String lastName = input.readUTF();
                    DataExecutor.getInstance().execute(new Runnable() {
                        @Override
                        public void run() {
                            DataHolder.getInstance().addEmployee(firstName, lastName);
                        }
                    });
                    logi("Employee " + firstName + " " + lastName + " added.");
                    break;
                case REGISTER_SCHEDULER_DEVICE:
                    logger.info("Request: REGISTER_SCHEDULER_DEVICE");
                    int devicePort = input.readInt();
                    String deviceIP = clientSocket.getInetAddress().getHostAddress();
                    DataExecutor.getInstance().execute(new Runnable() {
                        @Override
                        public void run() {
                            DataHolder.getInstance().registerSchedulerDevice(deviceIP, devicePort);
                        }
                    });
                    logi("Scheduler device " + deviceIP + ":" + devicePort + " registered.");
                    break;
                case UNSAFE_WORK_DAY:
                    logger.info("Request: UNSAFE_WORK_DAY");
                    try {
                        final UUID uuid = (UUID) input.readObject();
                        DataExecutor.getInstance().execute(() -> {
                            checkUUID(uuid, clientIP);
                            EmployeeTimesheet timesheet = DataHolder.getInstance().getEmployeeTimesheetMap().get(uuid);
                            MGmail.sendUnsafeWorkdayEmail(timesheet.getFirstName() + timesheet.getLastName(), new SimpleDateFormat("M/d/yy").format(new Date()));
                        });
                    } catch (ClassNotFoundException e) {
                        logger.error(e);
                        e.printStackTrace();
                    }
                    break;

            }
            input.close();
            output.close();
            logi("Transfer complete " + time + "\nElapsed time: " + (System.currentTimeMillis() - time));
        } catch (IOException e) {
            logger.error(e);
            e.printStackTrace();
        } finally {
            if (clientSocket != null){
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    logger.error("Error closing client socket.", e);
                    e.printStackTrace();
                }
            }
        }
    }

    private void checkUUID(UUID uuid, String clientIP) {
        if (DataHolder.getInstance().getEmployeeTimesheetMap().get(uuid) != null) return;
        DataHolder.getInstance().addUnregisteredEmployee(uuid);
        new Thread(new RequestEmployeeInfoRunnable(clientIP, ServerConfig.getInstance().getDefaultDevicePort())).start();

    }
}
