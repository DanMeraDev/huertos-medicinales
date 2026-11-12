package com.huertos.repository;

import com.huertos.model.Siembra;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SiembraRepository extends JpaRepository<Siembra, Long> {

    List<Siembra> findBySubhuertoIdOrderByFechaSiembraDesc(Long subhuertoId);
}

