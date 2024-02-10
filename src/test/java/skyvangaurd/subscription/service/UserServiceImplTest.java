package skyvangaurd.subscription.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import skyvangaurd.subscription.models.User;
import skyvangaurd.subscription.models.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private UserServiceImpl userService;

  @Test
  void testRegisterUser() {
    User user = new User();
    user.setEmail("test@example.com");
    user.setPassword("password");

    when(userRepository.save(any(User.class))).thenReturn(user);

    User savedUser = userService.registerUser(user);
    assertNotNull(savedUser);
    assertEquals("test@example.com", savedUser.getEmail());

    // Verify that the userRepository.save() method was called once
    verify(userRepository, times(1)).save(any(User.class));
  }

  @Test
  @Disabled
  void testAddSubscription() {

  }

  @Test
  @Disabled
  void testUpdateSubscriptionForUser() {

  }

  @Test
  @Disabled
  void testDeleteSubscriptionForUser() {
    
  }

  @Test
  void testExistsByEmail() {
    String email = "test@example.com";

    when(userRepository.existsByEmail(email)).thenReturn(true);

    boolean exists = userService.existsByEmail(email);
    assertTrue(exists);

    // Verify interaction
    verify(userRepository, times(1)).existsByEmail(email);
  }

  @Test
  void testFindByEmail() {
    String email = "test@example.com";
    User user = new User();
    user.setEmail(email);

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

    Optional<User> foundUser = userService.findByEmail(email);
    assertTrue(foundUser.isPresent());
    assertEquals(email, foundUser.get().getEmail());

    // Verify interaction
    verify(userRepository, times(1)).findByEmail(email);
  }

  @Test
  @Disabled
  void testFindById() {

  }

  @Test
  void testFindUsersWithSubscriptionsExpiringBetween() {
    // Define the date range
    LocalDate startDate = LocalDate.of(2024, 1, 1);
    LocalDate endDate = LocalDate.of(2024, 12, 31);

    // Create test data
    User user1 = new User(); // Assume User class has a setter for email or a constructor that sets the email
    user1.setEmail("user1@example.com");

    User user2 = new User();
    user2.setEmail("user2@example.com");

    List<User> testUsers = Arrays.asList(user1, user2);

    // Mock the behavior of the userRepository to return the test data
    when(userRepository.findUsersWithSubscriptionsExpiringBetween(any(LocalDate.class), any(LocalDate.class)))
        .thenReturn(testUsers);

    // Execute the service method
    List<User> users = userService.findUsersWithSubscriptionsExpiringBetween(startDate, endDate);

    // Assertions
    assertNotNull(users);
    assertEquals(2, users.size());
    assertEquals("user1@example.com", users.get(0).getEmail());
    assertEquals("user2@example.com", users.get(1).getEmail());

    // Verify that the repository method was called with the correct parameters
    verify(userRepository, times(1)).findUsersWithSubscriptionsExpiringBetween(startDate, endDate);
  }

  @Test
  @Disabled
  void testGetAllusers() {

  }
}
