package com.proyecto.GestionVeterinaria.security;

import org.springframework.security.core.Authentication;

public final class AuthUtils {

  private AuthUtils() {
  }

  public static boolean hasRole(Authentication authentication, String role) {
    String authority = "ROLE_" + role;
    return authentication.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals(authority));
  }
}
