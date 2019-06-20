package valkyrie.server;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Paint;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.appender.FileAppender;
import server.data.Day;
import server.data.EmployeeTimesheet;
import valkyrie.server.local.data.DataHolder;
import valkyrie.server.local.data.ExcelWriter;
import valkyrie.server.local.data.FileHandler;
import valkyrie.server.logging.messages.EmployeeLogMessage;
import valkyrie.server.logging.messages.EmployeeStatusMessage;
import valkyrie.server.logging.messages.ScheduleMessage;
import valkyrie.server.notification.DeviceNotificationHandler;
import valkyrie.server.ui.dialog.add.AddEmployeeDialog;
import valkyrie.server.ui.dialog.config.ConfigurationDialog;
import valkyrie.server.ui.dialog.edit.employee.EmployeeDetailsDialog;
import valkyrie.server.ui.dialog.report.SendLogsDialog;
import valkyrie.server.ui.listview.employee.EmployeeStatus;
import valkyrie.server.ui.listview.employee.ListViewItem;
import valkyrie.server.ui.listview.employee.ObservableDay;
import valkyrie.server.ui.listview.scheduler.SchedulerListView;
import valkyrie.server.ui.window.console.ConsoleWindow;

import java.awt.*;
import java.io.*;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;


public class MainController {
    private static final Logger logger = LogManager.getLogger(MainController.class.getName());

    @FXML
    private MenuItem miServerConfig;
    @FXML
    private MenuItem miClose;
    @FXML
    private MenuItem miConsoleWindow;
    @FXML
    private AnchorPane paneEmployees;
    @FXML
    private Button btnBack;
    @FXML
    private TabPane tabPane;
    @FXML
    private ListView<UUID> employeeInfoListView;
    @FXML
    private Button btnAddEmployee;
    @FXML
    private GridPane gridPaneSchedule;
    @FXML
    private AnchorPane paneHome;
    @FXML
    private Button startConnectionButton;
    @FXML
    private Button endConnectionButton;
    @FXML
    private Label lblServerStatus;
    @FXML
    private Button saveDataButton;
    @FXML
    private Button excelButton;
    @FXML
    private Button sendIPButton;
    @FXML
    private Button lateEmployeesButton;
    @FXML
    private Button btnEmployees;
    @FXML
    private Button btnSchedule;
    @FXML
    private Label inputLabel;
    @FXML
    private Button btnSomethingBroke;
    @FXML
    private Button btnLogsDir;

    private ValkyrieServer server;
    private int port = 9696;
    private ExecutorService excelExecutor;
    private Executor consoleExecutor;
    private ObservableMap<UUID, EmployeeStatus> mapData;
    private ObservableList<UUID> listData;
    private ObservableList<ObservableDay> observableSchedule;

    private BooleanProperty bpServerStatus = new SimpleBooleanProperty(false);

    private Stage mStage = null;

    private static String getLoggerFile() {
        org.apache.logging.log4j.core.Logger loggerImpl = (org.apache.logging.log4j.core.Logger) logger;
        Appender appender = loggerImpl.getAppenders().get("File");
        return ((FileAppender) appender).getFileName();
    }

