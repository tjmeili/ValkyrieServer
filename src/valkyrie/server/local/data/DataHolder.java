package valkyrie.server.local.data;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.data.*;
import valkyrie.server.logging.messages.EmployeeLogMessage;
import valkyrie.server.logging.messages.ScheduleMessage;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;

// Thread safe singleton to handle changes to server data
// TODO: Convert to use SQL database or Google Firebase
public class DataHolder {
    private static final Logger logger = LogManager.getLogger(DataHolder.class.getName());
    private HashMap<String, DeviceInfo> schedulerDevices;
    private HashMap<UUID, EmployeeTimesheet> employeeTimesheets;
    private ArrayList<Employee> employees;
    private ArrayList<Day> schedule;

    private Consumer<EmployeeTimesheet> newEmployeeCallback = employeeTimesheet -> {};
    private Consumer<EmployeeTimesheet> employeePunchInCallback = employeeTimesheet -> {};
    private Consumer<EmployeeTimesheet> employeePunchOutCallback = employeeTimesheet -> {};
    private Consumer<EmployeeTimesheet> employeeNewJobCallback = employeeTimesheet -> {};
    private Consumer<ArrayList<Day>> scheduleCallback = schedule -> {};
    private Consumer<String> newSchedulerDeviceCallback = deviceIP -> {};
    private Consumer<UUID> employeeRemovedCallback = uuid -> {
    };

    private SimpleDateFormat logFormatter = new SimpleDateFormat("M/d/yy h:mm.ss");

    public DataHolder() {
        initialize();
    }

    private static class SingletonHelper{
        private static final DataHolder INSTANCE = new DataHolder();
    }

    public static DataHolder getInstance(){
        return SingletonHelper.INSTANCE;
    }

    // load data and initialize data
    private void initialize(){
        FileHandler.EmployeeAndSchedulerData esData = FileHandler.loadEmployeeAndSchedulerData();
        FileHandler.ScheduleData scheduleData = FileHandler.loadScheduleData();
        logger.info("Employee data Loaded: " + esData.dataLoaded);
        logger.info("Schedule Loaded: " + esData.dataLoaded);
        if (!esData.dataLoaded) {
            schedule = new ArrayList<Day>();
            employees = new ArrayList<Employee>();
            schedulerDevices = new HashMap<String, DeviceInfo>();
            employeeTimesheets = new HashMap<UUID, EmployeeTimesheet>();
        } else {
            employees = esData.employees;
            schedulerDevices = esData.schedulerDevices;
            employeeTimesheets = esData.employeeTimesheets;
            logi("Registered Employees: " + employees.size());
            logi("Registered Schedulers " + schedulerDevices.values().size());
        }
        if (!scheduleData.dataLoaded) {
            schedule = new ArrayList<>();
            initializeSchedule();
        } else {
            schedule = scheduleData.schedule;
            checkScheduleDates();
        }

    }

    private void initializeSchedule(){
        if(schedule.size() > 0) {
            schedule.clear();
        }
        for(int i = 0; i < 7; i++){
            Calendar c = Calendar.getInstance();
            c.setFirstDayOfWeek(Calendar.WEDNESDAY);
            c.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
            c.add(Calendar.DAY_OF_WEEK, i);
            Day d = new Day(c.getTime(), true);
            d.setDefaultTimes();
            schedule.add(d);
        }
    }

    private void checkScheduleDates(){
        if (schedule == null) return;
        Calendar refereceCal = Calendar.getInstance();
        refereceCal.setFirstDayOfWeek(Calendar.WEDNESDAY);
        refereceCal.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
        for (Day d : schedule){
            Calendar c = Calendar.getInstance();
            c.setTime(d.getDate());
            c.set(refereceCal.get(Calendar.YEAR), refereceCal.get(Calendar.MONTH), refereceCal.get(Calendar.DAY_OF_MONTH));
            d.setDate(c.getTime());

            c.setTime(d.getStartTime());
            c.set(refereceCal.get(Calendar.YEAR), refereceCal.get(Calendar.MONTH), refereceCal.get(Calendar.DAY_OF_MONTH));
            d.setStartTime(c.getTime());

            c.setTime(d.getEndTime());
            c.set(refereceCal.get(Calendar.YEAR), refereceCal.get(Calendar.MONTH), refereceCal.get(Calendar.DAY_OF_MONTH));
            d.setEndTime(c.getTime());

            refereceCal.add(Calendar.DAY_OF_MONTH, 1);
        }

    }

    // reset timesheet for each employee
    public void resetDay(){
        logger.traceEntry();
        for(EmployeeTimesheet t : employeeTimesheets.values()){
            t.newDay();
        }
        logger.traceExit();
    }

    public void resetWeek(){
        logger.traceEntry();
        resetDay();
        for(Day d : schedule){
            d.rollToNextDay();
        }
        logger.traceExit();
    }

