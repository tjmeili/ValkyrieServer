package valkyrie.server.ui.dialog.edit.employee;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import server.data.EmployeeTimesheet;

import java.io.IOException;

public class EmployeeDetailsDialog {

    public EmployeeDetailsDialog() {
    }

    public Stage showEmployeeDetailsDialog(EmployeeTimesheet timesheet) {

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource(
                        "valkyrie_server_employee_details.fxml"
                )
        );

        Stage stage = new Stage(StageStyle.DECORATED);
        stage.setTitle("Employee Details");
        try {
            stage.setScene(
                    new Scene(
                            (Pane) loader.load()
                    )
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        EmployeeDetailsController controller =
                loader.<EmployeeDetailsController>getController();
        controller.initData(timesheet);
        return stage;
    }
}
