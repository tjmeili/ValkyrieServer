package valkyrie.server.ui.dialog.add;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import valkyrie.server.local.data.DataHolder;

public class AddEmployeeController {

    @FXML private TextField tfFirstName;
    @FXML private TextField tfLastName;
    @FXML private Button btnOk;
    @FXML private Button btnCancel;

    public void initialize(){

        btnCancel.setOnAction(event -> closeStage(event));

        btnOk.setOnAction(event -> {
            String first = tfFirstName.getText().trim(),
                    last = tfLastName.getText().trim();
            if(!first.isEmpty() || last.isEmpty()){
                DataHolder.getInstance().addEmployee(first, last);
            }
            closeStage(event);
        });

    }

    private void closeStage(Event event){
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }
}
