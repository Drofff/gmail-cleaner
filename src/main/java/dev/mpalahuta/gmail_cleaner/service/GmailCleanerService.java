package dev.mpalahuta.gmail_cleaner.service;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.BatchModifyMessagesRequest;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class GmailCleanerService {

    private static final Logger log = LoggerFactory.getLogger(GmailCleanerService.class);

    private static final int CLEANER_WORKER_POOL_SIZE = 100;
    private static final long CLEAN_BATCH_SIZE = 100;

    private static final String GMAIL_USER_ME = "me";
    private static final String GMAIL_LABEL_UNREAD = "UNREAD";

    private final ExecutorService executorService = Executors.newFixedThreadPool(CLEANER_WORKER_POOL_SIZE);
    private final Set<String> inProgressTracker = new ConcurrentSkipListSet<>();

    public boolean isRunning(String requestId) {
        return this.inProgressTracker.contains(requestId);
    }

    public void cleanAll(String requestId, Gmail gmailClient) {
        this.executorService.submit(() -> this.cleanAllInternal(requestId, gmailClient));
    }

    private void cleanAllInternal(String requestId, Gmail gmailClient) {
        this.inProgressTracker.add(requestId);

        try {
            while (true) {
                final boolean hasMoreBatches = cleanBatch(gmailClient);
                log.info("cleaned a batch (hasMore={})", hasMoreBatches);
                if (!hasMoreBatches) {
                    return;
                }
            }
        } finally {
            this.inProgressTracker.remove(requestId);
        }
    }

    private BatchModifyMessagesRequest batchMarkAsRead(List<String> messageIds) {
        return new BatchModifyMessagesRequest()
                .setIds(messageIds)
                .setRemoveLabelIds(List.of(GMAIL_LABEL_UNREAD));
    }

    private boolean cleanBatch(Gmail gmailClient) {
        Gmail.Users.Messages.List messageListRequest;
        try {
            messageListRequest = gmailClient.users().messages().list(GMAIL_USER_ME);
        } catch (IOException e) {
            throw new RuntimeException("failed to create list messages request", e);
        }

        messageListRequest.setMaxResults(CLEAN_BATCH_SIZE);
        messageListRequest.setLabelIds(List.of(GMAIL_LABEL_UNREAD));

        ListMessagesResponse response;
        try {
            response = messageListRequest.execute();
        } catch (IOException e) {
            throw new RuntimeException("failed to execute list messages request", e);
        }

        if (response.getMessages() == null || response.getMessages().isEmpty()) {
            return false;
        }

        final var hasMoreBatches = response.getMessages().size() >= CLEAN_BATCH_SIZE;

        final var messageIds = response.getMessages().stream().map(Message::getId).toList();

        try {
            gmailClient.users().messages()
                    .batchModify(GMAIL_USER_ME, batchMarkAsRead(messageIds))
                    .execute();
        } catch (IOException e) {
            throw new RuntimeException("failed to execute batch request", e);
        }

        return hasMoreBatches;
    }

}