    public void registerEmployeeDevice(UUID employeeID, String deviceIP, int devicePort){
        logger.traceEntry(employeeID.toString(), deviceIP, devicePort);
        if(employeeTimesheets.containsKey(employeeID)){
            EmployeeTimesheet timesheet = employeeTimesheets.get(employeeID);
            timesheet.setDeviceInfo(new DeviceInfo(deviceIP, devicePort));
            employeeTimesheets.put(employeeID, timesheet);
            saveEmployeesAndSchedulers();
            logger.info(new EmployeeLogMessage(timesheet));
        }
        logger.traceExit();
    }

    public void registerSchedulerDevice(String deviceIP, int devicePort){
        logger.traceEntry(deviceIP, devicePort);
        schedulerDevices.put(deviceIP, new DeviceInfo(deviceIP, devicePort));
        newSchedulerDeviceCallback.accept(deviceIP);
        saveEmployeesAndSchedulers();
        logger.traceExit();
    }

    public void addEmployee(String firstName, String lastName){
        logger.traceEntry(firstName, lastName);
        UUID id = UUID.randomUUID();
        Employee e = new Employee(firstName, lastName, id);
        EmployeeTimesheet t = new EmployeeTimesheet(e);
        this.employees.add(e);
        this.employeeTimesheets.put(t.getEmployeeID(), t);
        newEmployeeCallback.accept(t);
        saveEmployeesAndSchedulers();
        logger.traceExit();
    }

    public void addUnregisteredEmployee(UUID uuid) {
        logger.traceEntry(uuid.toString());
        Employee e = new Employee("unregistered", "", uuid);
        this.employees.add(e);
        EmployeeTimesheet t = new EmployeeTimesheet(e);
        this.employees.add(e);
        this.employeeTimesheets.put(t.getEmployeeID(), t);
        newEmployeeCallback.accept(t);
        saveEmployeesAndSchedulers();
        logger.traceExit();
    }

    public void removeEmployee(UUID uuid) {
        logger.traceEntry(employeeTimesheets.get(uuid).getFirstName() + " " + employeeTimesheets.get(uuid).getLastName());
        employeeTimesheets.remove(uuid);
        employeeRemovedCallback.accept(uuid);
        saveEmployeesAndSchedulers();
        logger.traceExit();
    }

    public void updateEmployeeTimesheet(EmployeeTimesheet timesheet){
        logger.traceEntry(new EmployeeLogMessage(timesheet));
        employeeTimesheets.put(timesheet.getEmployeeID(), timesheet);
        saveEmployeesAndSchedulers();
        logger.traceExit(new EmployeeLogMessage(employeeTimesheets.get(timesheet.getEmployeeID())));
    }

    public void updateSchedule(ArrayList<Day> newSchedule){
        logger.traceEntry(new ScheduleMessage(newSchedule));
        this.schedule = newSchedule;
        scheduleCallback.accept(newSchedule);
        FileHandler.saveSchedule(schedule);
        logger.traceExit(new ScheduleMessage(this.schedule));
    }

    public Date getCurrentDateStartTime(){
        logger.traceEntry();
        Calendar c = Calendar.getInstance();
        int dayOfWeek = 0;
        switch (c.get(Calendar.DAY_OF_WEEK)){
            case Calendar.WEDNESDAY:
                dayOfWeek = 0;
                break;
            case Calendar.THURSDAY:
                dayOfWeek = 1;
                break;
            case Calendar.FRIDAY:
                dayOfWeek = 2;
                break;
            case Calendar.SATURDAY:
                dayOfWeek = 3;
                break;
            case Calendar.SUNDAY:
                dayOfWeek = 4;
                break;
            case Calendar.MONDAY:
                dayOfWeek = 5;
                break;
            case Calendar.TUESDAY:
                dayOfWeek = 6;
                break;

        }
        return logger.traceExit(schedule.get(dayOfWeek).getStartTime());
    }

    public Day getCurrentDateSchedule(){
        logger.traceEntry();
        Calendar c = Calendar.getInstance();
        c.setFirstDayOfWeek(Calendar.WEDNESDAY);
        return logger.traceExit(schedule.get(c.get(Calendar.DAY_OF_WEEK)));
    }

    public ArrayList<DeviceInfo> getAllDevices(){
        ArrayList<DeviceInfo> devices = new ArrayList<>();
        devices.addAll(schedulerDevices.values());
        devices.addAll(getEmployeeDevices());
        return devices;
    }

    public ArrayList<DeviceInfo> getEmployeeDevices(){
        ArrayList<DeviceInfo> devices = new ArrayList<>();
        for(EmployeeTimesheet t : employeeTimesheets.values()){
            devices.add(t.getDeviceInfo());
        }
        return devices;
    }

    public HashMap<String, DeviceInfo> getSchedulerDevices() {
        return schedulerDevices;
    }

    public ArrayList<Day> getSchedule() {
        return schedule;
    }

    public ArrayList<Employee> getEmployees() {
        return employees;
    }

