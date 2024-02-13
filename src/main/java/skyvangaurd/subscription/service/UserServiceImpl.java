package skyvangaurd.subscription.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;

import skyvangaurd.subscription.models.Authority;
import skyvangaurd.subscription.models.Subscription;
import skyvangaurd.subscription.models.User;
import skyvangaurd.subscription.models.UserRepository;
import skyvangaurd.subscription.repositories.AuthorityRepository;
import skyvangaurd.subscription.repositories.SubscriptionRepository;
import skyvangaurd.subscription.serialization.SubscriptionDto;
import skyvangaurd.subscription.serialization.UserRegistrationDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.HashSet;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    @Transactional(readOnly = true)
    public List<String> getAuthoritiesForUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AccessDeniedException("Forbidden access: " + email));

        return user.getAuthorities().stream()
                .map(Authority::getName) // Assuming Authority entity has getName() method
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public User registerUser(UserRegistrationDto user) {
        if (existsByEmail(user.email())) {
            throw new IllegalArgumentException("User with email: " + user.email() + " already exists");
        }

        User newUser = new User();
        newUser.setEmail(user.email());

        String hashedPassword = passwordEncoder.encode(user.password());
        newUser.setPassword(hashedPassword);

        // Ensure user's authorities are properly managed
        Set<Authority> managedAuthorities = new HashSet<>();
        for (Authority authority : user.authorities()) {
            Authority managedAuthority = authorityRepository.findByName(authority.getName())
                    .orElseGet(() -> authorityRepository.save(authority));
            managedAuthorities.add(managedAuthority);
        }
        // Now we have a Set<Authority> of managed entities
        newUser.setAuthorities(managedAuthorities);

        return userRepository.save(newUser);
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
    public Optional<Subscription> findByUserAndSubscriptionIds(Long userId, Long subscriptionId) {
        return subscriptionRepository.findByIdAndUserId(userId, subscriptionId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Subscription> findAllSubscriptions(Long userId) {
        return subscriptionRepository.findAllByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional
    public void addSubscription(Long userId, SubscriptionDto subscriptionDto) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new IllegalArgumentException("User with user id: " + userId + " not found"));

        Subscription subscription = new Subscription();
        subscription.setName(subscriptionDto.name());
        subscription.setCost(subscriptionDto.cost());
        subscription.setRenewalDate(subscriptionDto.renewalDate());
        subscription.setUser(user);

        subscriptionRepository.save(subscription);
    }

    @Override
    @Transactional
    public Subscription updateSubscriptionForUser(Long userId, Long subscriptionId,
            SubscriptionDto updatedSubscriptionDetails) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        Subscription subscription = subscriptionRepository.findByIdAndUserId(subscriptionId, user.getId())
                .orElseThrow(
                        () -> new IllegalArgumentException("Subscription not found for the user with id: " + userId));

        subscription.setName(updatedSubscriptionDetails.name());
        subscription.setCost(updatedSubscriptionDetails.cost());
        subscription.setRenewalDate(updatedSubscriptionDetails.renewalDate());

        return subscriptionRepository.save(subscription);
    }

    @Override
    @Transactional
    public void deleteSubscriptionForUser(Long userId, Long subscriptionId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        Optional<Subscription> subscription = subscriptionRepository.findById(subscriptionId);
        if (!subscription.isPresent() || !subscription.get().getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException(
                    "Subscription id: " + subscriptionId + " not found for the user with id: " + userId);
        }

        subscriptionRepository.deleteById(subscriptionId);
    }

    @Override
    public List<User> findUsersWithSubscriptionsExpiringBetween(LocalDate startDate, LocalDate endDate) {
        return userRepository.findUsersWithSubscriptionsExpiringBetween(startDate, endDate);
    }
}
