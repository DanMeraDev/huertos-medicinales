package com.huertos.repository;

import com.huertos.dto.FiltrosPlantaRequest;
import com.huertos.model.CategoriaUso;
import com.huertos.model.CicloCultivo;
import com.huertos.model.CualidadTermica;
import com.huertos.model.OrigenPlanta;
import com.huertos.model.Planta;
import jakarta.persistence.criteria.Predicate;
import java.util.Arrays;
import java.util.Set;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.query.EscapeCharacter;

public final class PlantaSpecification {

    private static final EscapeCharacter ESCAPE = EscapeCharacter.DEFAULT;

    private PlantaSpecification() {
    }

    public static Specification<Planta> filtrar(FiltrosPlantaRequest filtros) {
        return Specification.allOf(
                activa(),
                conCualidad(filtros.cualidad()),
                abarcaAltitud(filtros.altitud()),
                enRegion(filtros.region()),
                conCategoria(filtros.categoria()),
                conCiclo(filtros.ciclo()),
                conOrigen(filtros.origen()),
                nombreContiene(filtros.criterio()));
    }

    public static Specification<Planta> activa() {
        return (root, query, cb) -> cb.isTrue(root.get("activa"));
    }

    public static Specification<Planta> conCualidad(CualidadTermica cualidad) {
        return igualA("cualidadTermica", cualidad);
    }

    public static Specification<Planta> conCiclo(CicloCultivo ciclo) {
        return igualA("cicloCultivo", ciclo);
    }

    public static Specification<Planta> conOrigen(OrigenPlanta origen) {
        return igualA("origen", origen);
    }

    public static Specification<Planta> abarcaAltitud(Integer altitud) {
        if (altitud == null) {
            return Specification.unrestricted();
        }
        return (root, query, cb) -> cb.and(
                cb.or(cb.isNull(root.get("altitudMinMsnm")),
                        cb.lessThanOrEqualTo(root.get("altitudMinMsnm"), altitud)),
                cb.or(cb.isNull(root.get("altitudMaxMsnm")),
                        cb.greaterThanOrEqualTo(root.get("altitudMaxMsnm"), altitud)));
    }

    public static Specification<Planta> enRegion(String region) {
        return contiene(region, "regionesDistribucion");
    }

    public static Specification<Planta> conCategoria(CategoriaUso categoria) {
        if (categoria == null) {
            return Specification.unrestricted();
        }
        return (root, query, cb) -> cb.isMember(categoria, root.<Set<CategoriaUso>>get("categorias"));
    }

    public static Specification<Planta> nombreContiene(String criterio) {
        return contiene(criterio, "nombreComun", "nombreCientifico", "nombreKichwa");
    }

    private static Specification<Planta> igualA(String atributo, Object valor) {
        if (valor == null) {
            return Specification.unrestricted();
        }
        return (root, query, cb) -> cb.equal(root.get(atributo), valor);
    }

    private static Specification<Planta> contiene(String texto, String... atributos) {
        if (texto == null || texto.isBlank()) {
            return Specification.unrestricted();
        }
        String patron = "%" + ESCAPE.escape(texto.trim()) + "%";
        return (root, query, cb) -> cb.or(Arrays.stream(atributos)
                .map(atributo -> cb.like(root.get(atributo), patron, ESCAPE.getEscapeCharacter()))
                .toArray(Predicate[]::new));
    }
}
