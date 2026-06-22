package com.waper.waperapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.waper.waperapi.model.User;
import com.waper.waperapi.repository.UserRepository;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class WaperApiApplicationTests {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Test
	void connectsToConfiguredMongoDatabase() {
		Document result = mongoTemplate.executeCommand(new Document("ping", 1));

		assertThat(((Number) result.get("ok")).doubleValue()).isEqualTo(1.0);
		assertThat(mongoTemplate.getDb().getName()).isEqualTo("WaperDB");
	}

	@Test
	void registersAndAuthenticatesARealMongoUserWithoutExposingPassword() throws Exception {
		String suffix = Long.toString(System.nanoTime());
		String email = "codex-" + suffix + "@waper.test";
		String username = "codex_" + suffix;
		String password = "Testing1234";

		try {
			mockMvc.perform(post("/api/users")
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
						{
						  "name": "Codex Test",
						  "username": "%s",
						  "email": "%s",
						  "password": "%s"
						}
						""".formatted(username, email, password)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.email").value(email))
				.andExpect(jsonPath("$.role").value("STUDENT"))
				.andExpect(jsonPath("$.password").doesNotExist());

			User storedUser = userRepository.findByEmailIgnoreCase(email).orElseThrow();
			assertThat(storedUser.getPassword()).startsWith("$2");
			assertThat(passwordEncoder.matches(password, storedUser.getPassword())).isTrue();

			MvcResult loginResult = mockMvc.perform(post("/api/public/auth/token")
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
						{
						  "email": "%s",
						  "password": "%s"
						}
						""".formatted(email, password)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").isNotEmpty())
				.andExpect(jsonPath("$.expiresInSeconds").value(3600))
				.andExpect(jsonPath("$.user.id").value(storedUser.getId()))
				.andExpect(jsonPath("$.user.email").value(email))
				.andExpect(jsonPath("$.user.password").doesNotExist())
				.andReturn();

			String token = new com.fasterxml.jackson.databind.ObjectMapper()
				.readTree(loginResult.getResponse().getContentAsString())
				.get("token")
				.asText();

			mockMvc.perform(get("/api/users/" + storedUser.getId()))
				.andExpect(status().isUnauthorized());

			mockMvc.perform(get("/api/users/" + storedUser.getId())
					.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value(email))
				.andExpect(jsonPath("$.password").doesNotExist());
		} finally {
			userRepository.findByEmailIgnoreCase(email).ifPresent(userRepository::delete);
		}
	}

	@Test
	void rejectsInvalidRegistrationData() throws Exception {
		mockMvc.perform(post("/api/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "name": "Test",
					  "username": "x",
					  "email": "invalid",
					  "password": "123"
					}
					"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").isNotEmpty());
	}
}
