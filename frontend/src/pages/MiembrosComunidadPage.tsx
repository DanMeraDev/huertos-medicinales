import { TablaMiembrosComunidad } from '../features/miembros/TablaMiembrosComunidad'
import { ConsejoKinti } from '../components/ConsejoKinti'

export function MiembrosComunidadPage() {
  return (
    <div
      className="pagina-miembros-comunidad"
      style={{
        maxWidth: 'var(--ancho-maximo, 1200px)',
        margin: '0 auto',
        width: '100%',
        padding: 'var(--espacio-4)',
      }}
    >
      <ConsejoKinti
        variante="oficial"
        mensaje="Cada voluntario cuenta. Mantén sus datos al día y revisa su ficha antes de darlo de baja."
      />
      <TablaMiembrosComunidad />
    </div>
  )
}
