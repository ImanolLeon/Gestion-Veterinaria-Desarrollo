package com.proyecto.GestionVeterinaria.security;

import com.proyecto.GestionVeterinaria.service.UserDetailImpl;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

  private final JwtUtils jwtUtils;
  private final UserDetailImpl userDetailImpl;

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    final String authHeader = request.getHeader("Authorization");

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    final String token = authHeader.substring(7);
    try {
      final String username = jwtUtils.extractUsername(token);
      if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        UserDetails userDetails = userDetailImpl.loadUserByUsername(username);
        if (jwtUtils.isTokenValid(token, userDetails)) {
          UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null,
              userDetails.getAuthorities());
          authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
          SecurityContextHolder.getContext().setAuthentication(authToken);
        }
      }
    } catch (JwtException | IllegalArgumentException e) {
      // Token malformado, expirado o con firma inválida → continúa sin autenticar →
      // 401 vía EntryPoint
      log.debug("Token JWT inválido: {}", e.getMessage());
    } catch (Exception e) {
      // Error inesperado del servidor (BD caída, NPE, etc.) → propaga como 500
      log.error("Error interno al procesar autenticación JWT", e);
      throw new ServletException("Error interno al procesar autenticación", e);
    }

    filterChain.doFilter(request, response);
  }
}
