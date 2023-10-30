package com.grasp.email.controller;

import com.grasp.email.exception.ResourceNotFoundException;
import com.grasp.email.model.Email;
import com.grasp.email.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
class EmailController {
    @Autowired
    private EmailService service;


    /**
     * Get all emails for the user from their inbox
     * @param userId The user identifier
     * @return A list of all emails
     */
    @GetMapping("/v1/users/{userId}/emails/inbox")
    public List<Email> getInbox(@PathVariable("userId") BigInteger userId) {
        return service.getInbox(userId);
    }

    /**
     * Get all emails for the user from their inbox
     * @param userId The user identifier
     * @param emailId The email identifier
     * @return The requested email, or <code>null</code> if it doesn't exist
     */
    @GetMapping("/v1/users/{userId}/emails/{emailId}")
    public Email getById(@PathVariable("userId") BigInteger userId, @PathVariable("emailId") BigInteger emailId) {
        Email email = service.get(userId, emailId);
        if (email == null) {
            throw new ResourceNotFoundException();
        }
        return email;
    }

    /**
     * Save the submitted email in the drafts folder and allocates it a unique identifier
     * @param userId The user identifier
     * @param email The draft email
     * @return The draft email, post save
     */
    @PostMapping("/v1/users/{userId}/emails/drafts")
    @ResponseStatus(HttpStatus.CREATED)
    public Email createDraft(@PathVariable("userId") BigInteger userId, @RequestBody Email email) {
        return service.createDraft(userId, email);
    }

    /**
     * Save the submitted email in the drafts folder and allocates it a unique identifier
     * @param userId The user identifier
     * @param email The draft email
     * @return The draft email, post save
     */
    @PutMapping("/v1/users/{userId}/emails/drafts")
    @ResponseStatus(HttpStatus.OK)
    public void updateDraft(@PathVariable("userId") BigInteger userId, @RequestBody Email email) {
        service.updateDraft(userId, email);
    }

    /**
     * Send the email to the recipients and move to the sent folder. Either draft email will be updated,
     * or a new email will be created
     * @param userId The user identifier
     * @param email The email
     * @return The modified email
     */
    @PostMapping("/v1/users/{userId}/emails")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Email> send(@PathVariable("userId") BigInteger userId, @RequestBody Email email) {
        HttpStatus status = email.getId() == null? HttpStatus.CREATED : HttpStatus.OK;
        Email updatedEmail = service.send(userId, email);
        return new ResponseEntity<>(updatedEmail, status);
    }
}
