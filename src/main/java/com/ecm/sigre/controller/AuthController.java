package com.ecm.sigre.controller;

import com.ecm.sigre.dto.LoginRequest;
import com.ecm.sigre.dto.LoginResponse;
import com.ecm.sigre.dto.RegistroRequest;
import com.ecm.sigre.dto.UsuarioResponse;
import com.ecm.sigre.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** Registro de participantes e inicio de sesion. */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticacion", description = "Registro e inicio de sesion")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar participante",
            description = "Crea una cuenta con rol ASISTENTE. 409 si el correo ya existe.")
    public UsuarioResponse registrar(@Valid @RequestBody RegistroRequest datos) {
        return authService.registrar(datos);
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesion",
            description = "Devuelve 'Autenticacion satisfactoria' y los datos del usuario, o 401 si falla.")
    public LoginResponse login(@Valid @RequestBody LoginRequest datos) {
        return authService.iniciarSesion(datos);
    }
}
