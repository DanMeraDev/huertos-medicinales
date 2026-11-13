import { Link } from 'react-router'
import { KintiGuia } from '../../components/KintiGuia'
import './CtaComunidadInicio.css'

export function CtaComunidadInicio() {
  return (
    <section className="cta-inicio" aria-labelledby="cta-inicio-titulo">
      <div className="cta-inicio-tarjeta">
        <div className="cta-inicio-ilustracion">
          <KintiGuia
            variante="celebracion"
            tamano="xl"
            alt="Kinti llamando a crear una nueva comunidad"
          />
        </div>
        <div className="cta-inicio-contenido">
          <h2 id="cta-inicio-titulo" className="cta-inicio-titulo">
            ¡Organiza tu territorio y crea el huerto medicinal de tu comunidad!
          </h2>
          <p className="cta-inicio-descripcion">
            Desde Hampi Muyu podrás administrar tu huerto, registrar voluntarios, documentar
            saberes ancestrales y articular tu entrega solidaria con el Ministerio de Salud Pública
            para llevar medicina natural gratuita a más familias.
          </p>
          <div className="cta-inicio-acciones">
            <Link to="/solicitar-comunidad" className="cta-inicio-boton">
              Solicitar creación de comunidad
            </Link>
            <Link to="/comunidades" className="cta-inicio-secundario">
              Explorar comunidades activas
            </Link>
          </div>
        </div>
      </div>
    </section>
  )
}
