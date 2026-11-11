package com.huertos.repository;

import com.huertos.model.Rol;
import com.huertos.model.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    boolean existsByCorreo(String correo);

    boolean existsByIdentificacion(String identificacion);

    Optional<Usuario> findByCorreo(String correo);

    boolean existsByRol(Rol rol);
}
