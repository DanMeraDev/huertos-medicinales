import { TablaMiembrosComunidad } from '../features/miembros/TablaMiembrosComunidad'

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
      <TablaMiembrosComunidad />
    </div>
  )
}
