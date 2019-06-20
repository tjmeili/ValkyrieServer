package valkyrie.server.ui.listview.employee;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;


import java.io.IOException;

public class ListViewItem {

    @FXML
    private HBox hBox;

    @FXML
    private Label labelName;

    @FXML
    private Label labelJobNumber;

    @FXML
    private Circle clockedInCircle;

    public ListViewItem() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("list_view_item.fxml"));
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public void setInfo(EmployeeStatus employeeStatus){
        labelName.setText(employeeStatus.getName());
        labelJobNumber.setText(employeeStatus.getCurrentJobNumber() + "");
    }

    public StringProperty textPropertyName(){
        return labelName.textProperty();
    }

    public StringProperty textPropertyJobNumber(){
        return labelJobNumber.textProperty();
    }

    public HBox getBox()
    {
        return hBox;
    }

    public DoubleProperty prefWidthProperty(){
        return hBox.prefWidthProperty();
    }

    public ObjectProperty<Paint> fillPropertyClockedInCircle(){
        return clockedInCircle.fillProperty();
    }

}
