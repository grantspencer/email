package com.grasp.email.repository;

import com.grasp.email.model.Email;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Component
public class InMemoryEmailRepository implements EmailRepository {
    private static final Map<BigInteger, Map<BigInteger, Email>> userEmails = new ConcurrentHashMap<>();
    private AtomicReference<BigInteger> nextEmailId = new AtomicReference<>(BigInteger.ONE);


    private Collection<Email> getAllForUserId(BigInteger userId) {
        return userEmails.getOrDefault(userId, Collections.emptyMap()).values();
    }

    @Override
    public List<Email> getInboxForUserId(BigInteger userId) {
        return getAllForUserId(userId)
                .stream()
                .filter(v -> Email.INBOX.equals(v.getFolder()))
                .collect(Collectors.toList());
    }

    @Override
    public Email getEmailForUserIdAndEmailId(BigInteger userId, BigInteger emailId) {
        return getAllForUserId(userId).stream()
                .filter(v -> emailId.equals(v.getId()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Email saveEmail(Email email) {
        validate(email);
        userEmails.computeIfAbsent(email.getUserId(), v -> new ConcurrentHashMap<>())
                .put(email.getId(), email);
        return email;
    }

    private void validate(Email email) {
        if (email == null) {
            throw new NullPointerException("No email supplied");
        }
        if (email.getUserId() == null) {
            throw new IllegalArgumentException("No user supplied with email");
        }
        if (email.getFolder() == null) {
            throw new IllegalArgumentException("No folder supplied with email");
        }

        if (email.getId() == null) {
            email.setId(nextEmailId.getAndAccumulate(BigInteger.ONE, BigInteger::add));
        }
    }
}
