package skyvangaurd.subscription.security;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import skyvangaurd.subscription.config.TestServiceConfiguration;
import skyvangaurd.subscription.models.Authority;
import skyvangaurd.subscription.models.User;
import skyvangaurd.subscription.serialization.UserDetailsDto;
import skyvangaurd.utils.LogInOutService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = TestServiceConfiguration.class)
@ActiveProfiles("test")
public class AuthenticationIntegrationTest {

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  private LogInOutService logInOutService;

  private User newUser;
  private HttpHeaders headers;
  private String token;

  @BeforeEach
  public void setup() {
    newUser = new User();
    newUser.setEmail("user1@example.com");

    Authority authority1 = new Authority();
    authority1.setName("ROLE_ADMIN");
    newUser.addAuthority(authority1);

    Authority authority2 = new Authority();
    authority2.setName("ROLE_SUPERADMIN");
    newUser.addAuthority(authority2);

    ResponseEntity<String> loginResponse = logInOutService.doLogin(newUser);
    token = loginResponse.getBody();
  }

  @AfterEach
  public void teardown() {
    logInOutService.doLogout(headers);
    headers = null;
    newUser = null;
    token = null;
  }

  private <T> ResponseEntity<T> makeExchange(String url, HttpMethod method, HttpEntity<?> requestEntity,
      Class<T> responseType) {
    return restTemplate.exchange(url, method, requestEntity, responseType);
  }

  @Test
  public void shouldHaveValidToken() {
    newUser.setPassword("changeme");
    headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + token);
    
    assertThat(headers.get("Authorization")).isNotEmpty();
    assertTrue(headers.get("Authorization").get(0).contains("Bearer"));
  }

  @Test
  public void shouldDenyAccessWithBlacklistedToken() {

    newUser.setPassword("changeme");
    headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + token);

    HttpEntity<String> blacklistCheckRequest = new HttpEntity<>(headers);
    ResponseEntity<UserDetailsDto[]> response = makeExchange(
        logInOutService.createURLWithPort("/api/users"),
        HttpMethod.GET,
        blacklistCheckRequest,
        UserDetailsDto[].class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  public void shouldNotLoginWithInvalidCredentials() {
    newUser.setPassword("wrongPassword");
    headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + token);

    assertThat(token).isEqualTo("Incorrect username or password");
  }
}
