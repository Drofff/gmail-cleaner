package dev.mpalahuta.gmail_cleaner.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.services.gmail.Gmail;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Service
public class GmailAuthService {

    private final GoogleAuthorizationCodeFlow flow;
    private final String applicationName;

    public GmailAuthService(
            GoogleAuthorizationCodeFlow flow,
            @Value("${spring.application.name}") String applicationName
    ) {
        this.flow = flow;
        this.applicationName = applicationName;
    }

    public String buildAuthorizationUrl(String redirectUri, String state) {
        return flow.newAuthorizationUrl()
                .setRedirectUri(redirectUri)
                .setState(state)
                .build();
    }

    public void exchangeCode(String userId, String code, String redirectUri) throws IOException {
        GoogleTokenResponse tokenResponse = flow.newTokenRequest(code)
                .setRedirectUri(redirectUri)
                .execute();
        flow.createAndStoreCredential(tokenResponse, userId);
    }

    public Optional<Credential> loadCredential(String userId) throws IOException {
        return Optional.ofNullable(flow.loadCredential(userId));
    }

    public Optional<Gmail> gmailClient(String userId) throws IOException {
        return loadCredential(userId).map(credential ->
                new Gmail.Builder(flow.getTransport(), flow.getJsonFactory(), credential)
                        .setApplicationName(applicationName)
                        .build());
    }

    public void revoke(String userId) throws IOException {
        flow.getCredentialDataStore().delete(userId);
    }
}
