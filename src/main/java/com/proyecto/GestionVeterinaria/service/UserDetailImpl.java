package com.proyecto.GestionVeterinaria.service;

import com.proyecto.GestionVeterinaria.persistence.entity.Usuario;
import com.proyecto.GestionVeterinaria.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserDetailImpl implements UserDetailsService {
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Usuario usuario = usuarioRepository.findByUsername(username).orElseThrow(
                () ->new UsernameNotFoundException("no se encontró el username")
        );

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        usuario.getRoles().stream()
                .forEach(roles -> authorities.add(
                        new SimpleGrantedAuthority("ROLE_".concat(roles.getRol().name()))
                ));
        usuario.getRoles().stream()
                .flatMap(roles -> roles.getPermisos().stream())
                .forEach( permisos -> authorities.add(
                        new SimpleGrantedAuthority(permisos.getPermisos().name())
                ));

        return new User(
                usuario.getUsername(),
                usuario.getPassword(),
                usuario.isEnabled(),
                usuario.isAccountNonExpired(),
                usuario.isCredentialsNonExpired(),
                usuario.isAccountNonLocked(),
                authorities
        );
    }
}
