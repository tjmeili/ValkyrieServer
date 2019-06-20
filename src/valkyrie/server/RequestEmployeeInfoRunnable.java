package valkyrie.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.data.EmployeeTimesheet;
import valkyrie.server.local.data.DataExecutor;
import valkyrie.server.local.data.DataHolder;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;

// Handles registering employee device if they connect, but there is no information.
// Just in case data could not be found on the server.

public class RequestEmployeeInfoRunnable implements Runnable {
    private static final Logger logger = LogManager.getLogger(RequestEmployeeInfoRunnable.class.getName());
    private static final int REQUEST_EMPLOYEE_INFO = 444;
    private int port = 9797;
    private Socket socket;
    private String host;

    public RequestEmployeeInfoRunnable(String host, int port) {
        this.port = port;
        this.host = host;
    }

    @Override
    public void run() {

        try {
            socket = new Socket(host, port);
            ObjectOutputStream output = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            output.flush();
            ObjectInputStream input = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));

            output.writeInt(REQUEST_EMPLOYEE_INFO);
            output.flush();
            try {
                final EmployeeTimesheet timesheet = (EmployeeTimesheet) input.readObject();
                registerEmployee(timesheet, host, port);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            input.close();
            output.close();
        } catch (ConnectException e) {
            logger.error("Unable to connect to device " + host, e);
        } catch (IOException e) {
            logger.error("Unable to connect to device " + host, e);
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


    }

    private void registerEmployee(EmployeeTimesheet timesheet, String deviceIp, int devicePort) {
        logger.traceEntry();
        if (timesheet == null) return;
        final String ip = deviceIp;
        final int port = devicePort;
        DataExecutor.getInstance().execute(() -> {
            DataHolder.getInstance().updateEmployeeTimesheet(timesheet);
            DataHolder.getInstance().registerEmployeeDevice(timesheet.getEmployeeID(), ip, port);
        });
        logger.traceExit();
    }
}
























