package com.proyecto.GestionVeterinaria.controlers;

import com.proyecto.GestionVeterinaria.dto.reporte.CitasPorVeterinarioDto;
import com.proyecto.GestionVeterinaria.dto.reporte.ServicioPopularDto;
import com.proyecto.GestionVeterinaria.security.JwtAuthFilter;
import com.proyecto.GestionVeterinaria.security.MethodSecurityTestConfig;
import com.proyecto.GestionVeterinaria.service.CitaSecurityService;
import com.proyecto.GestionVeterinaria.service.ReporteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ReporteController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthFilter.class))
@Import(MethodSecurityTestConfig.class)
class ReporteControllerAuthTest {

  private static final String DESDE = "2026-06-13T00:00:00";
  private static final String HASTA = "2026-07-13T23:59:59";

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private ReporteService reporteService;

  @MockitoBean(name = "citaSecurityService")
  private CitaSecurityService citaSecurityService;

  private Authentication principal(String username, String role) {
    return new UsernamePasswordAuthenticationToken(username, "pw",
        AuthorityUtils.createAuthorityList("ROLE_" + role));
  }

  // ---------- /citas-por-veterinario ----------

  @Test
  @WithMockUser(username = "admin1", roles = "ADMIN")
  void adminObtieneTodasLasFilasCuandoNoFiltraPorVeterinario() throws Exception {
    when(reporteService.citasPorVeterinario(isNull(), any(), any())).thenReturn(List.of(
        new CitasPorVeterinarioDto(1L, "Juan Pérez", 12, 9, 2),
        new CitasPorVeterinarioDto(2L, "Ana Gómez", 5, 4, 1)));

    mockMvc.perform(get("/api/reportes/citas-por-veterinario")
            .principal(principal("admin1", "ADMIN"))
            .param("desde", DESDE)
            .param("hasta", HASTA))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].veterinarioId").value(1))
        .andExpect(jsonPath("$[0].veterinarioNombres").value("Juan Pérez"))
        .andExpect(jsonPath("$[0].totalCitas").value(12))
        .andExpect(jsonPath("$[0].citasCompletadas").value(9))
        .andExpect(jsonPath("$[0].citasCanceladas").value(2));
  }

  @Test
  @WithMockUser(username = "admin1", roles = "ADMIN")
  void adminPuedeFiltrarPorCualquierVeterinario() throws Exception {
    when(reporteService.citasPorVeterinario(eq(2L), any(), any())).thenReturn(List.of());

    mockMvc.perform(get("/api/reportes/citas-por-veterinario")
            .principal(principal("admin1", "ADMIN"))
            .param("veterinarioId", "2")
            .param("desde", DESDE)
            .param("hasta", HASTA))
        .andExpect(status().isOk());

    verify(reporteService).citasPorVeterinario(eq(2L), any(), any());
  }

  @Test
  @WithMockUser(username = "vet1", roles = "VETERINARIO")
  void veterinarioSinVeterinarioIdSoloObtieneSuPropiaFila() throws Exception {
    when(citaSecurityService.resolveVeterinarioId("vet1")).thenReturn(Optional.of(5L));
    when(reporteService.citasPorVeterinario(eq(5L), any(), any()))
        .thenReturn(List.of(new CitasPorVeterinarioDto(5L, "Juan Pérez", 12, 9, 2)));

    mockMvc.perform(get("/api/reportes/citas-por-veterinario")
            .principal(principal("vet1", "VETERINARIO"))
            .param("desde", DESDE)
            .param("hasta", HASTA))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].veterinarioId").value(5));

    // El id sale del username del JWT, nunca del query param.
    verify(reporteService).citasPorVeterinario(eq(5L), any(), any());
    verify(reporteService, never()).citasPorVeterinario(isNull(), any(), any());
  }

  @Test
  @WithMockUser(username = "vet1", roles = "VETERINARIO")
  void veterinarioPuedePedirExplicitamenteSuPropioId() throws Exception {
    when(citaSecurityService.resolveVeterinarioId("vet1")).thenReturn(Optional.of(5L));
    when(reporteService.citasPorVeterinario(eq(5L), any(), any())).thenReturn(List.of());

    mockMvc.perform(get("/api/reportes/citas-por-veterinario")
            .principal(principal("vet1", "VETERINARIO"))
            .param("veterinarioId", "5")
            .param("desde", DESDE)
            .param("hasta", HASTA))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(username = "vet1", roles = "VETERINARIO")
  void veterinarioPidiendoIdAjenoObtiene403() throws Exception {
    when(citaSecurityService.resolveVeterinarioId("vet1")).thenReturn(Optional.of(5L));

    mockMvc.perform(get("/api/reportes/citas-por-veterinario")
            .principal(principal("vet1", "VETERINARIO"))
            .param("veterinarioId", "999")
            .param("desde", DESDE)
            .param("hasta", HASTA))
        .andExpect(status().isForbidden());

    verify(reporteService, never()).citasPorVeterinario(any(), any(), any());
  }

  @Test
  @WithMockUser(username = "vet-sin-perfil", roles = "VETERINARIO")
  void veterinarioSinPerfilAsociadoObtieneListaVacia() throws Exception {
    when(citaSecurityService.resolveVeterinarioId("vet-sin-perfil")).thenReturn(Optional.empty());

    mockMvc.perform(get("/api/reportes/citas-por-veterinario")
            .principal(principal("vet-sin-perfil", "VETERINARIO"))
            .param("desde", DESDE)
            .param("hasta", HASTA))
        .andExpect(status().isOk())
        .andExpect(content().json("[]"));

    verify(reporteService, never()).citasPorVeterinario(any(), any(), any());
  }

  @Test
  @WithMockUser(username = "cliente1", roles = "CLIENTE")
  void clienteNoPuedeConsultarCitasPorVeterinario() throws Exception {
    mockMvc.perform(get("/api/reportes/citas-por-veterinario")
            .principal(principal("cliente1", "CLIENTE"))
            .param("desde", DESDE)
            .param("hasta", HASTA))
        .andExpect(status().isForbidden());
  }

  // ---------- /servicios-populares ----------

  @Test
  @WithMockUser(username = "admin1", roles = "ADMIN")
  void adminObtieneRankingSobreTodasLasCitas() throws Exception {
    when(reporteService.serviciosPopulares(isNull(), any(), any()))
        .thenReturn(List.of(new ServicioPopularDto(1L, "Consulta general", 20)));

    mockMvc.perform(get("/api/reportes/servicios-populares")
            .principal(principal("admin1", "ADMIN"))
            .param("desde", DESDE)
            .param("hasta", HASTA))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].servicioId").value(1))
        .andExpect(jsonPath("$[0].servicioNombre").value("Consulta general"))
        .andExpect(jsonPath("$[0].totalCitas").value(20));

    verify(reporteService).serviciosPopulares(isNull(),
        eq(LocalDateTime.parse(DESDE)), eq(LocalDateTime.parse(HASTA)));
  }

  @Test
  @WithMockUser(username = "admin1", roles = "ADMIN")
  void serviciosPopularesSinRangoEsValido() throws Exception {
    when(reporteService.serviciosPopulares(isNull(), isNull(), isNull())).thenReturn(List.of());

    mockMvc.perform(get("/api/reportes/servicios-populares")
            .principal(principal("admin1", "ADMIN")))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(username = "vet1", roles = "VETERINARIO")
  void veterinarioObtieneRankingSoloDeSusCitas() throws Exception {
    when(citaSecurityService.resolveVeterinarioId("vet1")).thenReturn(Optional.of(5L));
    when(reporteService.serviciosPopulares(eq(5L), any(), any()))
        .thenReturn(List.of(new ServicioPopularDto(1L, "Consulta general", 7)));

    mockMvc.perform(get("/api/reportes/servicios-populares")
            .principal(principal("vet1", "VETERINARIO"))
            .param("desde", DESDE)
            .param("hasta", HASTA))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].totalCitas").value(7));

    verify(reporteService).serviciosPopulares(eq(5L), any(), any());
    verify(reporteService, never()).serviciosPopulares(isNull(), any(), any());
  }

  @Test
  @WithMockUser(username = "vet-sin-perfil", roles = "VETERINARIO")
  void veterinarioSinPerfilObtieneRankingVacio() throws Exception {
    when(citaSecurityService.resolveVeterinarioId("vet-sin-perfil")).thenReturn(Optional.empty());

    mockMvc.perform(get("/api/reportes/servicios-populares")
            .principal(principal("vet-sin-perfil", "VETERINARIO")))
        .andExpect(status().isOk())
        .andExpect(content().json("[]"));

    verify(reporteService, never()).serviciosPopulares(any(), any(), any());
  }

  @Test
  @WithMockUser(username = "cliente1", roles = "CLIENTE")
  void clienteNoPuedeConsultarServiciosPopulares() throws Exception {
    mockMvc.perform(get("/api/reportes/servicios-populares")
            .principal(principal("cliente1", "CLIENTE")))
        .andExpect(status().isForbidden());
  }
}
