package com.huertos.repository;

import com.huertos.model.Huerto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HuertoRepository extends JpaRepository<Huerto, Long> {
}
