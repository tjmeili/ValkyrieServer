package valkyrie.server.notification;

import valkyrie.server.local.data.DataHolder;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;

public class NotifyScheduleUpdateRunnable implements Runnable{

    private int port = 9797;
    private Socket socket;
    private String host;

    private static final int NOTIFY_SCHEDULE_UPDATED = 301;

    public NotifyScheduleUpdateRunnable(String host, int port) {
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

            output.writeInt(NOTIFY_SCHEDULE_UPDATED);
            output.flush();
            output.writeObject(DataHolder.getInstance().getSchedule());
            output.flush();

            input.close();
            output.close();
        } catch(ConnectException e){
            System.out.println("Unable to connect to device " + host);
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
}
