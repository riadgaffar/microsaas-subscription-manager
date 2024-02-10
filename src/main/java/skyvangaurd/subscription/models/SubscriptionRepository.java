package skyvangaurd.subscription.models;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

  @Query("SELECT s FROM Subscription s WHERE s.id = :subscriptionId AND s.user.id = :userId")
  Optional<Subscription> findByIdAndUserId(Long subscriptionId, Long userId);
}
