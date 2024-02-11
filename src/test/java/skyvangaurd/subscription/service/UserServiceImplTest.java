package skyvangaurd.subscription.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import skyvangaurd.subscription.models.Subscription;
import skyvangaurd.subscription.models.SubscriptionRepository;
import skyvangaurd.subscription.models.User;
import skyvangaurd.subscription.models.UserRepository;
import java.util.Random;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private SubscriptionRepository subscriptionRepository;

  @InjectMocks
  private UserServiceImpl userService;

  private static final Random random = new Random();

  @BeforeEach
  void init_mocks() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testExistsByEmail() {
    List<User> testUsers = generateUsers(1);

    when(userRepository.existsByEmail(testUsers.get(0).getEmail())).thenReturn(true);

    boolean exists = userService.existsByEmail(testUsers.get(0).getEmail());
    assertTrue(exists);

    // Verify interaction
    verify(userRepository, times(1)).existsByEmail(testUsers.get(0).getEmail());
  }

  @Test
  void testFindByEmail() {
    List<User> testUsers = generateUsers(1);

    when(userRepository.findByEmail(testUsers.get(0).getEmail())).thenReturn(Optional.of(testUsers.get(0)));

    Optional<User> foundUser = userService.findByEmail(testUsers.get(0).getEmail());
    assertTrue(foundUser.isPresent());
    assertEquals(testUsers.get(0).getEmail(), foundUser.get().getEmail());

    // Verify interaction
    verify(userRepository, times(1)).findByEmail(testUsers.get(0).getEmail());
  }

  @Test
  void testFindById() {
    List<User> testUsers = generateUsers(1);

    when(userRepository.findById(testUsers.get(0).getId())).thenReturn(Optional.of(testUsers.get(0)));

    Optional<User> foundUser = userService.findById(testUsers.get(0).getId());
    assertTrue(foundUser.isPresent());
    assertEquals(testUsers.get(0).getId(), foundUser.get().getId());

    // Verify interaction
    verify(userRepository, times(1)).findById(testUsers.get(0).getId());
  }

  @Test
  void testGetAllUsers() {
    List<User> users = generateUsers(3);

    when(userRepository.findAll()).thenReturn(users);

    userService.getAllUsers();

    // Verify interaction
    verify(userRepository, times(1)).findAll();
  }

  @Test
  void testFindUsersWithSubscriptionsExpiringBetween() {
    // Define the date range
    LocalDate startDate = LocalDate.of(2024, 1, 1);
    LocalDate endDate = LocalDate.of(2024, 12, 31);

    // Create test data
    List<User> testUsers = generateUsers(2);

    // Mock the behavior of the userRepository to return the test data
    when(userRepository.findUsersWithSubscriptionsExpiringBetween(any(LocalDate.class), any(LocalDate.class)))
        .thenReturn(testUsers);

    // Execute the service method
    List<User> users = userService.findUsersWithSubscriptionsExpiringBetween(startDate, endDate);

    // Assertions
    assertNotNull(users);
    assertEquals(2, users.size());
    assertEquals("user_1@example.com", users.get(0).getEmail());
    assertEquals("user_2@example.com", users.get(1).getEmail());

    // Verify that the repository method was called with the correct parameters
    verify(userRepository, times(1)).findUsersWithSubscriptionsExpiringBetween(startDate, endDate);
  }

  @Test
  void testRegisterUser() {
    List<User> testUsers = generateUsers(1);

    when(userRepository.save(any(User.class))).thenReturn(testUsers.get(0));

    User savedUser = userService.registerUser(testUsers.get(0));
    assertNotNull(savedUser);
    assertEquals("user_1@example.com", savedUser.getEmail());

    // Verify that the userRepository.save() method was called once
    verify(userRepository, times(1)).save(any(User.class));
  }

  @Test
  void testAddSubscription() {
    List<User> testUsers = generateUsers(1);

    Subscription subscription = new Subscription(); // Or however subscriptions are created

    when(userRepository.findById(testUsers.get(0).getId())).thenReturn(Optional.of(testUsers.get(0)));
    when(userRepository.save(any(User.class))).thenReturn(testUsers.get(0));

    userService.addSubscription(testUsers.get(0).getId(), subscription);

    verify(userRepository).findById(testUsers.get(0).getId());
    verify(userRepository).save(testUsers.get(0));
  }

  @Test
  void testUpdateSubscriptionForUser() {
    List<User> testUsers = generateUsers(1);

    Long subscriptionId = 10L;

    Subscription oldSubscription = new Subscription();
    Subscription updatedSubscription = new Subscription();

    oldSubscription.setId(subscriptionId);
    updatedSubscription.setId(subscriptionId);

    testUsers.get(0).getSubscriptions().add(oldSubscription);

    when(subscriptionRepository.findByIdAndUserId(subscriptionId, testUsers.get(0).getId()))
        .thenReturn(Optional.of(updatedSubscription));
    when(userRepository.findById(eq(testUsers.get(0).getId()))).thenReturn(Optional.of(testUsers.get(0)));

    userService.updateSubscriptionForUser(testUsers.get(0).getId(), subscriptionId, updatedSubscription);

    verify(userRepository).findById(eq(testUsers.get(0).getId()));
    verify(subscriptionRepository).findByIdAndUserId(subscriptionId, testUsers.get(0).getId()); // Verify
                                                                                                // findByIdAndUserId was
                                                                                                // called
    verify(subscriptionRepository).save(updatedSubscription);
  }

  @Test
  public void deleteSubscriptionForUser_ShouldDeleteSubscription_WhenValidUserAndSubscription() {
    // Arrange
    Long userId = 1L;
    Long subscriptionId = 2L;

    User user = mock(User.class);
    when(user.getId()).thenReturn(userId);

    Subscription subscription = mock(Subscription.class);
    when(subscription.getUser()).thenReturn(user);

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(subscription));

    // Act
    userService.deleteSubscriptionForUser(userId, subscriptionId);

    // Assert
    verify(subscriptionRepository).deleteById(subscriptionId);
  }

  @Test
  public void deleteSubscriptionForUser_ShouldThrowException_WhenUserNotFound() {
    // Arrange
    Long userId = 1L;
    Long subscriptionId = 2L;
    when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

    // Act & Assert
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      userService.deleteSubscriptionForUser(userId, subscriptionId);
    });

    assertEquals("User not found with id: " + userId, exception.getMessage());
    verify(subscriptionRepository, never()).deleteById(anyLong());
  }

  @Test
  public void deleteSubscriptionForUser_ShouldThrowException_WhenSubscriptionNotFoundForUser() {
    // Arrange
    Long userId = 1L;
    Long subscriptionId = 2L;
    User user = mock(User.class);
    Subscription anotherSubscription = mock(Subscription.class);
    User anotherUser = mock(User.class);

    when(user.getId()).thenReturn(userId);
    when(anotherUser.getId()).thenReturn(3L); // Different user ID
    when(anotherSubscription.getUser()).thenReturn(anotherUser);

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(anotherSubscription));

    // Act & Assert
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      userService.deleteSubscriptionForUser(userId, subscriptionId);
    });

    assertEquals("Subscription id: " + subscriptionId + " not found for the user with id: " + userId,
        exception.getMessage());
  }

  private static List<User> generateUsers(int numberOfUsers) {
    List<User> userList = new ArrayList<>();
    for (int i = 1; i <= numberOfUsers; i++) {
      User user = new User();
      long randomId = Math.abs(random.nextLong()) % numberOfUsers + 1; // Limit the max value by numberOfUsers
      user.setId(randomId);
      String email = String.format("user_%d@example.com", i);
      user.setEmail(email);
      user.setPassword("password123");
      userList.add(user);
    }
    return userList;
  }
}
