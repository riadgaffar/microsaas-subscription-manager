package skyvangaurd.subscription.models;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  // Find a user by email
  Optional<User> findByEmail(String email);

  // Check if an email already exists in the database
  boolean existsByEmail(String email);

  // Custom query to find users with subscriptions expiring soon - useful for
  // sending reminders
  @Query("SELECT u FROM User u JOIN u.subscriptions s WHERE s.renewalDate BETWEEN :startDate AND :endDate")
  List<User> findUsersWithSubscriptionsExpiringBetween(
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate);
}
