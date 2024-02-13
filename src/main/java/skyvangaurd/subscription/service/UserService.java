package skyvangaurd.subscription.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import skyvangaurd.subscription.models.Subscription;
import skyvangaurd.subscription.models.User;

public interface UserService {

  public List<String> getAuthoritiesForUser(String username);

  public User registerUser(User user);

  public Optional<User> findByEmail(String email);

  public Optional<User> findById(Long id);

  public List<User> getAllUsers();

  public boolean existsByEmail(String email);

  public Optional<Subscription> findByUserAndSubscriptionIds(Long userId, Long subscriptionId);

  public List<Subscription> findAllSubscriptions(Long userId);

  public void addSubscription(Long userId, Subscription subscription);

  public void deleteSubscriptionForUser(Long userId, Long subscriptionId);

  public Subscription updateSubscriptionForUser(Long userId, Long subscriptionId, Subscription updatedSubscriptionDetails);

  List<User> findUsersWithSubscriptionsExpiringBetween(LocalDate startDate, LocalDate endDate);
}
