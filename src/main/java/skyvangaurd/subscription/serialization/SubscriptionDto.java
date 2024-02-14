package skyvangaurd.subscription.serialization;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

public record SubscriptionDto(
        Long id,
        String name,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        BigDecimal cost,
        LocalDate renewalDate) {
}
