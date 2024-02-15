package skyvangaurd.subscription.serialization;

import java.util.List;

public record UserDetailsDto(
    Long id,
    String email,
    List<AuthorityDto> authorities,
    List<SubscriptionDto> subscriptions) {
}
