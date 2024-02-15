package skyvangaurd.subscription.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired
  private UserDetailsService userDetailsService;

  @Autowired
  private JwtUtil jwtUtil;

  @Autowired
  private JwtTokenBlacklistService jwtTokenBlacklistService;

  @Override
  protected void doFilterInternal(jakarta.servlet.http.HttpServletRequest request,
      jakarta.servlet.http.HttpServletResponse response, jakarta.servlet.FilterChain filterChain)
      throws jakarta.servlet.ServletException, IOException {

    final String authorizationHeader = request.getHeader("Authorization");

    if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
      String jwt = authorizationHeader.substring(7);
      if (jwtTokenBlacklistService.isTokenBlacklisted(jwt)) {
        logger.info("Blocked blacklisted token");
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token is blacklisted.");
        return;
      }
      authenticateRequest(jwt, request);
    }
    filterChain.doFilter(request, response);
  }

  private void authenticateRequest(String jwt, HttpServletRequest request) {
    try {
      String username = jwtUtil.extractUsername(jwt);
      if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (jwtUtil.tokenIsValid(jwt, userDetails)) {
          UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
              userDetails, null, userDetails.getAuthorities());
          authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
          SecurityContextHolder.getContext().setAuthentication(authentication);
          logger.info("Authenticated user {}", username);
        }
      }
    } catch (Exception e) {
      logger.error("Authentication request for user failed: {}", e.getMessage());
      // Not throwing the exception further allows the filter chain to continue.
      // Depending on security requirements, you may choose to halt the request here.
    }
  }
}
