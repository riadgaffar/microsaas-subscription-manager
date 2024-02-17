package skyvangaurd.subscription.client;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import net.minidev.json.JSONArray;
import skyvangaurd.subscription.config.TestServiceConfiguration;
import skyvangaurd.subscription.models.Authority;
import skyvangaurd.subscription.models.Subscription;
import skyvangaurd.subscription.models.User;
import skyvangaurd.subscription.serialization.UserDetailsDto;
import skyvangaurd.utils.LogInOutService;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = TestServiceConfiguration.class)
@ActiveProfiles("test")
public class UserClientTests {

  @LocalServerPort
  private int port;

  @Autowired
  TestRestTemplate restTemplate;

  @Autowired
  private LogInOutService logInOutService;

  private User userFromDb;
  private HttpHeaders headers;
  private String token;

  @BeforeEach
  public void setup() {
    userFromDb = new User();
    userFromDb.setEmail("user1@example.com");
    userFromDb.setPassword("changeme");

    ResponseEntity<String> loginResponse = logInOutService.doLogin(userFromDb);
    token = loginResponse.getBody();

    headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + token);
  }

  @AfterEach
  public void teardown() {
    userFromDb = null;
    logInOutService.doLogout(headers);
    headers = null;
    token = null;

    // Delay for 1000 ms to slow down token initialization
    try {
      Thread.sleep(800);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt(); // Restore interrupted status
      throw new RuntimeException(e); // Optional: rethrow as unchecked exception
    }
  }

  // Overload for POST request with a body
  private ResponseEntity<Void> makeExchange(String url, User user, HttpHeaders headers) {
    return restTemplate.postForEntity(logInOutService.createURLWithPort(url), new HttpEntity<>(user, headers),
        Void.class);
  }

  // Overload for GET requests (or other methods without a body)
  private <T> ResponseEntity<T> makeExchange(String url, HttpMethod method, Class<T> responseType,
      Object... uriVariables) {
    return restTemplate.exchange(logInOutService.createURLWithPort(url), method, null, responseType, uriVariables);
  }

  // Overload for PUT requests with RequestEntity and URI variables
  private <T> ResponseEntity<T> makeExchange(
      String url,
      HttpMethod method,
      HttpEntity<?> requestEntity,
      Class<T> responseType,
      Object... uriVariables) {
    return restTemplate.exchange(logInOutService.createURLWithPort(url), method, requestEntity, responseType,
        uriVariables);
  }

  @Test
  void shouldReturn403ForInvalidUserAuthorities() {
    String url = "/api/authorities?email=user@user.com";
    ResponseEntity<String> responseEntity = makeExchange(
        url,
        HttpMethod.GET,
        String.class);

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldReturnAuthoritiesRolesForAdminAndSuperAdminValidUser() {
    String url = "/api/authorities?email=" + userFromDb.getEmail();

    HttpEntity<String> entity = new HttpEntity<>(null, headers);

    ResponseEntity<String[]> authorities = makeExchange(
        url,
        HttpMethod.GET,
        entity,
        String[].class);

    assertThat(authorities.getBody()).contains("ROLE_SUPERADMIN");
  }

  @Test
  void shouldReturnAValidUserDetails() {
    String url = "/api/users/0";

    HttpEntity<String> entity = new HttpEntity<>(null, headers);

    ResponseEntity<String> responseEntity = makeExchange(
        url,
        HttpMethod.GET,
        entity,
        String.class);

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

    DocumentContext documentContext = JsonPath.parse(responseEntity.getBody());
    Number id = documentContext.read("$.id");
    assertThat(id).isEqualTo(0);

    String email = documentContext.read("$.email");
    assertThat(email).isEqualTo("user1@example.com");

    JSONArray subscriptions = documentContext.read("$.subscriptions");
    assertThat(subscriptions.size()).isEqualTo(4);
  }

  @Test
  void shouldNotReturnAUserWithAnUnknownId() {
    String url = "/api/users/1000";

    HttpEntity<String> entity = new HttpEntity<>(null, headers);

    ResponseEntity<String> responseEntity = makeExchange(
        url,
        HttpMethod.GET,
        entity,
        String.class);

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(responseEntity.getBody()).isBlank();
  }

  @Test
  void shouldReturnAllUsersWhenDataIsSaved() {
    String url = "/api/users";

    HttpEntity<String> entity = new HttpEntity<>(null, headers);

    ResponseEntity<UserDetailsDto[]> responseEntity = makeExchange(
        url,
        HttpMethod.GET,
        entity,
        UserDetailsDto[].class);

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(responseEntity.getBody()).isNotNull();
    assertThat(responseEntity.getBody().length).isGreaterThanOrEqualTo(0);
  }

  @Test
  void shouldReturnASubscriptionForAUser() {

    String url = "/api/users/{userId}/subscriptions/{subscriptionId}";

    HttpEntity<String> entity = new HttpEntity<>(null, headers);

    ResponseEntity<Subscription> responseEntity = makeExchange(
        url,
        HttpMethod.GET,
        entity,
        Subscription.class,
        0, 1);

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(responseEntity.getBody()).isNotNull();
  }

  @Test
  void shouldNotReturnASubscriptionForAUserWithAnUnknownSubscriptionId() {
    String url = "/api/users/0/subscriptions/1000";

    HttpEntity<String> entity = new HttpEntity<>(null, headers);

    ResponseEntity<UserDetailsDto[]> responseEntity = makeExchange(
        url,
        HttpMethod.GET,
        entity,
        UserDetailsDto[].class);

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(responseEntity.getBody()).isNull();
    ;
    ;
  }

  @Test
  void shouldReturnAllSubscriptionForAUser() {
    String url = "/api/users/{userId}/subscriptions";

    HttpEntity<String> entity = new HttpEntity<>(null, headers);

    ResponseEntity<Subscription[]> responseEntity = makeExchange(
        url,
        HttpMethod.GET,
        entity,
        Subscription[].class,
        0);

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(responseEntity.getBody()).isNotNull();
  }

  @Test
  void shouldRegisterANewUserAndAddANewSubscription() {
    String url = logInOutService.createURLWithPort("/api/users");

    User newUser = new User();
    newUser.setEmail("john.doe@here.com");
    newUser.setPassword("changeme");

    Authority authority = new Authority();
    authority.setName("ROLE_ADMIN");
    newUser.addAuthority(authority);

    URI newUserLocation = restTemplate
        .postForLocation(url, new HttpEntity<User>(newUser, headers), newUser);

    User retrievedUser = restTemplate
        .exchange(newUserLocation, HttpMethod.GET, new HttpEntity<>(headers), User.class).getBody();

    assertThat(retrievedUser.getEmail()).isEqualTo(newUser.getEmail());

    Subscription newSubscription = new Subscription();
    newSubscription.setName("Ultimate Streaming Service");
    newSubscription.setCost(BigDecimal.valueOf(17.99));
    newSubscription.setRenewalDate(LocalDate.of(2024, 6, 15));

    String addSubscriptionUrl = String.format(
        logInOutService.createURLWithPort("/api/users/%d/subscription"),
        retrievedUser.getId());

    URI newUserSubscriptionLocation = restTemplate
        .postForLocation(
            addSubscriptionUrl,
            new HttpEntity<>(newSubscription, headers),
            newSubscription,
            retrievedUser.getId());

    assertThat(newUserSubscriptionLocation).isNotNull();
  }

  @Test
  void shouldNotRegisterANewUserWithDuplicateEmail() {
    String url = "/api/users";

    User newUser = new User();
    newUser.setEmail("user1@example.com");
    newUser.setPassword("changeme");

    Authority authority = new Authority();
    authority.setName("ROLE_ADMIN");
    newUser.addAuthority(authority);

    ResponseEntity<Void> registerResponse = makeExchange(url, newUser, headers);

    assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void shouldUpdateAnExistingUserSubscription() {
    String url = "/api/users/{userId}/subscriptions/{subscriptionId}";
    ResponseEntity<String> loginResponse = logInOutService.doLogin(userFromDb);
    headers.set("Authorization", "Bearer " + loginResponse.getBody());
    headers.setContentType(MediaType.APPLICATION_JSON);

    // Prepare the updated subscription details
    Subscription updatedSubscription = new Subscription();
    updatedSubscription.setName("Updated Streaming Service");
    updatedSubscription.setCost(BigDecimal.valueOf(3.99));
    updatedSubscription.setRenewalDate(LocalDate.of(2024, 7, 15));

    // Create the request entity
    HttpEntity<Subscription> requestEntity = new HttpEntity<>(updatedSubscription, headers);

    // Execute the PUT request
    ResponseEntity<Subscription> responseEntity = makeExchange(
        url,
        HttpMethod.PUT,
        requestEntity,
        Subscription.class,
        0, // Assuming these are the URI variable values to replace {id}
        1);

    // Verify the response status and body
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(responseEntity.getHeaders()).isNotNull();

    // Extract JsonPath location
    DocumentContext documentContext = JsonPath.parse(responseEntity.getHeaders());
    String updatedSubscriptionLocation = documentContext.read("$.location[0]");
    assertThat(updatedSubscriptionLocation).isNotNull();
  }

  @Test
  void shouldAddAndDeleteASubscriptionForAnExistingUser() {
    String url = "/api/users/3/subscription";
    headers.setContentType(MediaType.APPLICATION_JSON);

    Subscription newSubscription = new Subscription();
    newSubscription.setName("Ultimate Streaming Service");
    newSubscription.setCost(BigDecimal.valueOf(17.99));
    newSubscription.setRenewalDate(LocalDate.of(2024, 6, 15));

    // User ID 3 does not have any subscription in the in memory h2 database
    String addSubscriptionUrl = logInOutService.createURLWithPort(url);
    HttpEntity<Subscription> subscriptionRequest = new HttpEntity<>(newSubscription, headers);

    // So we add it for clear test result
    URI userSubscriptionLocation = restTemplate.postForLocation(
        addSubscriptionUrl,
        new HttpEntity<Subscription>(newSubscription, headers),
        subscriptionRequest,
        3);

    assertThat(userSubscriptionLocation).isNotNull();

    restTemplate.exchange(userSubscriptionLocation, HttpMethod.DELETE, new HttpEntity<>(headers), Void.class);

    HttpEntity<Subscription> entity = new HttpEntity<>(newSubscription, headers);

    ResponseEntity<Subscription> responseEntity = makeExchange(
        url,
        HttpMethod.GET,
        entity,
        Subscription.class);

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  @Disabled
  void shouldFindUsersWithSubscriptionsExpiringBetween() {

  }

  @Test
  @Disabled
  void shouldDeleteAUserAndAllAssociatedSubscriptions() {

  }
}
