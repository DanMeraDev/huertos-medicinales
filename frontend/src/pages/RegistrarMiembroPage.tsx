import { RegistrarMiembroForm } from '../features/miembros/RegistrarMiembroForm'

export function RegistrarMiembroPage() {
  return (
    <div
      className="pagina-registrar-miembro"
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
        Registrar Voluntario
      </h1>
      <RegistrarMiembroForm />
    </div>
  )
}
