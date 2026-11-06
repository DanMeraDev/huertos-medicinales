CREATE TABLE auditoria_roles (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id     BIGINT      NOT NULL,
    rol_anterior   VARCHAR(30) NOT NULL,
    rol_nuevo      VARCHAR(30) NOT NULL,
    modificado_por BIGINT      NOT NULL,
    fecha_cambio   DATETIME    NOT NULL,
    CONSTRAINT fk_auditoria_roles_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id),
    CONSTRAINT fk_auditoria_roles_modificador FOREIGN KEY (modificado_por) REFERENCES usuarios (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
