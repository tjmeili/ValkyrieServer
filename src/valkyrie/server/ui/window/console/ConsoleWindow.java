package valkyrie.server.ui.window.console;

import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class ConsoleWindow {

    public ConsoleWindow() {
    }

    public Stage showConsoleWindow(/*ByteArrayOutputStream byteArrayOutputStream*/){
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource(
                        "valkyrie_server_console.fxml"
                )
        );

        Stage stage = new Stage(StageStyle.DECORATED);
        stage.setTitle("Server Console");
        try {
            stage.setScene(
                    new Scene(
                            (Pane) loader.load()
                    )
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                PrintStream sysPrintStream = new PrintStream(new FileOutputStream(FileDescriptor.out));
                System.setErr(sysPrintStream);
                System.setOut(sysPrintStream);
            }
        });

        ConsoleController controller =
                loader.<ConsoleController>getController();
        //controller.load(byteArrayOutputStream);
        return stage;
    }
}
