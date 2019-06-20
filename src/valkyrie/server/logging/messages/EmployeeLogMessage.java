package valkyrie.server.logging.messages;

import org.apache.logging.log4j.message.Message;
import server.data.EmployeeTimesheet;
import server.data.Job;

import java.text.SimpleDateFormat;

public class EmployeeLogMessage implements Message {

    private final EmployeeTimesheet timesheet;

    public EmployeeLogMessage(EmployeeTimesheet timesheet) {
        this.timesheet = timesheet;
    }

    @Override
    public String getFormattedMessage() {
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat formatter = new SimpleDateFormat("h:mm.ss");
        sb.append(timesheet.getFirstName());
        sb.append(" ");
        sb.append(timesheet.getLastName());
        sb.append("\n");
        sb.append(timesheet.getEmployeeID().toString());
        sb.append("\n");
        if (timesheet.getPunchInTime() != null)
            sb.append(formatter.format(timesheet.getPunchInTime()));
        sb.append(" ");
        if (timesheet.getPunchOutTime() != null)
            sb.append(formatter.format(timesheet.getPunchOutTime()));

        if (timesheet.getJobs().size() > 0) {
            sb.append("\nJobs\n");
            for (Job job : timesheet.getJobs()) {
                sb.append(job.getJobNumber());
                String jobStart = "null", jobEnd = "null";
                if (job.getStartTime() != null) {
                    jobStart = formatter.format(job.getStartTime());
                }
                if (job.getEndTime() != null) {
                    jobEnd = formatter.format(job.getEndTime());
                }
                sb.append(" ");
                sb.append(jobStart);
                sb.append(" ");
                sb.append(jobEnd);
            }
        }
        return sb.toString();
    }

    @Override
    public String getFormat() {
        return timesheet.getFirstName() + " " + timesheet.getLastName();
    }

    @Override
    public Object[] getParameters() {
        Object[] obj = new Object[1];
        obj[0] = timesheet;
        return obj;
    }

    @Override
    public Throwable getThrowable() {
        return null;
    }
}
