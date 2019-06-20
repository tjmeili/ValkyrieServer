package valkyrie.server;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Base64;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.appender.FileAppender;
import valkyrie.server.local.data.FileHandler;
import valkyrie.server.local.data.config.ServerConfig;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

// Emails log entries when an error occurs
// Sends emails to administrators when an employee has an unsafe work day

public class MGmail {
    private static final Logger logger = LogManager.getLogger(MGmail.class.getName());

    private static final String APPLICATION_NAME = "Valkryie Gmail API";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String CREDENTIALS_FOLDER = "resources/credentials"; // Directory to store user credentials.

    private static final List<String> SCOPES = Collections.singletonList(GmailScopes.MAIL_GOOGLE_COM);
    private static final String CLIENT_SECRET_DIR = "resources/client_secret.json"; // Directory for client secret

    private static String emailLogEntries = "DEV_EMAIL";


    public static void sendUnsafeWorkdayEmail(String name, String date) {
        logger.traceEntry(name, date);
        // Build a new authorized API client service.
        try {
            logger.info("Getting Gmail service.");
            Gmail service = getGmailService();
            String user = "me";
            String body = date + ": " + name + " reported an unsafe workday.";
            logger.info("Sending message...");

            MimeMessage mimeMessage = createEmail(ServerConfig.getInstance().getInjuryEmailRecipient(), ServerConfig.getInstance().getInjuryEmailSender(), "Unsafe Workday Report", body);
            Message message = sendMessage(service, user, mimeMessage);

            logger.info(message.toPrettyString());
            logger.info("Message sent.");
        } catch (IOException e) {
            logger.error(e);
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            logger.error(e);
            e.printStackTrace();
        } catch (MessagingException e) {
            logger.error(e);
            e.printStackTrace();
        }
        logger.traceExit();
    }

    public static void sendLogEmail(String body) {
        logger.traceEntry();
        String logPath = "";
        System.out.println(getLoggerFile());
        File logFile = new File(getLoggerFile());
        logger.info(logFile.getAbsolutePath());
        logger.info("Log file exists: " + logFile.exists());

        if (logFile.exists()) {
            try {
                Gmail service = getGmailService();
                MimeMessage mimeMessage = createEmailWithAttachment(emailLogEntries, emailLogEntries, "Valkyrie Problem", body, logFile);
                Message message = sendMessage(service, "me", mimeMessage);
            } catch (IOException e) {
                logger.error(e);
                e.printStackTrace();
            } catch (GeneralSecurityException e) {
                logger.error(e);
                e.printStackTrace();
            } catch (MessagingException e) {
                logger.error(e);
                e.printStackTrace();
            }
        }
        logger.traceExit();
    }

    private static String getLoggerFile() {
        org.apache.logging.log4j.core.Logger loggerImpl = (org.apache.logging.log4j.core.Logger) logger;
        Appender appender = loggerImpl.getAppenders().get("File");
        return ((FileAppender) appender).getFileName();
    }

    private static Gmail getGmailService() throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Gmail service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, MGmail.getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
        return service;
    }

    private static Message createMessageWithEmail(MimeMessage emailContent)
            throws MessagingException, IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        emailContent.writeTo(buffer);
        byte[] bytes = buffer.toByteArray();
        String encodedEmail = Base64.encodeBase64URLSafeString(bytes);
        Message message = new Message();
        message.setRaw(encodedEmail);
        return message;
    }

    private static Message sendMessage(Gmail service,
                                       String userId,
                                       MimeMessage emailContent)
            throws MessagingException, IOException {
        Message message = createMessageWithEmail(emailContent);
        message = service.users().messages().send(userId, message).execute();

        logger.info("Message id: " + message.getId());
        logger.info(message.toPrettyString());
        return message;
    }

    private static MimeMessage createEmail(String to,
                                           String from,
                                           String subject,
                                           String bodyText)
            throws MessagingException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        MimeMessage email = new MimeMessage(session);

        email.setFrom(new InternetAddress(from));
        email.addRecipient(javax.mail.Message.RecipientType.TO,
                new InternetAddress(to));
        email.setSubject(subject);
        email.setText(bodyText);
        return email;
    }

    private static MimeMessage createEmailWithAttachment(String to,
                                                         String from,
                                                         String subject,
                                                         String bodyText,
                                                         File file)
            throws MessagingException, IOException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        MimeMessage email = new MimeMessage(session);

        email.setFrom(new InternetAddress(from));
        email.addRecipient(javax.mail.Message.RecipientType.TO,
                new InternetAddress(to));
        email.setSubject(subject);

        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(bodyText, "text/plain");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(mimeBodyPart);

        mimeBodyPart = new MimeBodyPart();
        DataSource source = new FileDataSource(file);

        mimeBodyPart.setDataHandler(new DataHandler(source));
        mimeBodyPart.setFileName(file.getName());

        multipart.addBodyPart(mimeBodyPart);
        email.setContent(multipart);

        return email;
    }


    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = MGmail.class.getResourceAsStream(CLIENT_SECRET_DIR);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(FileHandler.getPath() + File.separator + CREDENTIALS_FOLDER)))
                .setAccessType("offline")
                .build();
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }

}
