import { useEffect, useState } from 'react'
import { Boton } from '../../components/Boton'
import { Insignia } from '../../components/Insignia'
import { Notificacion } from '../../components/Notificacion'
import { ETIQUETAS_CATEGORIA, ETIQUETAS_FASE_LUNAR } from './etiquetas'
import { FichaDetalleSaber } from './FichaDetalleSaber'
import {
  obtenerRutaSaberes,
  type SaberAncestralResumenResponse,
} from './saberesAncestralesApi'
import './RutaSecuencialSaberes.css'

export function RutaSecuencialSaberes() {
  const [ruta, setRuta] = useState<SaberAncestralResumenResponse[] | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [idDetalle, setIdDetalle] = useState<number | null>(null)

  useEffect(() => {
    let vigente = true
    obtenerRutaSaberes()
      .then((pasos) => {
        if (vigente) setRuta(pasos)
      })
      .catch(() => {
        if (vigente) setError('No se pudo consultar la biblioteca de saberes ancestrales.')
      })
    return () => {
      vigente = false
    }
  }, [])

  if (error) {
    return <Notificacion tipo="error" mensaje={error} />
  }

  if (!ruta) {
    return <p className="ruta-saberes-aviso">Cargando la ruta de saberes…</p>
  }

  return (
    <>
      <ol className="ruta-saberes" aria-label="Ruta secuencial de saberes ancestrales">
        {ruta.map((saber) => (
          <li key={saber.id} className="ruta-saberes-paso">
            <article className="ruta-saberes-tarjeta">
              <span className="ruta-saberes-numero" aria-hidden="true">
                {saber.ordenPaso}
              </span>
              <h3 className="ruta-saberes-titulo">{saber.titulo}</h3>
              {saber.tituloKichwa && (
                <p className="ruta-saberes-kichwa" lang="qu">
                  {saber.tituloKichwa}
                </p>
              )}
              <div className="ruta-saberes-etiquetas">
                <Insignia texto={ETIQUETAS_CATEGORIA[saber.categoria]} />
                <Insignia
                  texto={ETIQUETAS_FASE_LUNAR[saber.faseLunarIdeal]}
                  tono="exito"
                />
              </div>
              <p className="ruta-saberes-insignia">
                Insignia oficial: <strong>{saber.nombreInsigniaOficial}</strong>
              </p>
              <Boton onClick={() => setIdDetalle(saber.id)}>
                Ver contenido del paso
              </Boton>
            </article>
          </li>
        ))}
      </ol>
      {idDetalle !== null && (
        <FichaDetalleSaber idSaber={idDetalle} alCerrar={() => setIdDetalle(null)} />
      )}
    </>
  )
}
