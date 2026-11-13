package com.huertos.exception;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * Convierte las excepciones en respuestas con formato ProblemDetail (RFC 9457),
 * para que el front reciba siempre la misma estructura de error.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(RecursoNoEncontradoException.class)
        ProblemDetail manejarNoEncontrado(RecursoNoEncontradoException ex) {
                return ProblemDetail.forStatusAndDetail(
                                HttpStatus.NOT_FOUND,
                                ex.getMessage());
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        ProblemDetail manejarValidacion(MethodArgumentNotValidException ex) {
                Map<String, String> errores = new LinkedHashMap<>();

                ex.getBindingResult().getFieldErrors()
                                .forEach(e -> errores.putIfAbsent(
                                                e.getField(),
                                                e.getDefaultMessage()));

                ProblemDetail problema = ProblemDetail.forStatusAndDetail(
                                HttpStatus.BAD_REQUEST,
                                "Datos no válidos");

                problema.setProperty("errores", errores);

                return problema;
        }

        @ExceptionHandler(ConflictoException.class)
        ProblemDetail handleConflicto(ConflictoException ex) {
                return ProblemDetail.forStatusAndDetail(
                                HttpStatus.CONFLICT,
                                ex.getMessage());
        }

        @ExceptionHandler(ReglaNegocioException.class)
        ProblemDetail manejarReglaNegocio(ReglaNegocioException ex) {
                return ProblemDetail.forStatusAndDetail(
                                HttpStatus.BAD_REQUEST,
                                ex.getMessage());
        }

        @ExceptionHandler(CredencialesInvalidasException.class)
        ProblemDetail manejarCredencialesInvalidas(CredencialesInvalidasException ex) {
                return ProblemDetail.forStatusAndDetail(
                                HttpStatus.UNAUTHORIZED,
                                ex.getMessage());
        }

        @ExceptionHandler(DemasiadosIntentosException.class)
        ProblemDetail manejarDemasiadosIntentos(DemasiadosIntentosException ex) {
                return ProblemDetail.forStatusAndDetail(
                                HttpStatus.TOO_MANY_REQUESTS,
                                ex.getMessage());
        }

        @ExceptionHandler(CuentaInactivaException.class)
        ProblemDetail manejarCuentaInactiva(CuentaInactivaException ex) {
                return ProblemDetail.forStatusAndDetail(
                                HttpStatus.FORBIDDEN,
                                ex.getMessage());
        }

        @ExceptionHandler(FormatoImagenNoAdmitidoException.class)
        ProblemDetail manejarFormatoImagenNoAdmitido(FormatoImagenNoAdmitidoException ex) {
                return ProblemDetail.forStatusAndDetail(
                                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                                ex.getMessage());
        }

        @ExceptionHandler({ImagenDemasiadoGrandeException.class, MaxUploadSizeExceededException.class})
        ProblemDetail manejarImagenDemasiadoGrande() {
                return ProblemDetail.forStatusAndDetail(
                                HttpStatus.PAYLOAD_TOO_LARGE,
                                ImagenDemasiadoGrandeException.MENSAJE);
        }

        @ExceptionHandler(AccessDeniedException.class)
        ProblemDetail manejarAccesoDenegado(AccessDeniedException ex) {
                return ProblemDetail.forStatusAndDetail(
                                HttpStatus.FORBIDDEN,
                                ex.getMessage());
        }

        @ExceptionHandler(HandlerMethodValidationException.class)
        ProblemDetail manejarValidacionMetodo(HandlerMethodValidationException ex) {
                String detalle = ex.getValueResults().stream()
                                .flatMap(result -> result.getResolvableErrors().stream())
                                .map(MessageSourceResolvable::getDefaultMessage)
                                .filter(Objects::nonNull)
                                .findFirst()
                                .orElse("Datos no válidos");
                return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detalle);
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        ProblemDetail manejarCuerpoIlegible() {
                return ProblemDetail.forStatusAndDetail(
                                HttpStatus.BAD_REQUEST,
                                "El cuerpo de la petición no es válido o tiene un formato incorrecto");
        }

        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        ProblemDetail manejarTipoDeParametro(MethodArgumentTypeMismatchException ex) {
                return ProblemDetail.forStatusAndDetail(
                                HttpStatus.BAD_REQUEST,
                                "El valor de " + ex.getName() + " no es válido");
        }
}
