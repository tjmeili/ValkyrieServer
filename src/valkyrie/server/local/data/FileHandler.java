package valkyrie.server.local.data;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.data.Day;
import server.data.DeviceInfo;
import server.data.Employee;
import server.data.EmployeeTimesheet;
import valkyrie.server.local.data.config.ServerConfig;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Handles saving and loading server data.
// TODO: Convert to use SQL database or Google Firebase

public class FileHandler {
    private static final Logger logger = LogManager.getLogger(FileHandler.class.getName());
    private static final ExecutorService FILE_EXECUTOR = Executors.newSingleThreadExecutor();
    private static final String VALKYRIE_SUB_DIRECTORY = "\\Valkyrie\\data";
    private static String fileName = "data_backup.bin", scheduleFileName = "schedule_backup.bin", configFileName = "config.properties",
            path = "";


    public static void configureFileDirectory() {
        logger.traceEntry();
        String userHomeDir = System.getProperty("user.home");
        File dirFile = new File(userHomeDir + VALKYRIE_SUB_DIRECTORY);
        if (!dirFile.exists()) {
            if (!dirFile.mkdirs()) {
                logger.warn("Failed to make application directory: " + userHomeDir + VALKYRIE_SUB_DIRECTORY);
            }
        }
        path = userHomeDir + VALKYRIE_SUB_DIRECTORY;
        logger.traceExit(dirFile.exists());
    }

    public static void saveEmployeeAndSchedulerData(EmployeeAndSchedulerData data) {
        FILE_EXECUTOR.execute(() -> saveEmployeeData(data));
    }

    public static void saveSchedule(ArrayList<Day> schedule) {
        FILE_EXECUTOR.execute(() -> saveScheduleData(schedule));
    }

    public static void saveServerConfig() {
        FILE_EXECUTOR.execute(() -> saveConfigData());
    }

