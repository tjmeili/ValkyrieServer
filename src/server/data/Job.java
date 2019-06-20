package server.data;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by TJ on 3/23/2018.
 */

public class Job implements Serializable{
    private int jobNumber;
    private Date startTime, endTime;

    public Job(int jobNumber) {
        this.jobNumber = jobNumber;
    }

    public Job(int jobNumber, Date startTime) {
        this.jobNumber = jobNumber;
        this.startTime = startTime;
    }



    public int getJobNumber() {
        return jobNumber;
    }

    public void setJobNumber(int jobNumber) {
        this.jobNumber = jobNumber;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }


    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
}
