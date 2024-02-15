package skyvangaurd.subscription.config;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.ClientHttpRequestInterceptor;

import skyvangaurd.utils.LoggingRequestInterceptor;

import java.util.ArrayList;
import java.util.List;

@Configuration
@Profile("test")
public class TestRestTemplateConfig {

    @Bean
    public TestRestTemplate testRestTemplate() {
        TestRestTemplate testRestTemplate = new TestRestTemplate();
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>(testRestTemplate.getRestTemplate().getInterceptors());
        interceptors.add(new LoggingRequestInterceptor());
        testRestTemplate.getRestTemplate().setInterceptors(interceptors);
        return testRestTemplate;
    }
}