    private void initSystemConsole() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps;
        try {
            ps = new PrintStream(baos, true, "UTF-8");
            System.setOut(ps);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void initialize() {
        logger.traceEntry();
        FileHandler.configureFileDirectory();
        //startServer();

        // stop server before closing application
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                logger.traceEntry();
                if (server != null) {
                    if (!server.isStopped) {
                        server.stop();
                    }
                }
                logger.traceExit();
            }
        }));

        // get the server ip address and display for applications to connect
        try {
            final DatagramSocket socket = new DatagramSocket();
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            String ip = socket.getLocalAddress().getHostAddress();
            inputLabel.setText(ip);
            socket.close();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        // Initialize UI components
        initializeControls();
        initializeCallbacks();
        initializeListView();
        initializeScheduleGridPane();
        initSchedulerDevicesTab();
        startServer();
        //endConnectionButton.setDisable(true);
        //sendIPButton.setDisable(true);
        logger.traceExit();
    }

    private void initSchedulerDevicesTab() {
        SchedulerListView schedulerListView = new SchedulerListView();
        Node schedulerListViewNode = schedulerListView.createListView();

        Tab tab = new Tab();
        tab.setText("Scheduler Devices");
        tab.setContent(schedulerListViewNode);
        tabPane.getTabs().add(1, tab);
    }

    private void initializeMap() {
        mapData = FXCollections.observableHashMap();
        HashMap<UUID, EmployeeTimesheet> employeeTimesheet = DataHolder.getInstance().getEmployeeTimesheetMap();
        for (EmployeeTimesheet t : employeeTimesheet.values()) {
            String name = t.getFirstName() + " " + t.getLastName();
            boolean isClockedIn = t.getPunchOutTime() == null && t.getPunchInTime() != null;
            int currentJobNumber = -1;
            if (t.getJobs().size() > 0) {
                currentJobNumber = t.getJobs().get(t.getJobs().size() - 1).getJobNumber();
            }
            mapData.put(t.getEmployeeID(), new EmployeeStatus(name, isClockedIn, currentJobNumber));
        }
        listData = FXCollections.observableArrayList(mapData.keySet());
        mapData.addListener(new MapChangeListener<UUID, EmployeeStatus>() {
            @Override
            public void onChanged(Change<? extends UUID, ? extends EmployeeStatus> change) {
                if (change.wasAdded()) {
                    listData.add(change.getKey());
                }
                if (change.wasRemoved()) {
                    listData.remove(change.getKey());
                }
            }
        });
    }

    // callbacks for updating UI components when change is made to employee data
    private void initializeCallbacks() {
        initializeMap();
        DataHolder.getInstance().setNewEmployeeCallback(employeeTimesheet -> Platform.runLater(() -> {
            logger.traceEntry(new EmployeeLogMessage(employeeTimesheet));
            mapData.put(employeeTimesheet.getEmployeeID(),
                    new EmployeeStatus(employeeTimesheet.getFirstName() + " " + employeeTimesheet.getLastName(),
                            employeeTimesheet.getPunchOutTime() == null && employeeTimesheet.getPunchInTime() != null)
            );
            logger.traceExit();
        }));
        DataHolder.getInstance().setEmployeeNewJobCallback(employeeTimesheet -> Platform.runLater(() -> {
            logger.traceEntry(new EmployeeLogMessage(employeeTimesheet));
            int jobNumber = employeeTimesheet.getJobs().get(employeeTimesheet.getJobs().size() - 1).getJobNumber();
            mapData.get(employeeTimesheet.getEmployeeID()).setCurrentJobNumber(jobNumber);
            logger.traceExit();
        }));
        DataHolder.getInstance().setEmployeePunchInCallback(employeeTimesheet -> Platform.runLater(() -> {
            logger.traceEntry(new EmployeeLogMessage(employeeTimesheet));
            mapData.get(employeeTimesheet.getEmployeeID()).setClockedIn(true);
            logger.traceExit();
        }));
        DataHolder.getInstance().setEmployeePunchOutCallback(employeeTimesheet -> Platform.runLater(() -> {
            logger.traceEntry(new EmployeeLogMessage(employeeTimesheet));
            mapData.get(employeeTimesheet.getEmployeeID()).setClockedIn(false);
            mapData.get(employeeTimesheet.getEmployeeID()).setCurrentJobNumber(-1);
            logger.traceExit();
        }));
        DataHolder.getInstance().setScheduleCallback(schedule -> Platform.runLater(() -> {
            logger.traceEntry(new ScheduleMessage(schedule));
            updateSchedulePane(schedule);
            logger.traceExit();
        }));
        DataHolder.getInstance().setEmployeeRemovedCallback(uuid -> {
            logger.traceEntry(mapData.get(uuid).getName());
            mapData.remove(uuid);
            logger.traceExit();
        });
    }

    private void initializeScheduleGridPane() {
        ArrayList<Day> schedule = DataHolder.getInstance().getSchedule();
        observableSchedule = FXCollections.observableArrayList();

        for (int i = 0; i < schedule.size(); i++) {
            observableSchedule.add(new ObservableDay(schedule.get(i)));
            Label lblDate = new Label(), lblActive = new Label(), lblStartTime = new Label(), lblEndTime = new Label();

            lblDate.textProperty().bind(observableSchedule.get(i).stringPropertyDateProperty());
            lblActive.textProperty().bind(Bindings.when(observableSchedule.get(i).booleanPropertyIsActiveProperty().isEqualTo(new SimpleBooleanProperty(true))).then("Yes").otherwise("No"));
            lblStartTime.textProperty().bind(observableSchedule.get(i).stringPropertyStartTimeProperty());
            lblEndTime.textProperty().bind(observableSchedule.get(i).stringPropertyEndTimeProperty());
            //lblDate.prefWidthProperty().bind(gridPaneSchedule.getColumnConstraints().get(0).prefWidthProperty());
            lblDate.setTextAlignment(TextAlignment.LEFT);

            gridPaneSchedule.add(lblDate, 0, i + 1);
            gridPaneSchedule.add(lblActive, 1, i + 1);
            gridPaneSchedule.add(lblStartTime, 2, i + 1);
            gridPaneSchedule.add(lblEndTime, 3, i + 1);
        }
        updateSchedulePane(schedule);
    }

    private void initializeControls() {
        startConnectionButton.setOnAction(event -> startServer());
        endConnectionButton.setOnAction(e -> stopServer());

        btnBack.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("resources/icons/round_arrow_back_white_48dp.png"))));
        btnBack.setOnAction(event -> {
            paneHome.setVisible(true);
            paneEmployees.setVisible(false);
        });
        btnEmployees.setOnAction(event -> {
            paneEmployees.setVisible(true);
            paneHome.setVisible(false);
            tabPane.getSelectionModel().select(0);
        });
        btnSchedule.setOnAction(event -> {
            paneEmployees.setVisible(true);
            paneHome.setVisible(false);
            tabPane.getSelectionModel().select(2);
        });
        miServerConfig.setOnAction(event -> {
            showConfig();
        });

        miConsoleWindow.setOnAction(event -> {
            showConsoleWindow();
        });

        btnAddEmployee.setOnAction(event -> {
            showAddEmployeeDialog();
        });
        //btnSomethingBroke.setVisible(false);
        //btnSomethingBroke.setDisable(true);
        btnSomethingBroke.setOnAction(event -> {
            SendLogsDialog dialog = new SendLogsDialog();
            dialog.showSendLogsDialog();
        });
        btnLogsDir.setOnAction(event -> {
            try {
                Desktop.getDesktop().open(new File(getLoggerFile()).getParentFile());
            } catch (IOException e) {
                logger.error("Error opening explorer to logs directory.", e);
            }
        });
        lblServerStatus.textProperty().bind(Bindings.when(bpServerStatus.isEqualTo(new SimpleBooleanProperty(true))).then("Online").otherwise("Offline"));
    }

    private void initializeListView() {
        employeeInfoListView.setItems(listData);
        employeeInfoListView.setCellFactory(new Callback<ListView<UUID>, ListCell<UUID>>() {
            @Override
            public ListCell<UUID> call(ListView<UUID> param) {
                EmployeeStatusListViewCell listViewCell = new EmployeeStatusListViewCell();
                listViewCell.setOnMouseClicked(event -> {
                    // double click on employee opens dialog with selected employee's data
                    if (event.getButton().equals(MouseButton.PRIMARY)) {
                        if (event.getClickCount() == 2) {
                            EmployeeTimesheet selectedEmployeeTimesheet =
                                    DataHolder.getInstance().getEmployeeTimesheetMap().get(
                                            listViewCell.getItem()
                                    );

                            if (selectedEmployeeTimesheet != null) {
                                showEmployeeDetailsDialog(selectedEmployeeTimesheet);

                            }

                        }
                    }
                });
                return listViewCell;
            }
        });
    }

    public void setStage(Stage stage) {
        this.mStage = stage;
    }

    private void showAddEmployeeDialog() {
        AddEmployeeDialog dialog = new AddEmployeeDialog();
        Stage stage = dialog.showAddEmployeeDialog();
        if (mStage != null) {
            stage.setX(mStage.getX() + mStage.getWidth() / 2 - stage.getWidth() / 2);
            stage.setY(mStage.getY() + mStage.getWidth() / 2 - stage.getHeight() / 2);
        }
        stage.show();
    }

    private void showEmployeeDetailsDialog(EmployeeTimesheet timesheet) {
        Stage stage = new EmployeeDetailsDialog().showEmployeeDetailsDialog(timesheet);
        if (mStage != null) {
            stage.setX(mStage.getX() + mStage.getWidth() / 2 - stage.getWidth() / 2);
            stage.setY(mStage.getY() + mStage.getWidth() / 2 - stage.getHeight() / 2);
        }
        stage.showAndWait();
    }

    // Shows a window displaying server log entries
    private void showConsoleWindow() {
        ConsoleWindow consoleWindow = new ConsoleWindow();
        Stage consoleStage = consoleWindow.showConsoleWindow();
        if (mStage != null) {
            consoleStage.setX(mStage.getX() + mStage.getWidth() / 2 - consoleStage.getWidth() / 2);
            consoleStage.setY(mStage.getY() + mStage.getWidth() / 2 - consoleStage.getHeight() / 2);
        }
        consoleStage.show();
    }

    private void startServer() {
        logger.traceEntry();
        if (server == null || server.isStopped) {
            logi("Starting server...");
            server = new ValkyrieServer(port);
            Thread serverThread = new Thread(server);
            serverThread.start();

            bpServerStatus.setValue(true);
            startConnectionButton.setDisable(true);
            endConnectionButton.setDisable(false);
        }
        logger.traceExit();
    }

    private void stopServer() {
        logger.traceEntry();
        server.stop();
        bpServerStatus.setValue(false);
        startConnectionButton.setDisable(false);
        endConnectionButton.setDisable(true);
        logger.traceExit();
    }

    private void showConfig() {
        ConfigurationDialog dialog = new ConfigurationDialog();
        Stage configDialogStage = dialog.showConfigurationDialog();
        if (mStage != null) {
            configDialogStage.setX(mStage.getX() + mStage.getWidth() / 2 - configDialogStage.getWidth() / 2);
            configDialogStage.setY(mStage.getY() + mStage.getWidth() / 2 - configDialogStage.getHeight() / 2);
        }
        configDialogStage.show();
    }

    private void updateSchedulePane(ArrayList<Day> schedule) {
        logger.traceEntry(new ScheduleMessage(schedule));
        for (int i = 0; i < schedule.size(); i++) {
            observableSchedule.get(i).setDay(schedule.get(i));
        }
        logger.traceExit();
    }

    private void generateExcels() {
        //Create Excel
        ExcelWriter.getInstance().generateAllExcels(new ArrayList<>(DataHolder.getInstance().getEmployeeTimesheetMap().values()));
    }


    @FXML
    private void excelButtonClicked() {
        generateExcels();
    }

    @FXML
    private void lateEmployeesButtonClicked() {
        new DeviceNotificationHandler().notifyLateEmployees();
    }

    @FXML
    private void saveDataButtonClicked() {
        DataHolder.getInstance().saveEmployeesAndSchedulers();
    }

    @FXML
    private void sendIP() {
        new DeviceNotificationHandler().sendServerInfo();
    }

    @FXML
    private void closeApplication() {
        stopServer();
        Platform.exit();
        System.exit(0);
    }

    private void logi(String s) {
        logger.info(s);
    }

    private class EmployeeStatusListViewCell extends ListCell<UUID> {

        private ListViewItem listViewItem;

        public EmployeeStatusListViewCell() {
            super();
            listViewItem = new ListViewItem();
        }

        @Override
        protected void updateItem(UUID item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null) {
                logger.info(new EmployeeStatusMessage(mapData.get(item)));
                listViewItem.textPropertyName().bind(mapData.get(item).nameProperty());
                listViewItem.textPropertyJobNumber().bind(Bindings.when(mapData.get(item).currentJobNumberProperty().isNotEqualTo(-1)).then(mapData.get(item).currentJobNumberProperty().asString()).otherwise(""));
                listViewItem.prefWidthProperty().bind(employeeInfoListView.widthProperty().subtract(20));
                listViewItem.fillPropertyClockedInCircle().bind(Bindings.when(mapData.get(item).clockedInProperty().isEqualTo(new SimpleBooleanProperty(true))).then(Paint.valueOf("#4CAF50")).otherwise(Paint.valueOf("#f44336")));
                setGraphic(listViewItem.getBox());
            }

            if (empty) {
                if (listViewItem.textPropertyName().isBound()) {
                    listViewItem.textPropertyName().unbind();
                }
                if (listViewItem.textPropertyJobNumber().isBound()) {
                    listViewItem.textPropertyJobNumber().unbind();
                }
                if (listViewItem.fillPropertyClockedInCircle().isBound()) {
                    listViewItem.fillPropertyClockedInCircle().unbind();
                }
                setGraphic(null);
            }
        }

    }

}
