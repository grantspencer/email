package com.grasp.email.controller;

import com.grasp.email.model.Email;
import com.grasp.email.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

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
     * @return A list of all emails
     */
    @GetMapping("/v1/users/{userId}/emails/{emailId}")
    public Email getById(@PathVariable("userId") BigInteger userId, @PathVariable("emailId") BigInteger emailId) {
        return service.get(userId, emailId);
    }

    @PostMapping("/v1/users/{userId}/emails/drafts")
    @ResponseStatus(HttpStatus.CREATED)
    public Email createDraft(@PathVariable("userId") BigInteger userId, @RequestBody Email email) {
        return service.createDraft(userId, email);
    }

    @PutMapping("/v1/users/{userId}/emails/drafts/{emailId}")
    @ResponseStatus(HttpStatus.OK)
    public void updateDraft(@PathVariable("userId") BigInteger userId, @PathVariable("emailId") BigInteger emailId, @RequestBody Email email) {
        service.updateDraft(userId, emailId, email);
    }
}
