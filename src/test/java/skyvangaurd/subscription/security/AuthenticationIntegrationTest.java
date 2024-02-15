package skyvangaurd.subscription.security;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import skyvangaurd.subscription.models.Authority;
import skyvangaurd.subscription.models.User;
import skyvangaurd.subscription.serialization.UserDetailsDto;
import skyvangaurd.subscription.serialization.UserRegistrationDto;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
public class AuthenticationIntegrationTest {

  @Autowired
  private TestRestTemplate restTemplate;

  @LocalServerPort
  private int port;

  private User newUser;

  @BeforeAll
  public void setup() {
    newUser = new User();
    newUser.setEmail("user1@example.com");
    Authority authority = new Authority();
    authority.setName("ROLE_ADMIN");
    newUser.addAuthority(authority);
  }

  @Test
  public void shouldLoginWithValidUserAndReceiveJwtTokenAndSuccessfullyLogout() {

    newUser.setPassword("changeme");
    ResponseEntity<String> loginResponse = doLogin();

    assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(loginResponse.getBody()).isNotNull();

    ResponseEntity<String> logoutResponse = doLogout(loginResponse);

    assertThat(logoutResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  public void testAccessDeniedWithBlacklistedToken() {
    newUser.setPassword("changeme");
    ResponseEntity<String> loginResponse = doLogin();

    assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(loginResponse.getBody()).isNotNull();

    ResponseEntity<String> logoutResponse = doLogout(loginResponse);

    assertThat(logoutResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

    ResponseEntity<UserDetailsDto[]> response = restTemplate.getForEntity(createURLWithPort("/api/users"), UserDetailsDto[].class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  public void shouldNotLoginWithInalidUserCredentialAndReceiveJwtToken() {

    String url = createURLWithPort("/api/login");

    newUser.setPassword("invalid");

    UserRegistrationDto user = convertToUserDto(newUser);
    HttpHeaders headers = new HttpHeaders();
    HttpEntity<UserRegistrationDto> request = new HttpEntity<>(user, headers);

    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isEqualTo("Incorrect username or password");
  }

  private ResponseEntity<String> doLogin() {
    String loginUrl = createURLWithPort("/api/login");

    UserRegistrationDto user = convertToUserDto(newUser);
    HttpHeaders headers = new HttpHeaders();
    HttpEntity<UserRegistrationDto> loginRequest = new HttpEntity<>(user, headers);

    return restTemplate.postForEntity(loginUrl, loginRequest, String.class);
  }

  private ResponseEntity<String> doLogout(ResponseEntity<String> loginResponse) {
    String logoutUrl = createURLWithPort("/api/logout");

    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + loginResponse.getBody());
    HttpEntity<String> logoutRequest = new HttpEntity<>(null, headers);
    return restTemplate.postForEntity(logoutUrl, logoutRequest, String.class);
  }

  /**
   * 
   * Converts User to UserDetailsDto
   */
  private UserRegistrationDto convertToUserDto(User user) {

    UserRegistrationDto userRegistrationDto = new UserRegistrationDto(
        user.getEmail(),
        user.getPassword(),
        user.getAuthorities().stream().toList());
    return userRegistrationDto;
  }

  private String createURLWithPort(String uri) {
    return "http://localhost:" + port + uri;
  }

}
