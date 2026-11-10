package com.huertos.repository;

import com.huertos.model.Rol;
import com.huertos.model.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    boolean existsByEmail(String email);

    boolean existsByIdentificacion(String identificacion);

    Optional<Usuario> findByEmail(String email);

    boolean existsByRol(Rol rol);
}
