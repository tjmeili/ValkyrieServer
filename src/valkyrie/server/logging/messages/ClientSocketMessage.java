package valkyrie.server.logging.messages;

import org.apache.logging.log4j.message.Message;

import java.net.Socket;

public class ClientSocketMessage implements Message {

    private final Socket socket;
    private final long time;

    public ClientSocketMessage(Socket socket, long time) {
        this.socket = socket;
        this.time = time;
    }

    @Override
    public String getFormattedMessage() {
        String result = "Connected to: " +
                socket.getInetAddress().getHostName() +
                "\t" + socket.getInetAddress().getHostAddress() +
                "\t" + time;
        return result;
    }

    @Override
    public String getFormat() {
        return socket.getInetAddress().getHostAddress();
    }

    @Override
    public Object[] getParameters() {
        return new Object[0];
    }

    @Override
    public Throwable getThrowable() {
        return null;
    }
}
