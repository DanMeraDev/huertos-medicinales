import { Insignia } from '../../components/Insignia'
import type { TalentoComunidadResponse } from './talentosApi'
import './GaleriaTalentosComunidad.css'

export interface GaleriaTalentosComunidadProps {
  talentos: TalentoComunidadResponse[]
}

export function GaleriaTalentosComunidad({ talentos }: GaleriaTalentosComunidadProps) {
  if (talentos.length === 0) {
    return (
      <section className="galeria-talentos-seccion">
        <h3 className="galeria-talentos-titulo">Medallas de la Comunidad</h3>
        <div className="galeria-talentos-vacia">
          <p>Aún no se han configurado medallas para esta comunidad. ¡Crea la primera medalla arriba!</p>
        </div>
      </section>
    )
  }

  return (
    <section className="galeria-talentos-seccion">
      <h3 className="galeria-talentos-titulo">Medallas de la Comunidad</h3>
      <div className="galeria-talentos-grilla">
        {talentos.map((talento) => (
          <article key={talento.id} className="galeria-talento-tarjeta">
            <div className="galeria-talento-cabecera">
              <h4 className="galeria-talento-nombre">{talento.nombreInsignia}</h4>
              <Insignia texto={`${talento.metaRepeticiones} rep`} tono="exito" />
            </div>

            <p className="galeria-talento-descripcion">{talento.descripcion}</p>

            <div className="galeria-talento-detalles">
              <span>
                <strong>Actividad:</strong> {talento.nombreActividad}
              </span>
              <span>
                <strong>Meta requerida:</strong> {talento.metaRepeticiones} repeticiones
              </span>
            </div>
          </article>
        ))}
      </div>
    </section>
  )
}
