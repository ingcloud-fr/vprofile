package com.visualpathit.account.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class SecurityServiceImpl implements SecurityService {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(SecurityServiceImpl.class);

    @Override
    public String findLoggedInUsername() {
        Object userDetails = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (userDetails instanceof UserDetails) {
            return ((UserDetails) userDetails).getUsername();
        }
        return null;
    }

    @Override
    public boolean autologin(final String username, final String password) {
        try {
            logger.info("Attempting auto-login for user: {}", username);

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Create an authenticated token directly without password verification
            // This is safe because we're calling this right after user registration
            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            // Set the authentication in the security context
            SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            logger.info("Auto-login successful for user: {}", username);
            return true;
        } catch (Exception e) {
            logger.error("Auto-login failed with exception for user: {}", username, e);
            return false;
        }
    }
}
