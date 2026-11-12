package com.huertos.dto;

import com.huertos.model.DisenoPrincipal;
import com.huertos.model.Genero;
import com.huertos.model.TipoOrganizacion;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CrearSolicitudComunidadRequest(
        @NotBlank(message = "El nombre de la comunidad es obligatorio")
        @Pattern(regexp = SOLO_LETRAS, message = "El nombre de la comunidad solo admite letras y espacios")
        @Size(max = 150, message = "El nombre de la comunidad admite hasta 150 caracteres")
        String nombreComunidad,

        @NotNull(message = "El tipo de organización es obligatorio")
        TipoOrganizacion tipoOrganizacion,

        @NotBlank(message = "La provincia es obligatoria")
        @Pattern(regexp = SOLO_LETRAS, message = "La provincia solo admite letras y espacios")
        @Size(max = 100, message = "La provincia admite hasta 100 caracteres")
        String provincia,

        @NotBlank(message = "La parroquia es obligatoria")
        @Pattern(regexp = SOLO_LETRAS, message = "La parroquia solo admite letras y espacios")
        @Size(max = 100, message = "La parroquia admite hasta 100 caracteres")
        String parroquia,

        @NotBlank(message = "La dirección es obligatoria")
        @Size(max = 255, message = "La dirección admite hasta 255 caracteres")
        String direccion,

        @NotNull(message = "La latitud es obligatoria")
        @DecimalMin(value = "-6.0", message = FUERA_DEL_ECUADOR)
        @DecimalMax(value = "2.5", message = FUERA_DEL_ECUADOR)
        BigDecimal latitud,

        @NotNull(message = "La longitud es obligatoria")
        @DecimalMin(value = "-92.0", message = FUERA_DEL_ECUADOR)
        @DecimalMax(value = "-75.0", message = FUERA_DEL_ECUADOR)
        BigDecimal longitud,

        @NotNull(message = "La altitud es obligatoria")
        @Min(value = 0, message = "La altitud no puede ser negativa")
        @Max(value = 5000, message = "La altitud admite hasta 5000 msnm")
        Integer altitudMsnm,

        @NotNull(message = "El diseño principal es obligatorio")
        DisenoPrincipal disenoPrincipal,

        @NotBlank(message = "Los subtipos de cultivo son obligatorios")
        @Size(max = 150, message = "Los subtipos admiten hasta 150 caracteres")
        String subtiposSolicitados,

        @NotBlank(message = "La cédula es obligatoria")
        @Pattern(regexp = "^[0-9]{10}$", message = "La cédula debe tener 10 dígitos")
        String cedulaSolicitante,

        @NotBlank(message = "El nombre del solicitante es obligatorio")
        @Size(max = 120, message = "El nombre del solicitante admite hasta 120 caracteres")
        String nombreSolicitante,

        @NotNull(message = "La fecha de nacimiento es obligatoria")
        @Past(message = "La fecha de nacimiento debe ser anterior a hoy")
        LocalDate fechaNacimiento,

        @NotNull(message = "El género es obligatorio")
        Genero generoSolicitante,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "Formato de correo inválido")
        @Size(max = 180, message = "El correo admite hasta 180 caracteres")
        String correoSolicitante,

        @NotBlank(message = "El celular es obligatorio")
        @Pattern(regexp = "^09[0-9]{8}$", message = "El celular debe tener 10 dígitos y empezar con 09")
        String celularSolicitante,

        @NotNull(message = "Debe autorizar el tratamiento de datos personales bajo los términos de la LOPDP")
        @AssertTrue(message = "Debe autorizar el tratamiento de datos personales bajo los términos de la LOPDP")
        Boolean consentimientoDatos) {

    private static final String SOLO_LETRAS = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$";
    private static final String FUERA_DEL_ECUADOR =
            "Las coordenadas geográficas ingresadas no corresponden al territorio ecuatoriano";
}
