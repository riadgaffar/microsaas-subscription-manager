package skyvangaurd.subscription.client;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import net.minidev.json.JSONArray;
import skyvangaurd.subscription.models.Authority;
import skyvangaurd.subscription.models.Subscription;
import skyvangaurd.subscription.models.User;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserClientTests {

  @Autowired
  TestRestTemplate restTemplate;

  @BeforeAll
  public void setup() {
    restTemplate = restTemplate.withBasicAuth("user1@example.com", "changeme");
  }

  @Test
  void getAuthoritiesForUser_should_return_403_for_user() {
    ResponseEntity<String> responseEntity = restTemplate.getForEntity("/api/authorities?email=user@user.com", String.class);

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void getAuthoritiesForUser_should_return_authorities_for_admin_and_super_admin() {

    String[] authorities = restTemplate.getForObject("/api/authorities?email=user1@example.com", String[].class);
    
    assertThat(authorities.toString().contains("ROLE_ADMIN"));
    assertThat(authorities.toString().contains("ROLE_SUPERADMIN"));
  }

  @Test
  void shouldReturnAUserDetailsWhenDataIsSaved() {
    ResponseEntity<String> response = restTemplate.getForEntity("/api/users/0", String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    DocumentContext documentContext = JsonPath.parse(response.getBody());
    Number id = documentContext.read("$.id");
    assertThat(id).isEqualTo(0);

    String email = documentContext.read("$.email");
    assertThat(email).isEqualTo("user1@example.com");

    JSONArray subscriptions = documentContext.read("$.subscriptions");
    assertThat(subscriptions.size()).isEqualTo(4);
  }

  @Test
  void shouldNotReturnAUserWithAnUnknownId() {
    ResponseEntity<String> response = restTemplate.getForEntity("/api/users/1000", String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).isBlank();
  }

  @Test
  void shouldReturnAllUsersWhenDataIsSaved() {
    ResponseEntity<User[]> response = restTemplate.getForEntity("/api/users", User[].class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().length).isGreaterThanOrEqualTo(0);
  }

  @Test
  void shouldReturnASubscriptionForAUser() {
    String url = "/api/users/{userId}/subscriptions/{subscriptionId}";
    ResponseEntity<Subscription> response = restTemplate.getForEntity(url, Subscription.class, 0, 0);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
  }

  @Test
  void shouldNotReturnASubscriptionForAUserWithAnUnknownSubscriptionId() {
    ResponseEntity<String> response = restTemplate.getForEntity("/api/users/0/subscriptions/1000", String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).isBlank();
  }

  @Test
  void shouldReturnAllSubscriptionForAUser() {
    String url = "/api/users/{userId}/subscriptions";
    ResponseEntity<Subscription[]> response = restTemplate.getForEntity(url, Subscription[].class, 0);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
  }

  @Test
  void shouldRegisterANewUserAndAddANewSubscription() {
    User newUser = new User();
    newUser.setEmail("john.doe@here.com");
    newUser.setPassword("changeme");

    Authority authority = new Authority();
    authority.setName("ROLE_ADMIN");
    newUser.addAuthority(authority);

    URI newUserLocation = restTemplate
        .postForLocation("/api/users", newUser);
    User retrievedUser = restTemplate
        .getForObject(newUserLocation, User.class);
    assertThat(retrievedUser.getEmail()).isEqualTo(newUser.getEmail());

    Subscription newSubscription = new Subscription();
    newSubscription.setName("Ultimate Streaming Service");
    newSubscription.setCost(BigDecimal.valueOf(17.99));
    newSubscription.setRenewalDate(LocalDate.of(2024, 6, 15));

    String addSubscriptionUrl = String.format("/api/users/%d/subscription", retrievedUser.getId());
    URI newUserSubscriptionLocation = restTemplate
        .postForLocation(addSubscriptionUrl, newSubscription, retrievedUser.getId());
    assertThat(newUserSubscriptionLocation).isNotNull();
  }

  @Test
  void shouldUpdateAnExistingUserSubscription() {
    // Prepare the updated subscription details
    Subscription updatedSubscription = new Subscription();
    updatedSubscription.setName("Updated Streaming Service");
    updatedSubscription.setCost(BigDecimal.valueOf(3.99));
    updatedSubscription.setRenewalDate(LocalDate.of(2024, 7, 15));

    // Set headers
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    // Create the request entity
    HttpEntity<Subscription> requestEntity = new HttpEntity<>(updatedSubscription, headers);

    // Define the URL with path variables
    String url = "/api/users/{userId}/subscriptions/{subscriptionId}";

    // Execute the PUT request
    ResponseEntity<Subscription> response = restTemplate.exchange(
        url,
        HttpMethod.PUT,
        requestEntity,
        Subscription.class,
        0,
        1);

    // Verify the response status and body
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getHeaders()).isNotNull();

    // Extract JsonPath location
    DocumentContext documentContext = JsonPath.parse(response.getHeaders());
    String updatedSubscriptionLocation = documentContext.read("$.location[0]");
    assertThat(updatedSubscriptionLocation).isNotNull();
  }

  @Test
  void shouldAddAndDeleteASubscriptionForAnExistingUser() {
    Subscription newSubscription = new Subscription();
    newSubscription.setName("Ultimate Streaming Service");
    newSubscription.setCost(BigDecimal.valueOf(17.99));
    newSubscription.setRenewalDate(LocalDate.of(2024, 6, 15));

    // User ID 3 does not have any subscription in the database
    String addSubscriptionUrl = String.format("/api/users/%d/subscription", 3);
    URI userSubscriptionLocation = restTemplate.postForLocation(addSubscriptionUrl, newSubscription, 3);

    restTemplate.delete(userSubscriptionLocation);

    ResponseEntity<Subscription> response = restTemplate.getForEntity(userSubscriptionLocation, Subscription.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
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
