package skyvangaurd.subscription.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import com.fasterxml.jackson.core.type.TypeReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import com.fasterxml.jackson.databind.ObjectMapper;

@JsonTest
public class UserTests {

  private User user;
  private Set<Subscription> subscriptions = new HashSet<>();

  @Autowired
  private JacksonTester<User> userJson;

  @Autowired
  private ObjectMapper objectMapper;

  @BeforeEach
  public void setUp() {

    Subscription sub1 = new Subscription();
    sub1.setId(123L);
    sub1.setName("Premium Streaming Service");
    sub1.setCost(BigDecimal.valueOf(12.99));
    sub1.setRenewalDate(LocalDate.of(2024, 1, 15));

    user = new User();
    user.setId(123123123L);
    user.setEmail("test@test.com");
    user.setPassword("changeMe");

    sub1.setUser(user);
    subscriptions.add(sub1);
    user.setSubscriptions(subscriptions);
  }

  @Test
  void testGetId() {
    assertEquals(user.getId(), 123123123L);
  }

  @Test
  void testGetEmail() {
    assertEquals(user.getEmail(), "test@test.com");
  }

  @Test
  void testGetPassword() {
    assertEquals(user.getPassword(), "changeMe");
  }

  @Test
  void testGetSubscriptions() {
    assertEquals(user.getSubscriptions().size(), 1);
  }

  @Test
  void userSerializationTest() throws IOException {
    String expectedUserWithSubscription = "{"
      + "\"id\":123123123,"
      + "\"email\":\"test@test.com\","
      + "\"password\":\"changeMe\","
      + "\"subscriptions\":["
      + "{"
      + "\"id\":123,"
      + "\"name\":\"Premium Streaming Service\","
      + "\"cost\":12.99,"
      + "\"renewalDate\":\"2024-01-15\"}]}";

    Map<String, Object> expectedUserJson = objectMapper.readValue(
      expectedUserWithSubscription, new TypeReference<Map<String, Object>>() {});

    String actualUserWithSubscription = userJson.write(user).getJson();
    Map<String, Object> actualUserJson = objectMapper.readValue(
                objectMapper.readTree(actualUserWithSubscription).toString(),
                new TypeReference<Map<String, Object>>() {}
        );

    assertThat(actualUserJson).isEqualTo(expectedUserJson);
  }
}
