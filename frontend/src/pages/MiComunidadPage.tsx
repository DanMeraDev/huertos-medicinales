import { MiComunidadForm } from '../features/comunidades/MiComunidadForm'

export function MiComunidadPage() {
  return (
    <div
      className="pagina-mi-comunidad"
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
        Mi Comunidad
      </h1>
      <MiComunidadForm />
    </div>
  )
}