    public HashMap<UUID, EmployeeTimesheet> getEmployeeTimesheetMap() {
        return employeeTimesheets;
    }

    public void setEmployeeTimesheetPunchIn(UUID uuid, Date punchInTime){
        EmployeeTimesheet timesheet = employeeTimesheets.get(uuid);
        logger.traceEntry(new EmployeeLogMessage(timesheet));
        logger.trace(logFormatter.format(punchInTime));
        if (timesheet != null) {
            logger.trace(employeeTimesheets.get(uuid));
            employeeTimesheets.get(uuid).setPunchInTime(punchInTime);
            employeePunchInCallback.accept(employeeTimesheets.get(uuid));
        }
        saveEmployeesAndSchedulers();
        logger.traceExit(new EmployeeLogMessage(employeeTimesheets.get(uuid)));
    }

    public void setEmployeeTimesheetPunchOut(UUID uuid, Date punchOutTime){
        EmployeeTimesheet timesheet = employeeTimesheets.get(uuid);
        logger.traceEntry(new EmployeeLogMessage(timesheet));
        logger.trace(logFormatter.format(punchOutTime));
        if (timesheet != null) {
            employeeTimesheets.get(uuid).setPunchOutTime(punchOutTime);
            employeePunchOutCallback.accept(employeeTimesheets.get(uuid));
            generateExcel(employeeTimesheets.get(uuid));
        }
        saveEmployeesAndSchedulers();
        logger.traceExit(new EmployeeLogMessage(employeeTimesheets.get(uuid)));
    }

    public void addJobToEmployeeTimesheet(UUID uuid, int jobNumber, Date startTime){
        EmployeeTimesheet timesheet = employeeTimesheets.get(uuid);
        logger.traceEntry(new EmployeeLogMessage(timesheet));
        logger.trace("Job Number: " + jobNumber, logFormatter.format(startTime));
        if (employeeTimesheets.get(uuid) != null) {
            employeeTimesheets.get(uuid).addJob(jobNumber, startTime);
            employeeNewJobCallback.accept(employeeTimesheets.get(uuid));
        }
        saveEmployeesAndSchedulers();
        logger.traceExit(new EmployeeLogMessage(employeeTimesheets.get(uuid)));
    }

    public void saveEmployeesAndSchedulers() {
        FileHandler.EmployeeAndSchedulerData data = new FileHandler.EmployeeAndSchedulerData();
        data.employees = employees;
        data.employeeTimesheets = employeeTimesheets;
        data.schedulerDevices = schedulerDevices;
        FileHandler.saveEmployeeAndSchedulerData(data);
    }

    private void generateExcel(EmployeeTimesheet timesheet) {
        ExcelWriter.getInstance().generateSingleExcel(timesheet);
    }

    public void setNewSchedulerDeviceCallback(Consumer<String> newSchedulerDeviceCallback) {
        this.newSchedulerDeviceCallback = newSchedulerDeviceCallback;
    }

    public void setNewEmployeeCallback(Consumer<EmployeeTimesheet> newEmployeeCallback) {
        this.newEmployeeCallback = newEmployeeCallback;
    }

    public void setEmployeePunchInCallback(Consumer<EmployeeTimesheet> employeePunchInCallback) {
        this.employeePunchInCallback = employeePunchInCallback;
    }

    public void setEmployeePunchOutCallback(Consumer<EmployeeTimesheet> employeePunchOutCallback) {
        this.employeePunchOutCallback = employeePunchOutCallback;
    }

    public void setEmployeeNewJobCallback(Consumer<EmployeeTimesheet> employeeNewJobCallback) {
        this.employeeNewJobCallback = employeeNewJobCallback;
    }

    public void setScheduleCallback(Consumer<ArrayList<Day>> scheduleCallback) {
        this.scheduleCallback = scheduleCallback;
    }

    public void setEmployeeRemovedCallback(Consumer<UUID> employeeRemovedCallback) {
        this.employeeRemovedCallback = employeeRemovedCallback;
    }

    private void logi(String log) {
        logger.info(log);
    }

    public void printSchedule(){
        SimpleDateFormat formatter = new SimpleDateFormat("h:mm a");
        SimpleDateFormat dayNameFormatter = new SimpleDateFormat("EEE\tM/d/yy");
        for(Day d : schedule){
            System.out.println( dayNameFormatter.format(d.getDate()) + "\t" + d.isActive() + "\t" + formatter.format(d.getStartTime()) + "\t" + formatter.format(d.getEndTime()));
        }
    }

    public void printTimesheet(EmployeeTimesheet t){
        SimpleDateFormat formatter = new SimpleDateFormat("h:mm a");
        System.out.println(t.getFirstName() + " " + t.getLastName());
        for(Job j : t.getJobs()){
            System.out.println(j.getJobNumber() + " " + formatter.format(j.getStartTime()) + " " + formatter.format(j.getEndTime()));
        }
    }
}
