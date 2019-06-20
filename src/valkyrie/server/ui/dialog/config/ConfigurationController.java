package valkyrie.server.ui.dialog.config;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import valkyrie.server.local.data.config.ServerConfig;
import valkyrie.server.notification.DeviceNotificationHandler;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ConfigurationController {

    @FXML
    private TextField tfExcelDirectory;

    @FXML
    private Button buttonExcelFileExplorer;

    @FXML
    private TextField tfServerPort;

    @FXML
    private Button buttonConfigOk;

    @FXML
    private Button buttonConfigCancel;
    @FXML
    TextField tfUnsafeWorkdayEmailRecipient;

    private StringProperty stringPropertyDirectory;
    private IntegerProperty integerPropertyPort;

    public void initialize(){
        ServerConfig serverConfig = ServerConfig.getInstance();
        stringPropertyDirectory = new SimpleStringProperty(serverConfig.getExcelPath());
        integerPropertyPort = new SimpleIntegerProperty(serverConfig.getServerPort());
        tfExcelDirectory.textProperty().bindBidirectional(stringPropertyDirectory);
        tfServerPort.textProperty().setValue(integerPropertyPort.getValue() + "");
        tfUnsafeWorkdayEmailRecipient.textProperty().setValue(serverConfig.getInjuryEmailRecipient());


        buttonConfigOk.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                boolean dirUpdated = false, portUpdated = false;

                int port = Integer.parseInt(tfServerPort.textProperty().getValue());
                File directory = new File(stringPropertyDirectory.getValue());
                if (!directory.getAbsolutePath().equals(ServerConfig.getInstance().getExcelPath())) {
                    if(directory.isDirectory()){
                        ServerConfig.getInstance().setExcelPath(directory.getAbsolutePath());
                        dirUpdated = true;
                    } else{
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setHeaderText("Error");
                        alert.setContentText("Invalid directory.");
                        alert.showAndWait();
                        return;
                    }
                }

                if (port != ServerConfig.getInstance().getServerPort()) {
                    if(isPortAvailable(port)){
                        ServerConfig.getInstance().setServerPort(port);
                        portUpdated = true;
                    } else {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setHeaderText("Error");
                        alert.setContentText("Port " + port + " is in use. Please enter a different one.");
                        alert.showAndWait();
                        return;
                    }
                }

                if(dirUpdated || portUpdated){
                    new DeviceNotificationHandler().sendServerInfo();
                }

                ServerConfig.getInstance().setInjuryEmailRecipient(tfUnsafeWorkdayEmailRecipient.textProperty().getValue());
                // close stage
                Node source = (Node) event.getSource();
                Stage stage = (Stage) source.getScene().getWindow();
                stage.close();
            }
        });

        buttonConfigCancel.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Node source = (Node) event.getSource();
                Stage stage = (Stage) source.getScene().getWindow();
                stage.close();
            }
        });

        buttonExcelFileExplorer.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                Stage stage = new Stage();
                stage.sizeToScene();
                File selectedDirectory = directoryChooser.showDialog(stage);
                if(selectedDirectory != null){
                    stringPropertyDirectory.setValue(selectedDirectory.getAbsolutePath());
                }
            }
        });
    }

    public void showStage(){
        try {
            Parent root = FXMLLoader.load(getClass().getResource("valkyrie/server/ui/dialog/config/valkyrie_server_config.fxml"));
            Scene consoleScene = new Scene(root);
            Stage configStage = new Stage();
            configStage.setTitle("Valkyrie Server Configuration");
            configStage.setScene(consoleScene);
            configStage.sizeToScene();
            configStage.setResizable(false);
            configStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void okButtonAction(MouseEvent event) {

    }

    @FXML
    void buttonExcelDirectoryChooserAction(MouseEvent event) {

    }

    @FXML
    void cancelButtonAction(MouseEvent event) {

    }

    private static boolean isPortAvailable(int port) {
        Socket socket = null;
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(), port), 1000);
            socket.close();
            System.out.println("Port in use.");
            return false;
        } catch (IOException e) {
            System.out.println("Port available.");
            return true;
        } finally {
            if(socket != null){
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
