package com.huertos.dto;

import com.huertos.model.Genero;
import com.huertos.model.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CrearUsuarioRequest(
        @NotBlank(message = "El nombre completo es obligatorio")
        @Size(max = 120, message = "El nombre completo admite hasta 120 caracteres")
        String nombreCompleto,

        @NotBlank(message = "La cédula es obligatoria")
        @Pattern(regexp = "^[0-9]{10}$", message = "La cédula debe tener 10 dígitos")
        String identificacion,

        @NotNull(message = "La fecha de nacimiento es obligatoria")
        @Past(message = "La fecha de nacimiento debe ser anterior a hoy")
        LocalDate fechaNacimiento,

        @NotNull(message = "El género es obligatorio")
        Genero genero,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "Formato de correo inválido")
        @Size(max = 180, message = "El correo admite hasta 180 caracteres")
        String correo,

        @NotBlank(message = "El teléfono es obligatorio")
        @Pattern(regexp = "^09[0-9]{8}$", message = "El teléfono debe tener 10 dígitos y empezar con 09")
        String telefono,

        @NotBlank(message = "La contraseña es obligatoria")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9]).{12,}$",
                message = "La contraseña debe tener al menos 12 caracteres, incluyendo al menos una mayúscula, "
                        + "una minúscula y un número")
        String password,

        @NotNull(message = "El rol es obligatorio")
        Rol rol) {
}
