package dev.mpalahuta.gmail_cleaner.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.MemoryDataStoreFactory;
import com.google.api.services.gmail.GmailScopes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
public class GoogleOAuthConfig {

    @Bean
    public HttpTransport googleHttpTransport() {
        return new NetHttpTransport();
    }

    @Bean
    public JsonFactory googleJsonFactory() {
        return GsonFactory.getDefaultInstance();
    }

    @Bean
    public GoogleClientSecrets googleClientSecrets(
            @Value("${gmail-cleaner.google.client-secrets}") Resource clientSecrets,
            JsonFactory jsonFactory
    ) throws IOException {
        try (Reader reader = new InputStreamReader(clientSecrets.getInputStream(), StandardCharsets.UTF_8)) {
            return GoogleClientSecrets.load(jsonFactory, reader);
        }
    }

    @Bean
    public GoogleAuthorizationCodeFlow googleAuthorizationCodeFlow(
            HttpTransport httpTransport,
            JsonFactory jsonFactory,
            GoogleClientSecrets clientSecrets
    ) throws IOException {
        return new GoogleAuthorizationCodeFlow.Builder(
                httpTransport,
                jsonFactory,
                clientSecrets,
                List.of(GmailScopes.GMAIL_MODIFY)
        )
                .setDataStoreFactory(MemoryDataStoreFactory.getDefaultInstance())
                .setAccessType("offline")
                .build();
    }
}
