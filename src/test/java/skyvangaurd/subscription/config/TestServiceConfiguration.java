package skyvangaurd.subscription.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

import skyvangaurd.utils.LogInOutService;

@TestConfiguration
public class TestServiceConfiguration {
  @Bean
  @Scope("prototype") // This ensures each injection point gets a new instance
  public LogInOutService logInOutService(TestRestTemplate restTemplate, @Value("${local.server.port}") int port) {
    return new LogInOutService(restTemplate, port);
  }
}
