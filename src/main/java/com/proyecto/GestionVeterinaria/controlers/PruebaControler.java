package com.proyecto.GestionVeterinaria.controlers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/prueba")
@RestController
public class PruebaControler {

    @PreAuthorize("hasRole('ADMIN')")
   @GetMapping("/bienvenidaAdmin")
    public String bienvenida(){
    return "Hola Admin";
}

}
