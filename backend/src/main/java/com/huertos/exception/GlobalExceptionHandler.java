package com.huertos.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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

        @ExceptionHandler(AccessDeniedException.class)
        ProblemDetail manejarAccesoDenegado() {
                return ProblemDetail.forStatusAndDetail(
                                HttpStatus.FORBIDDEN,
                                "No tienes permisos para acceder a este módulo.");
        }

        @ExceptionHandler(ReglaNegocioException.class)
        ProblemDetail manejarReglaNegocio(ReglaNegocioException ex) {
                return ProblemDetail.forStatusAndDetail(
                                HttpStatus.BAD_REQUEST,
                                ex.getMessage());
        }
}
