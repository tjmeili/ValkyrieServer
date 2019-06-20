package valkyrie.server.notification;

import server.data.DeviceInfo;
import valkyrie.server.local.data.config.ServerConfig;

import java.io.*;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

public class NotifyServerInfoRunnable implements Runnable {

    private Socket socket;
    private String host;
    private int port = 9797;

    private static final int NOTIFY_SERVER_INFO = 901;

    public NotifyServerInfoRunnable(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void run() {
        log("Sending server info to device " + host);

            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(host, port), 3000);
                ObjectOutputStream output = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                output.flush();
                ObjectInputStream input = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
                long time = System.currentTimeMillis();

                output.writeInt(NOTIFY_SERVER_INFO);
                output.flush();
                output.writeUTF(InetAddress.getLocalHost().getHostAddress());
                output.flush();
                output.writeInt(ServerConfig.getInstance().getServerPort());
                output.flush();

                input.close();
                output.close();
                log("Transfer Complete " + time);
            } catch (ConnectException e){
                log("Could not connect to device " + host);
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
    private static void log(String log){
        System.out.println(log);
    }
}
