import { BuscadorPerfilCedula } from '../features/miembros-publico/BuscadorPerfilCedula'
import { CtaRegistroComunidad } from '../features/miembros-publico/CtaRegistroComunidad'
import './ConsultaMiembroPage.css'

export function ConsultaMiembroPage() {
  return (
    <div className="consulta-miembro-pagina">
      <h1 className="consulta-miembro-titulo">Consultar Voluntario</h1>
      <BuscadorPerfilCedula />
      <CtaRegistroComunidad />
    </div>
  )
}
