package com.proyecto.GestionVeterinaria.security;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Activa @PreAuthorize dentro de los slices @WebMvcTest sin traer el resto de
 * SecurityConfig (filtro JWT, proveedor de autenticación, etc.), que no aplica
 * en tests que usan @WithMockUser.
 */
@TestConfiguration
@EnableMethodSecurity
public class MethodSecurityTestConfig {
}
