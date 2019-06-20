package valkyrie.server.local.data.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import valkyrie.server.ValkyrieServer;
import valkyrie.server.local.data.FileHandler;

import java.io.File;
import java.net.URISyntaxException;

// Holds server configuration information
public class ServerConfig {
    private static final Logger logger = LogManager.getLogger(ServerConfig.class.getName());
    private String excelPath = "";
    private int serverPort = 9696;
    private int defaultDevicePort = 9797;
    private String injuryEmailRecipient = "EMAIL_HERE";
    private String injuryEmailSender = "EMAIL_HERE";

    public static ServerConfig getInstance() {
        return SingletonHelper.INSTANCE;
    }

    private static class SingletonHelper {
        private static final ServerConfig INSTANCE = new ServerConfig();
    }

    private ServerConfig() {
        logger.traceEntry();
        FileHandler.ServerConfigData serverConfigData = FileHandler.loadConfigData();
        logger.info(serverConfigData.dataLoaded);
        if (serverConfigData.dataLoaded) {
            this.excelPath = serverConfigData.excelPath;
            this.serverPort = serverConfigData.serverPort;
            this.injuryEmailRecipient = serverConfigData.injuryEmailRecipient;
        } else {
            try {
                excelPath = new File(ValkyrieServer.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile().getAbsolutePath();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        logger.traceExit();
    }

    public String getExcelPath() {
        return excelPath;
    }

    public void setExcelPath(String excelPath) {
        this.excelPath = excelPath;
        FileHandler.saveServerConfig();
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
        FileHandler.saveServerConfig();
    }

    public String getInjuryEmailRecipient() {
        return injuryEmailRecipient;
    }

    public void setInjuryEmailRecipient(String injuryEmailRecipient) {
        this.injuryEmailRecipient = injuryEmailRecipient;
        FileHandler.saveServerConfig();
    }

    public String getInjuryEmailSender() {
        return injuryEmailSender;
    }

    public void setInjuryEmailSender(String injuryEmailSender) {
        this.injuryEmailSender = injuryEmailSender;
    }

    public int getDefaultDevicePort() {
        return defaultDevicePort;
    }
}
