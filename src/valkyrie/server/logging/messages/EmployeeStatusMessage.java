package valkyrie.server.logging.messages;

import org.apache.logging.log4j.message.Message;
import valkyrie.server.ui.listview.employee.EmployeeStatus;

public class EmployeeStatusMessage implements Message {

    private final EmployeeStatus status;

    public EmployeeStatusMessage(EmployeeStatus status) {
        this.status = status;
    }

    @Override
    public String getFormattedMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("Employee Status: ");
        sb.append(status.getName());
        sb.append("\t");
        sb.append(status.isClockedIn());
        sb.append("\t");
        sb.append(status.getCurrentJobNumber());
        return sb.toString();
    }

    @Override
    public String getFormat() {
        return status.getName();
    }

    @Override
    public Object[] getParameters() {
        Object[] obj = new Object[1];
        obj[0] = status;
        return obj;
    }

    @Override
    public Throwable getThrowable() {
        return null;
    }
}
