package valkyrie.server.ui.window.console;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class ConsoleController {

    @FXML
    private TextArea consoleTextArea;

    private PrintStream ps;

    public void initialize(){
        ps = new PrintStream(new Console(consoleTextArea));
        System.setOut(ps);
        System.setErr(ps);
    }

    /*public void load(ByteArrayOutputStream baos){
        System.out.println(new String(baos.toByteArray(), StandardCharsets.UTF_8));
    }*/

    public class Console extends OutputStream {
        private TextArea console;

        public Console(TextArea console) {
            this.console = console;
        }

        public void appendText(final String valueOf) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    console.appendText(valueOf);
                }
            });
        }

        public void write(int b) throws IOException {
            appendText(String.valueOf((char)b));
        }
    }

}
