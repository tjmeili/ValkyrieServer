package valkyrie.server.ui.listview.scheduler;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

import java.io.IOException;

public class SchedulerListView {

    public SchedulerListView() {
    }

    public Node createListView(){
        Node root = null;
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "list_view_scheduler.fxml"
                    )
            );
            root = loader.load();

            SchedulerListViewController controller =
                    loader.<SchedulerListViewController>getController();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return root;
    }
}

