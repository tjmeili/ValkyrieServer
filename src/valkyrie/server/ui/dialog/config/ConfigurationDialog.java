package valkyrie.server.ui.dialog.config;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class ConfigurationDialog {

    public ConfigurationDialog() {
    }

    public Stage showConfigurationDialog(){
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource(
                        "valkyrie_server_config.fxml"
                )
        );

        Stage stage = new Stage(StageStyle.DECORATED);
        stage.setTitle("Server Configuration");
        try {
            stage.setScene(
                    new Scene(
                            (Pane) loader.load()
                    )
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        ConfigurationController controller =
                loader.<ConfigurationController>getController();
        return stage;
    }
}
