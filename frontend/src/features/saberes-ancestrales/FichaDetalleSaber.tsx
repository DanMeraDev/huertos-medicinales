import { useEffect, useState } from 'react'
import { ApiError } from '../../api/client'
import { Insignia } from '../../components/Insignia'
import { Modal } from '../../components/Modal'
import { Notificacion } from '../../components/Notificacion'
import { ETIQUETAS_CATEGORIA, ETIQUETAS_FASE_LUNAR } from './etiquetas'
import {
  obtenerDetalleSaber,
  type SaberAncestralDetalleResponse,
} from './saberesAncestralesApi'
import './FichaDetalleSaber.css'

type Carga =
  | 'cargando'
  | { tipo: 'lista'; detalle: SaberAncestralDetalleResponse }
  | { tipo: 'error'; mensaje: string }

export interface FichaDetalleSaberProps {
  idSaber: number
  alCerrar: () => void
}

export function FichaDetalleSaber({ idSaber, alCerrar }: FichaDetalleSaberProps) {
  const [carga, setCarga] = useState<Carga>('cargando')

  useEffect(() => {
    let vigente = true
    obtenerDetalleSaber(idSaber)
      .then((detalle) => {
        if (vigente) setCarga({ tipo: 'lista', detalle })
      })
      .catch((error: unknown) => {
        if (!vigente) return
        const mensaje =
          error instanceof ApiError && error.status === 404
            ? (error.detalle ?? 'El saber ancestral solicitado no fue encontrado')
            : 'No se pudo cargar el contenido del saber ancestral.'
        setCarga({ tipo: 'error', mensaje })
      })
    return () => {
      vigente = false
    }
  }, [idSaber])

  const titulo =
    carga === 'cargando'
      ? 'Detalle del saber ancestral'
      : carga.tipo === 'lista'
        ? carga.detalle.titulo
        : 'Detalle del saber ancestral'

  return (
    <Modal abierto titulo={titulo} alCancelar={alCerrar}>
      {carga === 'cargando' && <p>Consultando el saber ancestral…</p>}
      {carga !== 'cargando' && carga.tipo === 'error' && (
        <Notificacion tipo="error" mensaje={carga.mensaje} />
      )}
      {carga !== 'cargando' && carga.tipo === 'lista' && (
        <div className="ficha-saber">
          <p className="ficha-saber-paso">
            Paso {carga.detalle.ordenPaso} · {carga.detalle.codigoHito}
          </p>
          {carga.detalle.tituloKichwa && (
            <p className="ficha-saber-kichwa" lang="qu">
              Kichwa: {carga.detalle.tituloKichwa}
            </p>
          )}
          <div className="ficha-saber-etiquetas">
            <Insignia texto={ETIQUETAS_CATEGORIA[carga.detalle.categoria]} />
            <Insignia
              texto={ETIQUETAS_FASE_LUNAR[carga.detalle.faseLunarIdeal]}
              tono="exito"
            />
          </div>
          <section>
            <h3>Horario energético ideal</h3>
            <p>{carga.detalle.horarioEnergetico}</p>
          </section>
          <section>
            <h3>Contenido educativo</h3>
            <p>{carga.detalle.contenidoEducativo}</p>
          </section>
          <section>
            <h3>Ingredientes y herramientas tradicionales</h3>
            <p>{carga.detalle.ingredientesHerramientas ?? 'La comunidad indica lo necesario.'}</p>
          </section>
          <section className="ficha-saber-premio">
            <h3>Insignia oficial del paso</h3>
            <Insignia texto={carga.detalle.nombreInsigniaOficial} tono="advertencia" />
            <p>
              Otorga {carga.detalle.puntosBaseComunidad} puntos base a la comunidad.
            </p>
          </section>
        </div>
      )}
    </Modal>
  )
}
