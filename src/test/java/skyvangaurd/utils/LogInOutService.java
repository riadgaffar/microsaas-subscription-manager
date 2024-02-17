package skyvangaurd.utils;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import skyvangaurd.subscription.models.User;
import skyvangaurd.subscription.serialization.UserRegistrationDto;

public class LogInOutService {

  private final TestRestTemplate restTemplate;
  private final int port;

  public LogInOutService(TestRestTemplate restTemplate, int port) {
    this.restTemplate = restTemplate;
    this.port = port;
  }

  public ResponseEntity<String> doLogin(User newUser) {
    String loginUrl = createURLWithPort("/api/login");

    UserRegistrationDto user = convertToUserDto(newUser);
    HttpHeaders headers = new HttpHeaders();
    HttpEntity<UserRegistrationDto> loginRequest = new HttpEntity<>(user, headers);

    return restTemplate.postForEntity(loginUrl, loginRequest, String.class);
  }

  public ResponseEntity<String> doLogout(HttpHeaders headers) {
    String logoutUrl = createURLWithPort("/api/logout");

    HttpEntity<String> logoutRequest = new HttpEntity<>(null, headers);
    return restTemplate.postForEntity(logoutUrl, logoutRequest, String.class);
  }

  /**
   * 
   * Converts User to UserDetailsDto
   */
  public UserRegistrationDto convertToUserDto(User user) {

    UserRegistrationDto userRegistrationDto = new UserRegistrationDto(
        user.getEmail(),
        user.getPassword(),
        user.getAuthorities().stream().toList());
    return userRegistrationDto;
  }

  public String createURLWithPort(String uri) {
    return "http://localhost:" + port + uri;
  }
}
