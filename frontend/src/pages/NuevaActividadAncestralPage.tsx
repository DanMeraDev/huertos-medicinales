import { FormularioActividadAncestral } from '../features/actividades-ancestrales/FormularioActividadAncestral'

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
      <FormularioActividadAncestral />
    </div>
  )
}
