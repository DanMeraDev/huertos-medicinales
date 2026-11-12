import { DirectorioComunidadesTabla } from '../features/comunidades-publico/DirectorioComunidadesTabla'

export function DirectorioComunidadesPage() {
  return (
    <div
      className="pagina-directorio-comunidades"
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
        Directorio de Comunidades
      </h1>
      <DirectorioComunidadesTabla />
    </div>
  )
}
