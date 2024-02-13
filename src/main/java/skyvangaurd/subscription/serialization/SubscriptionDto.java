package skyvangaurd.subscription.serialization;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SubscriptionDto(
    Long id,
    String name,
    BigDecimal cost,
    LocalDate renewalDate) {
}