    private static void saveEmployeeData(EmployeeAndSchedulerData data) {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            logger.info("Saving employee and scheduler data... " + path);
            fos = new FileOutputStream(new File(path + "\\" + fileName), false);
            oos = new ObjectOutputStream(fos);

            oos.writeObject(data.schedulerDevices);
            oos.writeObject(data.employeeTimesheets);
            oos.writeObject(data.employees);

            oos.close();
            fos.close();
        } catch (FileNotFoundException e) {
            logger.error("Error saving employee and scheduler data.", e);
            logger.info("File not found: " + path + fileName);
        } catch (IOException e) {
            logger.error("Error saving employee and scheduler data.", e);
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    logger.error("Saving Data: Error closing FileOutputStream");
                    e.printStackTrace();
                }
            }
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    logger.error("Saving Data: Error closing ObjectOutputStream");
                    e.printStackTrace();
                }
            }
        }
        logger.info("Employee and scheduler data saved.");
    }

    private static void saveScheduleData(ArrayList<Day> schedule) {
        logger.traceEntry();
        SimpleDateFormat timeFormat = new SimpleDateFormat("M/d/yyyy HH:mm.ss a"),
                dateFormat = new SimpleDateFormat("M/d/yyyy");
        BufferedWriter bw = null;

        try {
            logger.info("Saving schedule data...");
            bw = new BufferedWriter(new FileWriter(new File(path + "\\" + scheduleFileName), false));
            bw.write("SCHEDULE");

            for (Day d : schedule) {
                bw.newLine();
                bw.write(dateFormat.format(d.getDate()));
                bw.newLine();
                bw.write(timeFormat.format(d.getStartTime()));
                bw.newLine();
                bw.write(timeFormat.format(d.getEndTime()));
                bw.newLine();
                if (d.isActive()) {
                    bw.write("1");
                } else {
                    bw.write("0");
                }
            }

        } catch (FileNotFoundException e) {
            logger.info("File not found: " + path + scheduleFileName);
            logger.error("Error saving schedule data.", e);
        } catch (IOException e) {
            logger.error("Error saving schedule data.", e);
            e.printStackTrace();
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    logger.error("Saving Schedule Data: Error closing FileOutputStream");
                    e.printStackTrace();
                }
            }

        }
        logger.info("Schedule data saved.");
    }

    private static void saveConfigData() {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            logger.info("Saving server config...");
            fos = new FileOutputStream(new File(path + "\\" + configFileName), false);
            oos = new ObjectOutputStream(fos);
            ServerConfig serverConfig = ServerConfig.getInstance();
            oos.writeUTF(serverConfig.getExcelPath());
            oos.writeInt(serverConfig.getServerPort());
            oos.writeUTF(serverConfig.getInjuryEmailRecipient());
            oos.close();
            fos.close();
        } catch (FileNotFoundException e) {
            logger.error("Error saving server config.", e);
            logger.info("File not found: " + path + "\\" + configFileName);
        } catch (IOException e) {
            logger.error("Error saving server config.", e);
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    logger.error("Saving server config: Error closing FileOutputStream");
                    e.printStackTrace();
                }
            }
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    logger.error("Saving server config: Error closing ObjectOutputStream");
                    e.printStackTrace();
                }
            }
        }
        logger.info("Server configuration saved.");
    }

    public static EmployeeAndSchedulerData loadEmployeeAndSchedulerData() {
        logger.traceEntry();
        EmployeeAndSchedulerData data = new EmployeeAndSchedulerData();
        File savedDataFile = new File(path + "\\" + fileName);
        if (savedDataFile.exists()) {
            logger.info("Loading employee and scheduler data...");
            FileInputStream fis = null;
            ObjectInputStream ois = null;
            try {
                fis = new FileInputStream(savedDataFile);
                ois = new ObjectInputStream(fis);

                data.schedulerDevices = (HashMap<String, DeviceInfo>) ois.readObject();
                data.employeeTimesheets = (HashMap<UUID, EmployeeTimesheet>) ois.readObject();
                data.employees = (ArrayList<Employee>) ois.readObject();

                data.dataLoaded = true;
                logger.info("Data loaded successfully.");
            } catch (FileNotFoundException e) {
                logger.error("Error loading data.", e);
                e.printStackTrace();
            } catch (IOException e) {
                logger.error("Error loading data.", e);
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                logger.error("Error loading data.", e);
                e.printStackTrace();
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        logger.error("Loading Data: Error closing FileInputStream");
                        e.printStackTrace();
                    }
                }
                if (ois != null) {
                    try {
                        ois.close();
                    } catch (IOException e) {
                        logger.error("Loading Data: Error closing ObjectOutputStream");
                        e.printStackTrace();
                    }
                }
            }
        }
        return logger.traceExit(data);
    }

    public static ScheduleData loadScheduleData() {
        logger.traceEntry();
        SimpleDateFormat timeFormat = new SimpleDateFormat("M/d/yyyy HH:mm.ss a"),
                dateFormat = new SimpleDateFormat("M/d/yyyy");
        ScheduleData data = new ScheduleData();
        data.schedule = new ArrayList<>();
        File savedDataFile = new File(path + "\\" + scheduleFileName);
        if (savedDataFile.exists()) {
            logger.info("Loading schedule data...");

            Scanner scanner = null;
            try {
                scanner = new Scanner(new BufferedReader(new FileReader(savedDataFile)));
                scanner.skip("SCHEDULE");
                for (int i = 0; i < 7; i++) {
                    String date = scanner.nextLine();
                    String startTime = scanner.nextLine();
                    String endTime = scanner.nextLine();
                    int isActiveInt = scanner.nextInt();
                    System.out.println(date + " " + startTime + " " + endTime + " " + isActiveInt);
                    boolean isActive = true;
                    if (isActiveInt == 0) isActive = false;
                    Day d = new Day(dateFormat.parse(date), isActive);
                    d.setStartTime(timeFormat.parse(startTime));
                    d.setEndTime(timeFormat.parse(endTime));
                    data.schedule.add(d);
                }

                data.dataLoaded = true;
                logger.info("Schedule data loaded successfully.");
            } catch (FileNotFoundException e) {
                logger.error("Error loading schedule data.", e);
                e.printStackTrace();
            } catch (ParseException e) {
                logger.error("Error parsing schedule file.", e);
            } finally {
                if (scanner != null) {
                    scanner.close();
                }
            }
        }
        logger.traceExit(data.dataLoaded);
        return data;
    }

    public static ServerConfigData loadConfigData() {
        logger.traceEntry();
        ServerConfigData serverConfigData = new ServerConfigData();
        File savedDataFile = new File(path + "\\" + configFileName);
        if (savedDataFile.exists()) {
            logger.info("Loading server config...");
            FileInputStream fis = null;
            ObjectInputStream ois = null;
            try {
                fis = new FileInputStream(savedDataFile);
                ois = new ObjectInputStream(fis);

                String excelPath = ois.readUTF();
                int serverPort = ois.readInt();
                String injuryEmailRecipient = ois.readUTF();
                serverConfigData.excelPath = excelPath;
                serverConfigData.serverPort = serverPort;
                serverConfigData.injuryEmailRecipient = injuryEmailRecipient;
                serverConfigData.dataLoaded = true;
                logger.info("Server config loaded successfully.");
            } catch (FileNotFoundException e) {
                logger.error("Error loading server config.", e);
                e.printStackTrace();
            } catch (IOException e) {
                logger.error("Error loading server config.", e);
                e.printStackTrace();
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        logger.error("Loading Server Config: Error closing FileInputStream");
                        e.printStackTrace();
                    }
                }
                if (ois != null) {
                    try {
                        ois.close();
                    } catch (IOException e) {
                        logger.error("Loading Server Config: Error closing ObjectOutputStream");
                        e.printStackTrace();
                    }
                }
            }
        }
        logger.traceExit(serverConfigData.dataLoaded);
        return serverConfigData;
    }

    public static String getPath() {
        return path;
    }

    public static class ServerConfigData {
        public boolean dataLoaded = false;
        public String excelPath = "", injuryEmailRecipient = "";
        public int serverPort = 9696;
    }

    public static class EmployeeAndSchedulerData {
        boolean dataLoaded = false;
        HashMap<String, DeviceInfo> schedulerDevices = null;
        HashMap<UUID, EmployeeTimesheet> employeeTimesheets = null;
        ArrayList<Employee> employees;
    }

    public static class ScheduleData {
        boolean dataLoaded = false;
        ArrayList<Day> schedule = null;
    }
}
