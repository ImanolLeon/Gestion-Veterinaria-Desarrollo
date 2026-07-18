package com.proyecto.GestionVeterinaria.controlers;

import com.proyecto.GestionVeterinaria.dto.mascota.MascotaResponseDto;
import com.proyecto.GestionVeterinaria.persistence.enumerates.Sexo;
import com.proyecto.GestionVeterinaria.security.JwtAuthFilter;
import com.proyecto.GestionVeterinaria.security.MethodSecurityTestConfig;
import com.proyecto.GestionVeterinaria.service.ClienteSecurityService;
import com.proyecto.GestionVeterinaria.service.MascotaSecurityService;
import com.proyecto.GestionVeterinaria.service.MascotaService;
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
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MascotaController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthFilter.class))
@Import(MethodSecurityTestConfig.class)
class MascotaControllerAuthTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private MascotaService mascotaService;

  @MockitoBean(name = "mascotaSecurityService")
  private MascotaSecurityService mascotaSecurityService;

  @MockitoBean(name = "clienteSecurityService")
  private ClienteSecurityService clienteSecurityService;

  private static final String MASCOTA_JSON = """
      {"clienteId":1,"nombre":"Firulais","especie":"Perro","raza":"Labrador","fechaNacimiento":"2020-01-01","pesoKg":10.0}
      """;

  private MascotaResponseDto respuesta() {
    return new MascotaResponseDto(1L, "Firulais", "Perro", "Labrador", LocalDate.now(), 10.0, 1L, "Cliente Uno",
        Sexo.DESCONOCIDO, false);
  }

  @Test
  @WithMockUser(username = "cliente1", roles = "CLIENTE")
  void clienteDuenio_puedeListarSusMascotas() throws Exception {
    when(clienteSecurityService.isOwner(1L, "cliente1")).thenReturn(true);
    when(mascotaService.findByClienteId(1L)).thenReturn(List.of(respuesta()));

    mockMvc.perform(get("/api/clientes/1/mascotas"))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(username = "otro", roles = "CLIENTE")
  void clienteNoDuenio_noPuedeListarMascotasDeOtroCliente() throws Exception {
    when(clienteSecurityService.isOwner(1L, "otro")).thenReturn(false);

    mockMvc.perform(get("/api/clientes/1/mascotas"))
        .andExpect(status().isForbidden());
  }

  private Authentication principal(String username, String role) {
    return new UsernamePasswordAuthenticationToken(username, "pw",
        AuthorityUtils.createAuthorityList("ROLE_" + role));
  }

  @Test
  @WithMockUser(username = "cliente1", roles = "CLIENTE")
  void clientePuedeCrearMascota() throws Exception {
    when(mascotaService.create(any(), anyString(), anyBoolean())).thenReturn(respuesta());

    mockMvc.perform(post("/api/mascotas").with(csrf())
            .principal(principal("cliente1", "CLIENTE"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(MASCOTA_JSON))
        .andExpect(status().isCreated());
  }

  @Test
  @WithMockUser(username = "vet1", roles = "VETERINARIO")
  void veterinarioNoPuedeCrearMascota() throws Exception {
    mockMvc.perform(post("/api/mascotas").with(csrf())
            .principal(principal("vet1", "VETERINARIO"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(MASCOTA_JSON))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(username = "cliente1", roles = "CLIENTE")
  void clienteDuenio_puedeEditarSuMascota() throws Exception {
    when(mascotaSecurityService.isOwner(1L, "cliente1")).thenReturn(true);
    when(mascotaService.update(eq(1L), any())).thenReturn(respuesta());

    mockMvc.perform(put("/api/mascotas/1").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(MASCOTA_JSON))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(username = "otro", roles = "CLIENTE")
  void clienteNoDuenio_noPuedeEditarMascotaAjena() throws Exception {
    when(mascotaSecurityService.isOwner(1L, "otro")).thenReturn(false);

    mockMvc.perform(put("/api/mascotas/1").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(MASCOTA_JSON))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(username = "cliente1", roles = "CLIENTE")
  void clienteDuenio_puedeBorrarSuMascota() throws Exception {
    when(mascotaSecurityService.isOwner(1L, "cliente1")).thenReturn(true);

    mockMvc.perform(delete("/api/mascotas/1").with(csrf()))
        .andExpect(status().isNoContent());
  }

  @Test
  @WithMockUser(username = "otro", roles = "CLIENTE")
  void clienteNoDuenio_noPuedeBorrarMascotaAjena() throws Exception {
    when(mascotaSecurityService.isOwner(1L, "otro")).thenReturn(false);

    mockMvc.perform(delete("/api/mascotas/1").with(csrf()))
        .andExpect(status().isForbidden());
  }
}
