package com.grasp.email.service;

import com.grasp.email.model.Email;
import com.grasp.email.repository.EmailRepository;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Slf4j
@Service
public class EmailService {
    @Autowired
    private EmailRepository repository;

    /**
     * Retrieve all emails for the user's inbox from the mail repository
     * @param userId The user identifier
     * @return A list of all emails
     */
    public List<Email> getInbox(@NonNull BigInteger userId) {
        return repository.getInboxForUserId(userId);
    }

    public Email get(@NonNull BigInteger userId, @NonNull BigInteger emailId) {
        return repository.getEmailForUserIdAndEmailId(userId, emailId);
    }

    public Email createDraft(@NonNull BigInteger userId, @NonNull Email email) {
        email.setUserId(userId);
        email.setFolder(Email.DRAFTS);
        return repository.saveEmail(email);
    }

    public void updateDraft(@NonNull BigInteger userId, @NonNull Email email) {
        if (email.getId() == null) {
            throw new NullPointerException("Email identifier cannot be null");
        }
        if (!Email.DRAFTS.equals(email.getFolder())) {
            throw new RuntimeException("Email folder must be " + Email.DRAFTS);
        }

        repository.saveEmail(email);
    }

    public Email send(BigInteger userId, Email email) {
        email.setUserId(userId);
        email.setFolder(Email.OUTBOX);
        repository.saveEmail(email);
        log.info("Sending email {}", email);
        email.setFolder(Email.SENT);
        repository.saveEmail(email);
        return email;
    }
}
