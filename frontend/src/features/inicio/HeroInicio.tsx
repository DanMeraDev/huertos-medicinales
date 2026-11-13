import { Link } from 'react-router'
import './HeroInicio.css'

export function HeroInicio() {
  const base = (import.meta.env.BASE_URL ?? '/').replace(/\/$/, '')
  const imagenFondo = `${base}/hero/hero.jpg`

  return (
    <section
      className="hero-inicio"
      style={{ backgroundImage: `url(${imagenFondo})` }}
      aria-labelledby="hero-inicio-titulo"
    >
      <div className="hero-inicio-velo">
        <div className="hero-inicio-contenido">
          <span className="hero-inicio-etiqueta">
            Red Nacional de Huertos Medicinales · MSP
          </span>
          <h1 id="hero-inicio-titulo" className="hero-inicio-titulo">
            Hampi Muyu
          </h1>
          <p className="hero-inicio-subtitulo">
            Huertos Medicinales Comunitarios para sembrar salud y sabiduría ancestral
          </p>
          <p className="hero-inicio-descripcion">
            Cultivamos, empaquetamos y entregamos gratis plantas medicinales para que el MSP atienda
            enfermedades leves y reduzca el uso de medicamentos occidentales. Organiza tu territorio,
            gestiona tu huerto y aprende jugando con rankings, calendario lunar e insignias.
          </p>
          <div className="hero-inicio-acciones">
            <Link to="/catalogo-plantas" className="hero-inicio-cta">
              Explorar catálogo de plantas
            </Link>
            <Link to="/comunidades" className="hero-inicio-secundario">
              Conocer comunidades
            </Link>
            <Link to="/ranking/comunidades" className="hero-inicio-enlace">
              Ver ranking comunitario
            </Link>
          </div>
        </div>
      </div>
    </section>
  )
}
