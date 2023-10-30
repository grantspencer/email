package com.grasp.email.service;

import com.grasp.email.model.Email;
import com.grasp.email.repository.EmailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
public class EmailService {
    @Autowired
    private EmailRepository repository;

    /**
     * Retrieve all emails for the user's inbox from the mail repository
     * @param userId The user identifier
     * @return A list of all emails
     */
    public List<Email> getInbox(BigInteger userId) {
        return repository.getInboxForUserId(userId);
    }

    public Email get(BigInteger userId, BigInteger emailId) {
        return repository.getEmailForUserIdAndEmailId(userId, emailId);
    }

    public Email createDraft(BigInteger userId, Email email) {
        email.setUserId(userId);
        email.setFolder(Email.DRAFTS);
        return repository.saveEmail(email);
    }

    public void updateDraft(BigInteger userId, BigInteger emailId, Email email) {
    }
}
