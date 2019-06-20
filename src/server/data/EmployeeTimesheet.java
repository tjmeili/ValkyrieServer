package server.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

/**
 * Created by TJ on 3/21/2018.
 */

public class EmployeeTimesheet extends Employee implements Serializable {

    private ArrayList<Job> jobs;
    private Date date, punchInTime, punchOutTime;
    private double totalHours;

    public EmployeeTimesheet(){
        super("", "", null);
        jobs = new ArrayList<Job>();
        date = new Date();
        totalHours = 0;
    }

    public EmployeeTimesheet(String firstName, String lastName, UUID employeeID) {
        super(firstName, lastName, employeeID);
        jobs = new ArrayList<Job>();
        date = new Date();
        totalHours = 0;
    }

    public EmployeeTimesheet(Employee e){
        super(e.getFirstName(), e.getLastName(), e.getEmployeeID());
        jobs = new ArrayList<Job>();
        date = new Date();
        totalHours = 0;
    }

    public void setInfo(String firstName, String lastName, UUID employeeID){
        this.setFirstName(firstName);
        this.setLastName(lastName);
        this.setEmployeeID(employeeID);
    }

    public void addJob(int jobNumber, Date jobStartTime){
        if(jobs.size() > 0){
            jobs.get(jobs.size() - 1).setEndTime(jobStartTime);
        }
        jobs.add(new Job(jobNumber, jobStartTime));
    }

    public void addJob(Job job){
        if(jobs.size() > 0){
            jobs.get(jobs.size() - 1).setEndTime(job.getStartTime());
        }
        jobs.add(job);
    }

    public void addAllJobs(ArrayList<Job> jobs){
        jobs.addAll(jobs);
    }

    public Date getPunchInTime() {
        return punchInTime;
    }

    public void setPunchInTime(Date punchInTime) {
        this.punchInTime = punchInTime;
    }

    public Date getPunchOutTime() {
        return punchOutTime;
    }

    public void setPunchOutTime(Date punchOutTime) {
        this.punchOutTime = punchOutTime;
        if(jobs.size() > 0){
            jobs.get(jobs.size() - 1).setEndTime(punchOutTime);
        }
    }

    public void newDay(){
        date = new Date();
        punchInTime = null;
        punchOutTime = null;
        jobs.clear();
    }

    public ArrayList<Job> getJobs(){
        return jobs;
    }


    public Date getDate(){
        return date;
    }

    public double getTotalHours() {
        return totalHours;
    }


}