package valkyrie.server.ui.listview.scheduler;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import valkyrie.server.local.data.DataHolder;



public class SchedulerListViewController {

    @FXML private ListView<String> listView;

    private ObservableList<String> schedulers;

    public void initialize(){
        schedulers = FXCollections.observableArrayList();
        schedulers.addAll(DataHolder.getInstance().getSchedulerDevices().keySet());

        DataHolder.getInstance().setNewSchedulerDeviceCallback(deviceIP -> Platform.runLater(() -> {
            if(!schedulers.contains(deviceIP)){
                schedulers.add(deviceIP);
            }
        }));
        listView.setItems(schedulers);
        listView.setOnMouseClicked(event -> {

        });
    }

    private class ListViewCell extends ListCell<String> {

    }

    private class ListViewItem{

    }
}
