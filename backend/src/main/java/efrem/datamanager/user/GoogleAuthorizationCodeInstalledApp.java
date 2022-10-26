package efrem.datamanager.user;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.extensions.java6.auth.oauth2.VerificationCodeReceiver;
import com.google.api.client.util.Preconditions;
import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GoogleAuthorizationCodeInstalledApp {
    private final AuthorizationCodeFlow flow;
    private final VerificationCodeReceiver receiver;
    private static final Logger LOGGER = Logger.getLogger(com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp.class.getName());
    private final GoogleAuthorizationCodeInstalledApp.Browser browser;

    public GoogleAuthorizationCodeInstalledApp(AuthorizationCodeFlow flow, VerificationCodeReceiver receiver) {
        this(flow, receiver, new GoogleAuthorizationCodeInstalledApp.DefaultBrowser());
    }

    public GoogleAuthorizationCodeInstalledApp(AuthorizationCodeFlow flow, VerificationCodeReceiver receiver, GoogleAuthorizationCodeInstalledApp.Browser browser) {
        this.flow = (AuthorizationCodeFlow)Preconditions.checkNotNull(flow);
        this.receiver = (VerificationCodeReceiver)Preconditions.checkNotNull(receiver);
        this.browser = browser;
    }

    public Credential authorize(String userId) throws IOException {
        Credential var3;
        try {
            Credential credential = this.flow.loadCredential(userId);
            if (credential == null || credential.getRefreshToken() == null && credential.getExpiresInSeconds() != null && credential.getExpiresInSeconds() <= 60L) {
                String redirectUri = this.receiver.getRedirectUri();
                AuthorizationCodeRequestUrl authorizationUrl = this.flow.newAuthorizationUrl().setRedirectUri(redirectUri);
                this.onAuthorization(authorizationUrl);
                String code = this.receiver.waitForCode();
                TokenResponse response = this.flow.newTokenRequest(code).setRedirectUri(redirectUri).execute();
                Credential var7 = this.flow.createAndStoreCredential(response, userId);
                return var7;
            }

            var3 = credential;
        } finally {
            this.receiver.stop();
        }

        return var3;
    }

    protected void onAuthorization(AuthorizationCodeRequestUrl authorizationUrl) throws IOException {
        String url = authorizationUrl.build();
        Preconditions.checkNotNull(url);
        this.browser.browse(url);
    }

    public static void browse(String url) throws IOException {
        Preconditions.checkNotNull(url);
        System.out.println("Please open the following address in your browser:");
        System.out.println("  " + url);

            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Action.BROWSE)) {
                    System.out.println("Attempting to open that address in the default browser now...");
                    desktop.browse(URI.create(url));
                }
            } else {
                Runtime runtime = Runtime.getRuntime();
                runtime.exec("rundll32 url.dll,FileProtocolHandler " + url);
            }

    }

    public final AuthorizationCodeFlow getFlow() {
        return this.flow;
    }

    public final VerificationCodeReceiver getReceiver() {
        return this.receiver;
    }

    public static class DefaultBrowser implements GoogleAuthorizationCodeInstalledApp.Browser {
        public DefaultBrowser() {
        }

        public void browse(String url) throws IOException {
            GoogleAuthorizationCodeInstalledApp.browse(url);
        }
    }

    public interface Browser {
        void browse(String var1) throws IOException;
    }
}
