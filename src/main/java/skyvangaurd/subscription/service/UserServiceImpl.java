package skyvangaurd.subscription.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import skyvangaurd.subscription.models.Subscription;
import skyvangaurd.subscription.models.SubscriptionRepository;
import skyvangaurd.subscription.models.User;
import skyvangaurd.subscription.models.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User registerUser(User user) {
        String duplicateEmailError = String.format("Email: %s already exists", user.getEmail());
        if (existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException(duplicateEmailError);
        }
        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllusers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional
    public void addSubscription(Long userId, Subscription subscription) {
        User user = userRepository.findById(userId).orElseThrow(
            () -> new IllegalArgumentException("User with user id: " + userId + " not found"));
        subscription.setUser(user);
        user.getSubscriptions().add(subscription);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public Subscription updateSubscriptionForUser(Long userId, Long subscriptionId,
            Subscription updatedSubscriptionDetails) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        Subscription subscription = subscriptionRepository.findByIdAndUserId(subscriptionId, user.getId())
                .orElseThrow(
                        () -> new IllegalArgumentException("Subscription not found for the user with id: " + subscriptionId));

        subscription.setName(updatedSubscriptionDetails.getName());
        subscription.setCost(updatedSubscriptionDetails.getCost());
        subscription.setRenewalDate(updatedSubscriptionDetails.getRenewalDate());

        return subscriptionRepository.save(subscription);
    }

    @Override
    @Transactional
    public void deleteSubscriptionForUser(Long userId, Long subscriptionId) {
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        
        Optional<Subscription> subscription = subscriptionRepository.findById(subscriptionId);
        if (!subscription.isPresent() || !subscription.get().getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Subscription not found for the user with id: " + subscriptionId);
        }

        subscriptionRepository.deleteById(subscriptionId);
    }

    @Override
    public List<User> findUsersWithSubscriptionsExpiringBetween(LocalDate startDate, LocalDate endDate) {
        return userRepository.findUsersWithSubscriptionsExpiringBetween(startDate, endDate);
    }
}
