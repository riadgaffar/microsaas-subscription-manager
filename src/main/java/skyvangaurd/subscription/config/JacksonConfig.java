package skyvangaurd.subscription.config;

import skyvangaurd.subscription.serialization.AuthorityMixin;
import skyvangaurd.subscription.models.Authority;

import java.util.Map;

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class JacksonConfig {
  @Bean
    public Jackson2ObjectMapperBuilderCustomizer addCustomSerializationSettings() {
        return jacksonObjectMapperBuilder ->
            jacksonObjectMapperBuilder.mixIns(
                Map.of(Authority.class, AuthorityMixin.class)
            );
    }
}
