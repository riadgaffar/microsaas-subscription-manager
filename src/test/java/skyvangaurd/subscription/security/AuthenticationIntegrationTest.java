package skyvangaurd.subscription.security;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import skyvangaurd.subscription.models.Authority;
import skyvangaurd.subscription.models.User;
import skyvangaurd.subscription.serialization.UserRegistrationDto;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AuthenticationIntegrationTest {
  @Autowired
  private TestRestTemplate restTemplate;

  @Test
  public void shouldLoginWithValidUserAndReceiveJwtToken() {
    // Construct the URL to your login endpoint
    String url = "/api/login";

    User newUser = new User();
    newUser.setEmail("user1@example.com");
    newUser.setPassword("changeme");

    Authority authority = new Authority();
    authority.setName("ROLE_ADMIN");
    newUser.addAuthority(authority);

    // Create the request body
    UserRegistrationDto user = convertToUserDto(newUser);
    HttpHeaders headers = new HttpHeaders();
    HttpEntity<UserRegistrationDto> request = new HttpEntity<>(user, headers);

    // Perform the HTTP POST request
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    // Assertions
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).contains("jwt");
  }

  @Test
  public void shouldNotLoginWithInalidUserCredentialAndReceiveJwtToken() {
    // Construct the URL to your login endpoint
    String url = "/api/login";

    User newUser = new User();
    newUser.setEmail("user1@example.com");
    newUser.setPassword("invalid");

    Authority authority = new Authority();
    authority.setName("ROLE_ADMIN");
    newUser.addAuthority(authority);

    // Create the request body
    UserRegistrationDto user = convertToUserDto(newUser);
    HttpHeaders headers = new HttpHeaders();
    HttpEntity<UserRegistrationDto> request = new HttpEntity<>(user, headers);

    // Perform the HTTP POST request
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    // Assertions
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isEqualTo("Incorrect username or password");
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

}
