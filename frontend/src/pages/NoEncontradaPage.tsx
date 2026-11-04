import { Link } from 'react-router'

export function NoEncontradaPage() {
  return (
    <main className="inicio">
      <h1>Página no encontrada</h1>
      <Link to="/">Volver al inicio</Link>
    </main>
  )
}
