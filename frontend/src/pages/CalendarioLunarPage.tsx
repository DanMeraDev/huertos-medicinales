import { ConsultarCalendarioLunar } from '../features/calendario-lunar/ConsultarCalendarioLunar'
import './CalendarioLunarPage.css'

export function CalendarioLunarPage() {
  return (
    <main className="pagina-calendario">
      <h1 className="pagina-calendario-titulo">Calendario lunar</h1>
      <p className="pagina-calendario-descripcion">
        Las fases de la luna según el calendario agroecológico andino, con las prácticas del huerto
        y el consejo de Kinti.
      </p>
      <ConsultarCalendarioLunar />
    </main>
  )
}
