package valkyrie.server.ui.dialog.add;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class AddEmployeeDialog {

    public AddEmployeeDialog() {
    }

    public Stage showAddEmployeeDialog(){
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource(
                        "dialog_add_employee.fxml"
                )
        );

        Stage stage = new Stage(StageStyle.DECORATED);
        stage.setTitle("Add Employee");
        try {
            stage.setScene(
                    new Scene(
                            (Pane) loader.load()
                    )
            );
        } catch (IOException e) {
            e.printStackTrace();
        }


        AddEmployeeController controller =
                loader.<AddEmployeeController>getController();

        return stage;
    }
}
