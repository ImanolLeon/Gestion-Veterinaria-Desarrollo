package com.proyecto.GestionVeterinaria.controlers;

import com.proyecto.GestionVeterinaria.dto.cliente.ClienteResponseDto;
import com.proyecto.GestionVeterinaria.security.JwtAuthFilter;
import com.proyecto.GestionVeterinaria.security.MethodSecurityTestConfig;
import com.proyecto.GestionVeterinaria.service.ClienteSecurityService;
import com.proyecto.GestionVeterinaria.service.ClienteService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ClienteController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthFilter.class))
@Import(MethodSecurityTestConfig.class)
class ClienteControllerAuthTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private ClienteService clienteService;

  @MockitoBean(name = "clienteSecurityService")
  private ClienteSecurityService clienteSecurityService;

  private static final String CLIENTE_JSON = """
      {"nombres":"Ana","apellidos":"Perez","dni":"12345678","telefono":"999","direccion":"Calle 1"}
      """;

  private ClienteResponseDto respuesta() {
    return new ClienteResponseDto(1L, "Ana", "Perez", "12345678", "999", "Calle 1", "a@a.com", "cliente1");
  }

  private Authentication principal(String username, String role) {
    return new UsernamePasswordAuthenticationToken(username, "pw",
        AuthorityUtils.createAuthorityList("ROLE_" + role));
  }

  @Test
  @WithMockUser(username = "cliente1", roles = "CLIENTE")
  void clienteDuenio_puedeEditarSuPerfil() throws Exception {
    when(clienteSecurityService.isOwner(1L, "cliente1")).thenReturn(true);
    when(clienteService.update(eq(1L), any(), anyBoolean())).thenReturn(respuesta());

    mockMvc.perform(put("/api/clientes/1").with(csrf())
            .principal(principal("cliente1", "CLIENTE"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(CLIENTE_JSON))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(username = "otro", roles = "CLIENTE")
  void clienteNoDuenio_noPuedeEditarPerfilAjeno() throws Exception {
    when(clienteSecurityService.isOwner(1L, "otro")).thenReturn(false);

    mockMvc.perform(put("/api/clientes/1").with(csrf())
            .principal(principal("otro", "CLIENTE"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(CLIENTE_JSON))
        .andExpect(status().isForbidden());
  }
}
