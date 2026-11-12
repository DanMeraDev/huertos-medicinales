import { TablaSolicitudes } from '../features/solicitudes/TablaSolicitudes'
import './SolicitudesPage.css'

export function SolicitudesPage() {
  return (
    <section className="solicitudes-pagina">
      <h1>Solicitudes de comunidad</h1>
      <TablaSolicitudes />
    </section>
  )
}
