package com.grasp.email;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.grasp.email.model.Email;
import com.grasp.email.model.EmailAddress;
import com.grasp.email.service.EmailService;
import io.restassured.internal.common.assertion.Assertion;
import io.restassured.path.json.JsonPath;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@SpringBootTest
@AutoConfigureMockMvc
class EmailApplicationTests {
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private EmailService mockService;

	@Test
	@WithMockUser(username = "admin", roles="ADMIN")
	void givenUserExists_whenUserInboxIsRetrieved_thenReturn3Emails() throws Exception {
		Mockito.when(mockService.getInbox(BigInteger.ONE)).thenReturn(List.of(new Email(), new Email(), new Email()));
		final BigInteger userId = BigInteger.ONE;

		RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/1/emails/inbox").accept(MediaType.APPLICATION_JSON);
		MvcResult result = mockMvc.perform(requestBuilder).andReturn();
		Assert.isTrue(result.getResponse().getStatus() == HttpServletResponse.SC_OK, "Incorrect status " + result.getResponse().getStatus() + " returned");
		Assert.isTrue("application/json".equals(result.getResponse().getContentType()), "Response should be JSON");
		Assert.notNull(result.getResponse().getContentAsString(), "Emails should have been returned");
		JsonPath json = JsonPath.from(result.getResponse().getContentAsString());
		List<Email> emails = json.<List<Email>>get("$");
		Assert.isTrue(emails.size() == 3, "User should return 3 emails");
	}

	@Test
	@WithMockUser(username = "admin", roles="ADMIN")
	void givenUserExists_whenNewUserInboxIsRetrieved_thenReturnNoEmails() throws Exception {
		//Mockito.when(mockService.getInbox(BigInteger.ONE)).thenReturn(Collections.emptyList());
		final BigInteger userId = BigInteger.ONE;

		RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/1/emails/inbox").accept(MediaType.APPLICATION_JSON);
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
	@AutoConfigureMockMvc(addFilters = false)
	void givenUserExists_whenDraftEmailIsSaved_thenReturnDraftIdentifier() throws Exception {
		final BigInteger userId = BigInteger.ONE;

		Email email = createEmail(userId);
		String body =  toJson(email);

		RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/1/emails/drafts")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body)
				.with(csrf());
		MvcResult result = mockMvc.perform(requestBuilder).andReturn();
		Assert.isTrue(result.getResponse().getStatus() == HttpServletResponse.SC_CREATED, "Incorrect status " + result.getResponse().getStatus() + " returned");
		JsonPath json = JsonPath.from(result.getResponse().getContentAsString());
		Map<String, Object> response = json.<Map<String, Object>>get("$");
		Assert.notNull(response.get("id"), "Email should have been allocated an email identifier");
	}

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

	private <T> String toJson(T value) {
		try {
			final ObjectMapper mapper = new ObjectMapper();
			return mapper.writeValueAsString(value);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
