package com.grasp.email.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Email {
    public static final String INBOX = "inbox";
    public static final String DRAFTS = "drafts";
    public static final String SENT = "sent";

    @JsonProperty
    private BigInteger id;
    @JsonProperty
    private BigInteger userId;
    @JsonProperty
    private String folder;
    @JsonProperty
    private EmailAddress sender;
    @JsonProperty
    private List<EmailAddress> recipients;
    @JsonProperty
    private List<EmailAddress> carbonCopies;
    @JsonProperty
    private List<EmailAddress> blindCarbonCopies;
    @JsonProperty
    private String subject;
    @JsonProperty
    private String message;
}
