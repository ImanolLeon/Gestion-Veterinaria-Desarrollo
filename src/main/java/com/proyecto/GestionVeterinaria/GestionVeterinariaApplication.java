package com.proyecto.GestionVeterinaria;

import com.proyecto.GestionVeterinaria.persistence.entity.PermisosEntity;
import com.proyecto.GestionVeterinaria.persistence.entity.RolesEntity;
import com.proyecto.GestionVeterinaria.persistence.entity.Usuario;
import com.proyecto.GestionVeterinaria.persistence.enumerates.Permisos;
import com.proyecto.GestionVeterinaria.persistence.enumerates.Rol;
import com.proyecto.GestionVeterinaria.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.util.Set;

@SpringBootApplication
public class GestionVeterinariaApplication {

	public static void main(String[] args) {
		SpringApplication.run(GestionVeterinariaApplication.class, args);
	}


	@Bean
	public CommandLineRunner commandLineRunner(UsuarioRepository usuarioRepository){
		return  args -> {

			RolesEntity admin = RolesEntity.builder()
					.rol(Rol.ADMIN)
					.permisos(
							Set.of(PermisosEntity.builder().permisos(Permisos.CREAR).build(),
									PermisosEntity.builder().permisos(Permisos.ACTUALIZAR).build())
					)
					.build();

			Usuario usuario = Usuario.builder()
					.email("admin@admin.com")
					.activo(true)
					.dateRegister(LocalDate.now())
					.password(new BCryptPasswordEncoder().encode("admin"))
					.username("admin")
					.roles(Set.of(admin))
					.isEnabled(true)
					.isAccountNonExpired(true)
					.isAccountNonLocked(true)
					.isCredentialsNonExpired(true)
					.build();

			usuarioRepository.save(usuario);
		};
	}
}
