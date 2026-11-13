import { FormularioActividadAncestral } from '../features/actividades-ancestrales/FormularioActividadAncestral'
import { ConsejoKinti } from '../components/ConsejoKinti'

export function NuevaActividadAncestralPage() {
  return (
    <div
      className="pagina-nueva-actividad-ancestral"
      style={{
        maxWidth: 'var(--ancho-maximo, 1200px)',
        margin: '0 auto',
        width: '100%',
        padding: 'var(--espacio-4)',
      }}
    >
      <h1
        style={{
          fontSize: '1.75rem',
          fontWeight: 700,
          color: 'var(--color-texto)',
          marginBottom: 'var(--espacio-4)',
        }}
      >
        Registrar Actividad Ancestral
      </h1>
      <ConsejoKinti
        variante="sabiduria"
        mensaje="Comparte los saberes de tu huerto. Una foto clara ayuda al Técnico MSP a validarlos más rápido."
      />
      <FormularioActividadAncestral />
    </div>
  )
}
