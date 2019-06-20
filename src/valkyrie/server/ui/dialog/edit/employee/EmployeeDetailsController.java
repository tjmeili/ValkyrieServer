package valkyrie.server.ui.dialog.edit.employee;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.apache.commons.validator.routines.InetAddressValidator;
import server.data.DeviceInfo;
import server.data.EmployeeTimesheet;
import valkyrie.server.local.data.DataHolder;

import java.util.Optional;

public class EmployeeDetailsController {

    @FXML private Label lblName;
    @FXML private TextField tfDeviceIp;
    @FXML private Button btnOk;
    @FXML private Button btnCancel;
    @FXML private Button btnRemoveEmployee;
    @FXML private Label lblNotRegistered;

    private String deviceIP = "";
    private EmployeeTimesheet mTimesheet = null;

    public void initialize(){
        btnRemoveEmployee.setOnAction(event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setHeaderText("Confirm Remove");
            alert.setHeaderText("Are you sure you want to remove " + mTimesheet.getFirstName() + " " + mTimesheet.getLastName() + "?");
            Optional<ButtonType> result = alert.showAndWait();
            if(result.get() == ButtonType.OK){
                DataHolder.getInstance().removeEmployee(mTimesheet.getEmployeeID());

                Node source = (Node) event.getSource();
                Stage stage = (Stage) source.getScene().getWindow();
                stage.close();
            }
        });

        btnCancel.setOnAction(event -> {
            Node source = (Node) event.getSource();
            Stage stage = (Stage) source.getScene().getWindow();
            stage.close();
        });

        btnOk.setOnAction(event -> {
            if(!tfDeviceIp.getText().equals(deviceIP)){
                String newIP = tfDeviceIp.getText().trim();
                if (InetAddressValidator.getInstance().isValid(newIP)) {
                        DataHolder.getInstance().getEmployeeTimesheetMap().get(mTimesheet.getEmployeeID())
                                .setDeviceInfo(
                                        new DeviceInfo(
                                                newIP,
                                                mTimesheet.getDeviceInfo().getDevicePort()
                                        )
                                );

                    Node source = (Node) event.getSource();
                    Stage stage = (Stage) source.getScene().getWindow();
                    stage.close();

                } else {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setHeaderText("Error");
                    alert.setContentText("Invalid IP address.");
                    alert.showAndWait();
                }
            }
        });
    }

    public void initData(EmployeeTimesheet timesheet){
        if(timesheet == null) return;
        mTimesheet = timesheet;
        lblName.setText(
                timesheet.getFirstName() +
                        " " +
                        timesheet.getLastName()
        );
        if(timesheet.getDeviceInfo() != null){
            lblNotRegistered.setVisible(false);
            tfDeviceIp.textProperty().setValue(
                    timesheet.getDeviceInfo().getDeviceIP()
            );
            deviceIP = timesheet.getDeviceInfo().getDeviceIP();
        } else {
            lblNotRegistered.setVisible(true);
        }
    }



}
