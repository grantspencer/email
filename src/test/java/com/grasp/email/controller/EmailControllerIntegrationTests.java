package com.grasp.email.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grasp.email.model.Email;
import com.grasp.email.model.EmailAddress;
import com.grasp.email.repository.EmailRepository;
import com.grasp.email.service.EmailService;
import io.restassured.path.json.JsonPath;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.Assert;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@SpringBootTest
@AutoConfigureMockMvc
class EmailControllerIntegrationTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmailRepository repository;

    @Test
    @WithMockUser(username = "admin", roles="ADMIN")
    void givenUserExists_whenNewUserInboxIsRetrieved_thenReturnNoEmails() throws Exception {
        final BigInteger userId = BigInteger.ONE;

        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/users/1/emails/inbox").accept(MediaType.APPLICATION_JSON);
        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
        Assert.isTrue(result.getResponse().getStatus() == HttpServletResponse.SC_OK, "Incorrect status " + result.getResponse().getStatus() + " returned");
        Assert.isTrue("application/json".equals(result.getResponse().getContentType()), "Response should be JSON");
        Assert.notNull(result.getResponse().getContentAsString(), "Emails should have been returned");
        JsonPath json = JsonPath.from(result.getResponse().getContentAsString());
        List<Email> emails = json.<List<Email>>get("$");
        Assert.isTrue(emails.isEmpty(), "User should have no emails");
    }

    @Test
    @WithMockUser(username = "admin", roles="ADMIN")
    void givenUserExists_whenUserInboxIsRetrieved_thenReturn3Emails() throws Exception {
        final BigInteger userId = BigInteger.valueOf(100L);
        repository.saveEmail(newEmail(createEmail(userId), Email.INBOX));
        repository.saveEmail(newEmail(createEmail(userId), Email.INBOX));
        repository.saveEmail(newEmail(createEmail(userId), Email.INBOX));

        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/users/100/emails/inbox").accept(MediaType.APPLICATION_JSON);
        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
        Assert.isTrue(result.getResponse().getStatus() == HttpServletResponse.SC_OK, "Incorrect status " + result.getResponse().getStatus() + " returned");
        Assert.isTrue("application/json".equals(result.getResponse().getContentType()), "Response should be JSON");
        Assert.notNull(result.getResponse().getContentAsString(), "Emails should have been returned");
        JsonPath json = JsonPath.from(result.getResponse().getContentAsString());
        List<Email> emails = json.<List<Email>>get("$");
        Assert.isTrue(emails.size() == 3, "User should return 3 emails");
    }

    private Email newEmail(Email email, String folder) {
        email.setId(null);
        email.setFolder(folder);
        return email;
    }

    @Test
    @WithMockUser(username = "admin", roles="ADMIN")
    @AutoConfigureMockMvc(addFilters = false)
    void givenUserExists_whenDraftEmailIsSaved_thenReturnDraftIdentifier() throws Exception {
        final BigInteger userId = BigInteger.valueOf(101L);

        Email email = createEmail(userId);
        String body =  toJson(email);

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/users/101/emails/drafts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .with(csrf());
        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
        Assert.isTrue(result.getResponse().getStatus() == HttpServletResponse.SC_CREATED, "Incorrect status " + result.getResponse().getStatus() + " returned");
        JsonPath json = JsonPath.from(result.getResponse().getContentAsString());
        Map<String, Object> response = json.<Map<String, Object>>get("$");
        Assert.notNull(response.get("id"), "Email should have been allocated an email identifier");
    }

    @Test
    @WithMockUser(username = "admin", roles="ADMIN")
    void givenUserDoesntExist_whenEmailRequested_thenResourceNotFound() throws Exception {
        final BigInteger userId = BigInteger.valueOf(102L);

        Email email = createEmail(userId);
        String body =  toJson(email);

        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/users/102/emails/101")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body);
        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
        Assert.isTrue(result.getResponse().getStatus() == HttpServletResponse.SC_NOT_FOUND, "Incorrect status " + result.getResponse().getStatus() + " returned");
    }

    @Test
    @WithMockUser(username = "admin", roles="ADMIN")
    @AutoConfigureMockMvc(addFilters = false)
    void givenUserAndNewEmail_whenEmailSent_thenResourceCreated() throws Exception {
        final BigInteger userId = BigInteger.valueOf(103L);

        Email email = createEmail(userId);
        String body =  toJson(email);

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/users/103/emails")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .with(csrf());
        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
        Assert.isTrue(result.getResponse().getStatus() == HttpServletResponse.SC_CREATED, "Incorrect status " + result.getResponse().getStatus() + " returned");
        JsonPath json = JsonPath.from(result.getResponse().getContentAsString());
        Map<String, Object> response = json.<Map<String, Object>>get("$");
        Assert.notNull(response.get("id"), "Email should have an email identifier");
        Assert.isTrue(Email.SENT.equals(response.get("folder")), "Email should have a folder of " + Email.SENT);
    }

    /**
     * Create a dummy email with required (and some optional) fields populated
     * @param userId User identifier
     * @return New {@link} Email
     */
    private Email createEmail(BigInteger userId) {
        Email email = new Email();
        email.setUserId(userId);
        email.setRecipients(List.of(new EmailAddress("abc@mail.com", "Jane Doe"), new EmailAddress("def@mail.com", null)));
        email.setCarbonCopies(List.of(new EmailAddress("uvw@mail.com", null), new EmailAddress("xyz@mail.com", null)));
        email.setSender(new EmailAddress("me@mail.com", "John Doe"));
        email.setSubject("This is a test");
        email.setMessage("Just testing if I can save this");
        return email;
    }

    /**
     * Convert the POJO to JSON
     * @param value POJO
     * @return The object expressed in JSON
     * @param <T> POJO class
     */
    private <T> String toJson(T value) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
