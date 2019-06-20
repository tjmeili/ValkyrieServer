package valkyrie.server.logging.messages;

import org.apache.logging.log4j.message.Message;
import server.data.Day;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class ScheduleMessage implements Message {

    private final ArrayList<Day> schedule;

    public ScheduleMessage(ArrayList<Day> schedule) {
        this.schedule = schedule;
    }

    @Override
    public String getFormattedMessage() {
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat dateFormat = new SimpleDateFormat("M/d/yyy"),
                timeFormat = new SimpleDateFormat("h:mm.ss a");
        sb.append("\n");
        for (Day day : schedule) {
            sb.append("Date: ");
            sb.append(dateFormat.format(day.getStartTime()));
            sb.append("\t");
            sb.append(day.isActive());
            if (day.isActive()) {
                sb.append("\t");
                sb.append("Start: ");
                sb.append(timeFormat.format(day.getStartTime()));
                sb.append("\t");
                sb.append("End: ");
                sb.append("\t");
                sb.append(timeFormat.format(day.getEndTime()));
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    @Override
    public String getFormat() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("M/d/yyy");
        return "Schedule " +
                dateFormat.format(schedule.get(0).getDate())
                + " - " +
                dateFormat.format(schedule.get(6).getDate());
    }

    @Override
    public Object[] getParameters() {
        Object[] objects = new Object[1];
        objects[0] = schedule;
        return objects;
    }

    @Override
    public Throwable getThrowable() {
        return null;
    }
}
