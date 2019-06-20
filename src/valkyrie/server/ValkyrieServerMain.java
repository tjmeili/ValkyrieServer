package valkyrie.server;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;

public class ValkyrieServerMain extends Application {
    private static final Logger logger = LogManager.getLogger(ValkyrieServerMain.class.getName());

    private boolean firstTime = true;
    private TrayIcon trayIcon;

    public static void main(String[] args) {
        logger.info("\n\n*** NEW LAUNCH ***");
        logger.info("Launching...");
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Platform.setImplicitExit(false);
        createTrayIcon(stage);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/valkyrie/server/valkyrie_server_main.fxml"));
        Parent root = (Parent) loader.load();
        Scene primaryScene = new Scene(root);
        stage.setTitle("Valkyrie");
        stage.getIcons().add(new javafx.scene.image.Image("/image/image_icon-0.png"));
        stage.setScene(primaryScene);
        stage.sizeToScene();
        ((MainController) loader.getController()).setStage(stage);
        stage.hide();
        logger.info("Launch complete.");
    }

    @Override
    public void stop() throws Exception {
        super.stop();
    }

    private void createTrayIcon(final Stage stage) {
        logger.traceEntry();
        if (!SystemTray.isSupported()) {
            System.out.println("System tray not supported.");
            return;
        }
        // get the SystemTray instance
        SystemTray tray = SystemTray.getSystemTray();
        // load an image
        Image image = null;
        try {
            image = ImageIO.read(getClass().getResource("/image/image_icon-0.png"));
        } catch (IOException e) {
            logger.error("Error reading tray image icon.", e);
        }

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                logger.info("Hidding stage");
                hide(stage);
            }
        });
        // create a action listener to listen for default action executed on the tray icon
        final ActionListener closeListener = new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                logger.traceEntry("Exiting Application");
                System.exit(0);
            }
        };

        ActionListener showListener = new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        logger.traceEntry();
                        stage.show();
                        logger.traceExit();
                    }
                });
            }
        };
        // create a popup menu
        PopupMenu popup = new PopupMenu();

        java.awt.MenuItem showItem = new java.awt.MenuItem("Show");
        showItem.addActionListener(showListener);
        popup.add(showItem);

        java.awt.MenuItem closeItem = new java.awt.MenuItem("Close");
        closeItem.addActionListener(closeListener);
        popup.add(closeItem);
        /// ... add other items
        // construct a TrayIcon
        trayIcon = new TrayIcon(image, "Valkyrie", popup);
        // set the TrayIcon properties
        trayIcon.addActionListener(showListener);

        // add the tray image
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.err.println(e);
        }
        logger.traceExit();
    }

    private void showProgramIsMinimizedMsg() {
        if (firstTime) {
            trayIcon.displayMessage("Valkyrie",
                    "Valkyrie has been minimized to system tray.",
                    TrayIcon.MessageType.INFO);
            firstTime = false;
        }
    }

    private void hide(final Stage stage) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (SystemTray.isSupported()) {
                    stage.hide();
                    showProgramIsMinimizedMsg();
                } else {
                    System.exit(0);
                }
            }
        });
    }
}
