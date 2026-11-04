import './Insignia.css'

export interface InsigniaProps {
  texto: string
  tono?: 'neutro' | 'exito' | 'advertencia' | 'error'
}

export function Insignia({ texto, tono = 'neutro' }: InsigniaProps) {
  return <span className={`insignia insignia-${tono}`}>{texto}</span>
}
