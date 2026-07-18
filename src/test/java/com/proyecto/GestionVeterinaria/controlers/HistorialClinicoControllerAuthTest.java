package com.proyecto.GestionVeterinaria.controlers;

import com.proyecto.GestionVeterinaria.dto.historial.HistorialResponseDto;
import com.proyecto.GestionVeterinaria.security.JwtAuthFilter;
import com.proyecto.GestionVeterinaria.security.MethodSecurityTestConfig;
import com.proyecto.GestionVeterinaria.service.CitaSecurityService;
import com.proyecto.GestionVeterinaria.service.HistorialClinicoService;
import com.proyecto.GestionVeterinaria.service.HistorialSecurityService;
import com.proyecto.GestionVeterinaria.service.MascotaSecurityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = HistorialClinicoController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthFilter.class))
@Import(MethodSecurityTestConfig.class)
class HistorialClinicoControllerAuthTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private HistorialClinicoService historialClinicoService;

  @MockitoBean(name = "mascotaSecurityService")
  private MascotaSecurityService mascotaSecurityService;

  @MockitoBean(name = "citaSecurityService")
  private CitaSecurityService citaSecurityService;

  @MockitoBean(name = "historialSecurityService")
  private HistorialSecurityService historialSecurityService;

  private static final String HISTORIAL_CREATE_JSON = """
      {"citaId":10,"mascotaId":1,"diagnostico":"d","tratamiento":"t","observaciones":"o"}
      """;

  private static final String HISTORIAL_UPDATE_JSON = """
      {"diagnostico":"d2","tratamiento":"t2","observaciones":"o2"}
      """;

  private HistorialResponseDto respuesta() {
    return new HistorialResponseDto(1L, 1L, "Firulais", 10L, "d", "t", "o", LocalDate.now(), "Vet Uno", "Consulta",
        10.0);
  }

  private Authentication principal(String username, String role) {
    return new UsernamePasswordAuthenticationToken(username, "pw",
        AuthorityUtils.createAuthorityList("ROLE_" + role));
  }

  @Test
  @WithMockUser(username = "cliente1", roles = "CLIENTE")
  void clienteDuenio_puedeVerHistorialDeSuMascota() throws Exception {
    when(mascotaSecurityService.isOwner(1L, "cliente1")).thenReturn(true);
    when(historialClinicoService.findByMascota(1L)).thenReturn(List.of(respuesta()));

    mockMvc.perform(get("/api/mascotas/1/historial"))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(username = "otro", roles = "CLIENTE")
  void clienteNoDuenio_noPuedeVerHistorialDeMascotaAjena() throws Exception {
    when(mascotaSecurityService.isOwner(1L, "otro")).thenReturn(false);

    mockMvc.perform(get("/api/mascotas/1/historial"))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(username = "vet1", roles = "VETERINARIO")
  void veterinarioAsignado_puedeCrearHistorial() throws Exception {
    when(citaSecurityService.isAssignedVet(10L, "vet1")).thenReturn(true);
    when(historialClinicoService.create(any())).thenReturn(respuesta());

    mockMvc.perform(post("/api/historial").with(csrf())
            .principal(principal("vet1", "VETERINARIO"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(HISTORIAL_CREATE_JSON))
        .andExpect(status().isCreated());
  }

  @Test
  @WithMockUser(username = "vet2", roles = "VETERINARIO")
  void veterinarioNoAsignado_noPuedeCrearHistorial() throws Exception {
    when(citaSecurityService.isAssignedVet(10L, "vet2")).thenReturn(false);

    mockMvc.perform(post("/api/historial").with(csrf())
            .principal(principal("vet2", "VETERINARIO"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(HISTORIAL_CREATE_JSON))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(username = "vet1", roles = "VETERINARIO")
  void veterinarioAsignado_puedeEditarHistorial() throws Exception {
    when(historialSecurityService.isAssignedVet(1L, "vet1")).thenReturn(true);
    when(historialClinicoService.update(eq(1L), any())).thenReturn(respuesta());

    mockMvc.perform(put("/api/historial/1").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(HISTORIAL_UPDATE_JSON))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(username = "vet2", roles = "VETERINARIO")
  void veterinarioNoAsignado_noPuedeEditarHistorial() throws Exception {
    when(historialSecurityService.isAssignedVet(1L, "vet2")).thenReturn(false);

    mockMvc.perform(put("/api/historial/1").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(HISTORIAL_UPDATE_JSON))
        .andExpect(status().isForbidden());
  }
}
