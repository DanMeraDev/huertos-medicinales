import { useEffect, useState } from 'react'
import { ApiError } from '../../api/client'
import { CampoTexto } from '../../components/CampoTexto'
import { Notificacion } from '../../components/Notificacion'
import {
  obtenerFaseLunarActual,
  obtenerFasePorFecha,
  type CalendarioLunarResponse,
} from './calendarioLunarApi'
import { TarjetaConsejoKinti } from './TarjetaConsejoKinti'
import { VisorFaseLunar } from './VisorFaseLunar'
import './ConsultarCalendarioLunar.css'

type Resultado =
  | { tipo: 'mostrado'; fase: CalendarioLunarResponse }
  | { tipo: 'error'; mensaje: string }

export function ConsultarCalendarioLunar() {
  const [fecha, setFecha] = useState('')
  const [resultado, setResultado] = useState<{ fecha: string; valor: Resultado } | null>(null)

  useEffect(() => {
    let vigente = true
    const peticion = fecha ? obtenerFasePorFecha(fecha) : obtenerFaseLunarActual()
    peticion
      .then((fase) => {
        if (vigente) setResultado({ fecha, valor: { tipo: 'mostrado', fase } })
      })
      .catch((error: unknown) => {
        if (!vigente) return
        const mensaje =
          error instanceof ApiError && error.status === 404
            ? (error.detalle ?? 'No hay datos lunares disponibles para la fecha seleccionada')
            : 'No se pudo consultar el calendario lunar.'
        setResultado({ fecha, valor: { tipo: 'error', mensaje } })
      })
    return () => {
      vigente = false
    }
  }, [fecha])

  const cargando = resultado?.fecha !== fecha

  return (
    <section className="consulta-calendario">
      <CampoTexto
        etiqueta="Consultar otra fecha"
        type="date"
        valor={fecha}
        alCambiar={setFecha}
      />
      {cargando && <p className="consulta-calendario-aviso">Consultando las fases lunares…</p>}
      {!cargando && resultado?.valor.tipo === 'error' && (
        <Notificacion tipo="error" mensaje={resultado.valor.mensaje} />
      )}
      {!cargando && resultado?.valor.tipo === 'mostrado' && (
        <>
          <VisorFaseLunar fase={resultado.valor.fase} vigente={fecha === ''} />
          <TarjetaConsejoKinti fase={resultado.valor.fase} />
        </>
      )}
    </section>
  )
}
