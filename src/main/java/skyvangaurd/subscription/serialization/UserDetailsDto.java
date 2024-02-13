package skyvangaurd.subscription.serialization;

import java.util.List;

import skyvangaurd.subscription.models.Authority;

public record UserDetailsDto(
    Long id,
    String email,
    List<AuthorityDto> authorities,
    List<SubscriptionDto> subscriptions) {
}
