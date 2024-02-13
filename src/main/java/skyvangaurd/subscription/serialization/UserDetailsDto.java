package skyvangaurd.subscription.serialization;

import java.util.List;

import skyvangaurd.subscription.models.Authority;

public record UserDetailsDto(
    String email,
    String password,
    List<Authority> authorities) {
}
