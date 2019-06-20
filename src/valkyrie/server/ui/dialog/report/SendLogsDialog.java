package valkyrie.server.ui.dialog.report;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class SendLogsDialog {

    public SendLogsDialog() {
    }

    public Stage showSendLogsDialog() {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource(
                        "dialog_send_logs.fxml"
                )
        );

        Stage stage = new Stage(StageStyle.DECORATED);
        stage.setTitle("Something Broke");
        try {
            stage.setScene(
                    new Scene(
                            (Pane) loader.load()
                    )
            );
        } catch (IOException e) {
            e.printStackTrace();
        }


        SendLogsDialogController controller =
                loader.<SendLogsDialogController>getController();

        stage.show();
        return stage;
    }
}
