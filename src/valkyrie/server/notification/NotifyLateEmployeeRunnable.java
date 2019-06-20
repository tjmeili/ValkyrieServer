package valkyrie.server.notification;

import server.data.DeviceInfo;
import server.data.Employee;
import server.data.EmployeeTimesheet;

import java.io.*;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class NotifyLateEmployeeRunnable implements Runnable {

    private int port = 9797;
    private Socket socket;
    private String host;
    private ArrayList<Employee> lateEmployees;

    private static final int NOTIFY_LATE_EMPLOYEES = 303;

    public NotifyLateEmployeeRunnable(String host, int port, ArrayList<Employee> lateEmployees) {
        this.host = host;
        this.port = port;
        this.lateEmployees = lateEmployees;
    }

    @Override
    public void run() {
        try {
            socket = new Socket(host, port);
            ObjectOutputStream output = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            output.flush();
            ObjectInputStream input = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));

            output.writeInt(NOTIFY_LATE_EMPLOYEES);
            output.flush();
            output.writeObject(lateEmployees);
            output.flush();

            input.close();
            output.close();
        } catch(ConnectException e){
            System.out.println("Unable to connect to device " + host + ":" +port);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(socket != null){
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void log(String s){
        System.out.println(s);
    }
}
