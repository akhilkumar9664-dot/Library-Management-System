package com.library.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT authentication filter — runs once per HTTP request.
 *
 * Flow:
 *   1. Extract the Bearer token from the Authorization header.
 *   2. Parse and validate the JWT.
 *   3. Load UserDetails from MongoDB.
 *   4. If valid, set the authentication in the SecurityContext so that
 *      downstream filters and controllers see an authenticated principal.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // Step 1: Extract token from "Authorization: Bearer <token>" header
            String jwt = extractTokenFromRequest(request);

            if (jwt != null) {
                // Step 2: Parse the username from the token
                String username = jwtUtil.extractUsername(jwt);

                // Step 3: Only authenticate if not already authenticated
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    // Step 4: Validate token against the loaded UserDetails
                    if (jwtUtil.isTokenValid(jwt, userDetails)) {
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,                          // no credentials needed after JWT validation
                                        userDetails.getAuthorities()
                                );
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        // Set the authentication so Spring Security knows the request is authenticated
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            }
        } catch (ExpiredJwtException ex) {
            // Expired token — let the request through unauthenticated (security config will block it)
            logger.warn("JWT token expired: " + ex.getMessage());
        } catch (MalformedJwtException ex) {
            logger.warn("JWT token malformed: " + ex.getMessage());
        } catch (Exception ex) {
            logger.error("JWT filter error: " + ex.getMessage());
        }

        // Continue the filter chain regardless
        filterChain.doFilter(request, response);
    }

    /**
     * Extract the raw JWT from the "Authorization" header.
     * Expected format: "Bearer eyJhbGci..."
     *
     * @return the token string, or null if the header is missing or malformed
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);  // strip "Bearer " prefix
        }
        return null;
    }
}
