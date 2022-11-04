package efrem.datamanager.gmail;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Label;
import com.google.api.services.gmail.model.ListLabelsResponse;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.gson.Gson;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/* class to demonstrate use of Gmail list labels API */
public class GmailQuickstart {
    /**
     * Application name.
     */
    private static final String APPLICATION_NAME = "Gmail API Java Quickstart";
    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    /**
     * Directory to store authorization tokens for this application.
     */
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = List.of(GmailScopes.GMAIL_SEND, GmailScopes.GMAIL_METADATA, GmailScopes.GMAIL_LABELS);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
            throws IOException {
        // Load client secrets.
        InputStream in = GmailQuickstart.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8080).build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
        //returns an authorized Credential object.
        return credential;
    }

/*
    public static void main(String... args) throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Gmail service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        // Print the labels in the user's account.
        String user = "me";
        List<Message> labels = getMessageID(service);
        if (labels.isEmpty()) {
            System.out.println("No labels found.");
        } else {
            System.out.println("Messages:");
            for (Message message : labels) {
                Message m1 = service.users().messages().get(user, message.getId()).setFields("payload/headers").execute();
                Stream<String> fromHeaderValue = m1.getPayload().getHeaders().stream()
                        .filter(h -> "From".equals(h.getName())).map(h -> h.getValue());
                String emailAddress = fromHeaderValue.toArray(String[]::new)[0];
                System.out.printf("- %s\n", emailAddress);
            }
        }

    }
//    private static List<Message> getMessageID() throws IOException, GeneralSecurityException {
//        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
//        Gmail service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
//                .setApplicationName(APPLICATION_NAME)
//                .build();
//        ListMessagesResponse response = service.users().messages().list("me").execute();
//
//        List<Message> messages = new ArrayList<Message>();
//        while (response.getMessages() != null) {
//            messages.addAll(response.getMessages());
//            for (Message message : response.getMessages()) {
//                Message m1 = service.users().messages().get("me", message.getId()).setFields("payload/headers").execute();
//                Stream<String> fromHeaderValue = m1.getPayload().getHeaders().stream()
//                        .filter(h -> "From".equals(h.getName())).map(h -> h.getValue());
//                String emailAddress = fromHeaderValue.toArray(String[]::new)[0];
//                System.out.printf("- %s\n", emailAddress);
//            }
//            if (response.getNextPageToken() != null) {
//                String pageToken = response.getNextPageToken();
//                response = service.users().messages().list("me")
//                        .setPageToken(pageToken).execute();
//            } else {
//                break;
//            }
//        }
//        Gson gson = new Gson();
//        String messageID = gson.toJson(messages);
//
//        return messages;
//    }

 */

}