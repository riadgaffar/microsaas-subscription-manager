package skyvangaurd.subscription.serialization;

import java.util.List;

import skyvangaurd.subscription.models.Authority;

public record UserRegistrationDto(
    String email,
    String password,
    List<Authority> authorities) {
}
