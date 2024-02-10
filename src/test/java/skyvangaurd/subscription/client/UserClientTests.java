package skyvangaurd.subscription.client;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import net.minidev.json.JSONArray;
import skyvangaurd.subscription.models.Subscription;
import skyvangaurd.subscription.models.User;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserClientTests {

  @Autowired
  TestRestTemplate restTemplate;

  @Test
  void shouldReturnAUserDetailsWhenDataIsSaved() {
    ResponseEntity<String> response = restTemplate.getForEntity("/users/0", String.class);
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
    ResponseEntity<String> response = restTemplate.getForEntity("/users/1000", String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).isBlank();
  }

  @Test
  void shouldReturnAllUsersWhenDataIsSaved() {
    ResponseEntity<User[]> response = restTemplate.getForEntity("/users", User[].class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().length).isGreaterThanOrEqualTo(0);
  }

  @Test
  void shouldRegisterANewUserAndAddANewSubscription() {
    User newUser = new User();
    newUser.setEmail("john.doe@here.com");
    newUser.setPassword("password");

    URI newUserLocation = restTemplate.postForLocation("/users", newUser);
    User retrievedUser = restTemplate.getForObject(newUserLocation, User.class);
    assertThat(retrievedUser.getEmail()).isEqualTo(newUser.getEmail());

    Subscription newSubscription = new Subscription();
    newSubscription.setName("Ultimate Streaming Service");
    newSubscription.setCost(BigDecimal.valueOf(17.99));
    newSubscription.setRenewalDate(LocalDate.of(2024, 6, 15));

    String addSubscriptionUrl = String.format("/users/%d/subscription", retrievedUser.getId());
    URI newUserSubscriptionLocation = restTemplate.postForLocation(addSubscriptionUrl, newSubscription, retrievedUser.getId());
    assertThat(newUserSubscriptionLocation).isNotNull();
  }

  @Test
  @Disabled
  void shouldUpdateAnExistingSubscription() {

  }

  @Test
  @Disabled
  void shouldDeleteAnExistingSubscription() {
    
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
