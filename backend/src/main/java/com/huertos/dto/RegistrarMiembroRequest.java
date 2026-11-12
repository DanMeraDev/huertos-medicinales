package com.huertos.dto;

import com.huertos.model.Genero;
import com.huertos.model.NivelMiembro;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record RegistrarMiembroRequest(
        @NotBlank(message = "La cédula es obligatoria")
        @Pattern(regexp = "^[0-9]{10}$", message = "La cédula debe tener 10 dígitos numéricos")
        String cedula,

        @NotBlank(message = "Los nombres son obligatorios")
        @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$", message = "Los nombres solo admiten letras y espacios")
        @Size(max = 100, message = "Los nombres no pueden superar 100 caracteres")
        String nombres,

        @NotBlank(message = "Los apellidos son obligatorios")
        @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$", message = "Los apellidos solo admiten letras y espacios")
        @Size(max = 100, message = "Los apellidos no pueden superar 100 caracteres")
        String apellidos,

        @NotNull(message = "La fecha de nacimiento es obligatoria")
        @Past(message = "La fecha de nacimiento debe ser anterior a la actual")
        LocalDate fechaNacimiento,

        @NotNull(message = "El género es obligatorio")
        Genero genero,

        @NotBlank(message = "El teléfono es obligatorio")
        @Pattern(regexp = "^09[0-9]{8}$", message = "El teléfono debe iniciar con 09 y tener 10 dígitos")
        String telefono,

        @NotNull(message = "El nivel es obligatorio")
        NivelMiembro nivel,

        @NotNull(message = "Debe certificar el consentimiento")
        @AssertTrue(message = "Debe certificar la autorización del voluntario o tutor legal")
        Boolean consentimientoDatos
) {
    public RegistrarMiembroRequest {
        if (nivel == null) {
            nivel = NivelMiembro.APRENDIZ;
        }
    }
}
