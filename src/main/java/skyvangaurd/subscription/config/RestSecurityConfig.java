package skyvangaurd.subscription.config;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import skyvangaurd.subscription.security.JwtRequestFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class RestSecurityConfig {

  @Autowired
  private JwtRequestFilter jwtRequestFilter;

  @Bean
  @Primary
  public PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    // @formatter:off
    http.authorizeHttpRequests((authz) -> authz
            .requestMatchers(HttpMethod.GET, "/api/users/**").hasAnyRole("USER", "ADMIN", "SUPERADMIN")
            .requestMatchers(HttpMethod.PUT, "/api/users/**").hasAnyRole("ADMIN", "SUPERADMIN")
            .requestMatchers(HttpMethod.POST, "/api/users/**").hasAnyRole("ADMIN", "SUPERADMIN")
            .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasAnyRole("SUPERADMIN")
            .requestMatchers(HttpMethod.GET, "/api/authorities").hasAnyRole("USER", "ADMIN", "SUPERADMIN")
            .requestMatchers("/api/login").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/users/**").hasAnyRole("USER", "ADMIN", "SUPERADMIN")
            .anyRequest().authenticated())
        .httpBasic(withDefaults())
        .csrf(CsrfConfigurer::disable)
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        // .logout(logout -> logout // should be removed/refactored when JWT is used
        //         .logoutUrl("/logout") // Specifies the logout URL, default is "/logout"
        //         .logoutSuccessUrl("/login?logout") // URL to redirect to after logout
        //         .invalidateHttpSession(true) // Invalidate the session
        //         .deleteCookies("JSESSIONID") // Delete session cookie
        //         // Additional logout configuration as needed
        //     );
    // @formatter:on
    return http.build();
  }
}
