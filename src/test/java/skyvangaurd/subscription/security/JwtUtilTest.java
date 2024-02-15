package skyvangaurd.subscription.security;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.time.Instant;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JwtUtilTest {

  @Autowired
  private JwtUtil jwtUtil;

  @Autowired
	private JwtTokenBlacklistService jwtTokenBlacklistService;

  private UserDetails userDetails;

  @BeforeAll
  public void setup() {
    String username = "user" + (int) (Math.random() * 1000);
    String password = "password" + (int) (Math.random() * 10000);
    SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_USER");
    userDetails = new User(username, password, Arrays.asList(authority));
  }

  @Test
  void shouldGenerateValidToken() {
    String subject = userDetails.getUsername();
    String token = jwtUtil.generateToken(subject);

    assertNotNull(token, "Generated token should not be null");
  }

  @Test
  void shouldValidateToken() {
    String subject = userDetails.getUsername();
    String token = jwtUtil.generateToken(subject);
    assertTrue(jwtUtil.tokenIsValid(token, userDetails));
  }

  @Test
  void shouldExtractUserName() {
    String subject = userDetails.getUsername();
    String token = jwtUtil.generateToken(subject);
    String userName = jwtUtil.extractUsername(token);

    assertThat(userName).isEqualTo(subject);
  }

  @Test
  void shouldConfirmInvalidBlackListedToken() {
    String subject = userDetails.getUsername();
    String token = jwtUtil.generateToken(subject);
    long expiryDate = jwtUtil.extractExpiration(token).getTime();
		jwtTokenBlacklistService.blacklistToken(token, expiryDate);
    
    assertTrue(jwtTokenBlacklistService.isTokenBlacklisted(token));
  }
}
