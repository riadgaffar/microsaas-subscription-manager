package skyvangaurd.subscription.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import skyvangaurd.subscription.models.Authority;

public interface AuthorityRepository extends JpaRepository<Authority, Long> {
  Optional<Authority> findByName(String name);
}
