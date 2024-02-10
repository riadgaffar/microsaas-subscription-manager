package skyvangaurd.subscription.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Map;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

@JsonTest
public class SubscriptionTests {

  private Subscription subscription;

  @Autowired
  private JacksonTester<Subscription> subscriptionJson;

  @Autowired
  private ObjectMapper objectMapper;

  @BeforeEach
  public void setUp() {
    User user = new User();
    user.setId(123123123L);
    user.setPassword("changeme");
    user.setEmail("test@test.com");

    subscription = new Subscription();
    subscription.setId(123L);
    ;
    subscription.setName("Premium Streaming Service");
    subscription.setCost(BigDecimal.valueOf(12.99));
    subscription.setRenewalDate(LocalDate.of(2024, 1, 15));
    subscription.setUser(user);
  }

  @Test
  void testGetId() {
    assertEquals(subscription.getId(), 123L);
  }

  @Test
  void testGetName() {
    assertEquals(subscription.getName(), "Premium Streaming Service");
  }

  @Test
  void testGetCost() {
    assertEquals(subscription.getCost(), BigDecimal.valueOf(12.99));
  }

  @Test
  void testGetRenewalDate() {
    assertEquals(subscription.getRenewalDate(), LocalDate.of(2024, 1, 15));
  }

  @Test
  void testGetUser() {
    assertEquals(subscription.getUser().getId(), 123123123L);
    assertEquals(subscription.getUser().getEmail(), "test@test.com");
  }

  @Test
  void subscriptionSerializationTest() throws IOException {
    String expectedSubscriptionWithUser = "{"
        + "\"id\": 123,"
        + "\"name\": \"Premium Streaming Service\","
        + "\"cost\": 12.99,"
        + "\"renewalDate\": \"2024-01-15\","
        + "\"user\": {"
        + "    \"id\": 123123123,"
        + "    \"email\": \"test@test.com\","
        + "    \"password\": \"changeme\","
        + "    \"subscriptions\": []"
        + "  }"
        + "}";

    Map<String, Object> expectedSubscriptionJson = objectMapper.readValue(
        expectedSubscriptionWithUser, new TypeReference<Map<String, Object>>() {
        });

    String actualUserWithSubscription = subscriptionJson.write(subscription).getJson();
    Map<String, Object> actualSubscriptionJson = objectMapper.readValue(
        objectMapper.readTree(actualUserWithSubscription).toString(),
        new TypeReference<Map<String, Object>>() {
        });

    assertThat(actualSubscriptionJson).isEqualTo(expectedSubscriptionJson);
  }
}
