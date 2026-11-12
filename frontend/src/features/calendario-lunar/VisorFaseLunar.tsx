import { Insignia } from '../../components/Insignia'
import type { CalendarioLunarResponse } from './calendarioLunarApi'
import './VisorFaseLunar.css'

export interface VisorFaseLunarProps {
  fase: CalendarioLunarResponse
  vigente: boolean
}

const formatoFecha = new Intl.DateTimeFormat('es-EC', {
  day: 'numeric',
  month: 'long',
  year: 'numeric',
})

function formatearFecha(fecha: string): string {
  return formatoFecha.format(new Date(`${fecha}T00:00:00`))
}

export function VisorFaseLunar({ fase, vigente }: VisorFaseLunarProps) {
  return (
    <article className="visor-fase">
      <div
        className={`visor-fase-luna visor-fase-luna--${fase.faseLunar.toLowerCase()}`}
        role="img"
        aria-label={`Ilustración de ${fase.nombreEspanol}`}
      />
      <div className="visor-fase-datos">
        <h2 className="visor-fase-kichwa">{fase.nombreKichwa}</h2>
        <p className="visor-fase-espanol">{fase.nombreEspanol}</p>
        <Insignia
          texto={vigente ? 'Fase vigente' : 'Fecha consultada'}
          tono={vigente ? 'exito' : 'neutro'}
        />
        <p className="visor-fase-rango">
          Del {formatearFecha(fase.fechaInicio)} al {formatearFecha(fase.fechaFin)}
        </p>
      </div>
    </article>
  )
}
