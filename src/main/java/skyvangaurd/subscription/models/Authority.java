package skyvangaurd.subscription.models;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "authorities")
@Getter
@Setter
@NoArgsConstructor
public class Authority implements GrantedAuthority {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @JsonIgnore
  private Long id;

  @Column(nullable = false, unique = true)
  private String name; // Example: ROLE_USER, ROLE_ADMIN

  @ManyToMany(mappedBy = "authorities")
  @JsonIgnore
  private Set<User> users = new HashSet<>();

  @Override
  public String getAuthority() {
    return name;
  }
}
