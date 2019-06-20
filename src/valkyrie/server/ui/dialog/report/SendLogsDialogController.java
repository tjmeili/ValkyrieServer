package valkyrie.server.ui.dialog.report;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import valkyrie.server.MGmail;

public class SendLogsDialogController {
    private static final Logger logger = LogManager.getLogger(SendLogsDialogController.class.getName());
    @FXML
    private TextArea taDescription;

    @FXML
    private Button btnSendIt;


    public void initialize() {
        btnSendIt.setOnAction(event -> {
            String body = taDescription.textProperty().getValue();
            logger.traceEntry(body);
            Node source = (Node) event.getSource();
            Stage stage = (Stage) source.getScene().getWindow();
            stage.close();
            new Thread(() -> MGmail.sendLogEmail(body)).start();
        });
    }
}
