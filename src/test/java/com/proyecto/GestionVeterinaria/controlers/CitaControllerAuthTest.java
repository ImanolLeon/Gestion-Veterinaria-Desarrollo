package com.proyecto.GestionVeterinaria.controlers;

import com.proyecto.GestionVeterinaria.dto.cita.CitaResponseDto;
import com.proyecto.GestionVeterinaria.persistence.enumerates.Estado;
import com.proyecto.GestionVeterinaria.security.JwtAuthFilter;
import com.proyecto.GestionVeterinaria.security.MethodSecurityTestConfig;
import com.proyecto.GestionVeterinaria.service.CitaSecurityService;
import com.proyecto.GestionVeterinaria.service.CitaService;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CitaController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthFilter.class))
@Import(MethodSecurityTestConfig.class)
class CitaControllerAuthTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private CitaService citaService;

  @MockitoBean(name = "citaSecurityService")
  private CitaSecurityService citaSecurityService;

  @MockitoBean(name = "mascotaSecurityService")
  private MascotaSecurityService mascotaSecurityService;

  private static final String CITA_JSON = """
      {"mascotaId":1,"veterinarioId":2,"servicioId":3,"fechaHora":"2030-01-01T10:00:00","notas":"n"}
      """;

  private CitaResponseDto respuesta() {
    return new CitaResponseDto(1L, LocalDateTime.now().plusDays(1), Estado.PENDIENTE, null,
        1L, "Firulais", 2L, "Vet Uno", 3L, "Consulta", 50.0, null, null, false, null, null);
  }

  private Authentication principal(String username, String role) {
    return new UsernamePasswordAuthenticationToken(username, "pw",
        AuthorityUtils.createAuthorityList("ROLE_" + role));
  }

  @Test
  @WithMockUser(username = "admin1", roles = "ADMIN")
  void adminPuedeListarTodasLasCitas() throws Exception {
    when(citaService.findAll()).thenReturn(List.of(respuesta()));

    mockMvc.perform(get("/api/citas").with(csrf())
            .principal(principal("admin1", "ADMIN")))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(username = "vet1", roles = "VETERINARIO")
  void veterinarioListaSoloSusCitas() throws Exception {
    when(citaSecurityService.resolveVeterinarioId("vet1")).thenReturn(Optional.of(2L));
    when(citaService.findByVeterinarioId(2L)).thenReturn(List.of(respuesta()));

    mockMvc.perform(get("/api/citas").with(csrf())
            .principal(principal("vet1", "VETERINARIO")))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(username = "cliente1", roles = "CLIENTE")
  void clienteNoPuedeListarCitasGenerales() throws Exception {
    mockMvc.perform(get("/api/citas").with(csrf())
            .principal(principal("cliente1", "CLIENTE")))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(username = "cliente1", roles = "CLIENTE")
  void clienteDuenio_puedeAgendarCitaParaSuMascota() throws Exception {
    when(mascotaSecurityService.isOwner(1L, "cliente1")).thenReturn(true);
    when(citaService.agendar(any())).thenReturn(respuesta());

    mockMvc.perform(post("/api/citas").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(CITA_JSON))
        .andExpect(status().isCreated());
  }

  @Test
  @WithMockUser(username = "otro", roles = "CLIENTE")
  void clienteNoDuenio_noPuedeAgendarCitaParaMascotaAjena() throws Exception {
    when(mascotaSecurityService.isOwner(1L, "otro")).thenReturn(false);

    mockMvc.perform(post("/api/citas").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(CITA_JSON))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(username = "cliente1", roles = "CLIENTE")
  void clienteDuenio_puedeCancelarSuCita() throws Exception {
    when(citaSecurityService.isOwnerOfCita(1L, "cliente1")).thenReturn(true);

    mockMvc.perform(delete("/api/citas/1").with(csrf())
            .principal(principal("cliente1", "CLIENTE")))
        .andExpect(status().isNoContent());
  }

  @Test
  @WithMockUser(username = "otro", roles = "CLIENTE")
  void clienteNoDuenio_noPuedeCancelarCitaAjena() throws Exception {
    when(citaSecurityService.isOwnerOfCita(1L, "otro")).thenReturn(false);

    mockMvc.perform(delete("/api/citas/1").with(csrf()))
        .andExpect(status().isForbidden());
  }
}
