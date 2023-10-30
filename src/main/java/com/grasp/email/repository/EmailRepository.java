package com.grasp.email.repository;

import com.grasp.email.model.Email;
import lombok.NonNull;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Component
public interface EmailRepository {
    public List<Email> getInboxForUserId(BigInteger userId);

    public Email getEmailForUserIdAndEmailId(BigInteger userId, BigInteger emailId);

    public Email saveEmail(Email email);
}
