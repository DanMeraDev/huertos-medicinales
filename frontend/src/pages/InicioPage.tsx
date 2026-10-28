import { EstadoServidor } from '../features/ping/EstadoServidor'

export function InicioPage() {
  return (
    <main className="inicio">
      <h1>Huertos Medicinales Comunitarios</h1>
      <EstadoServidor />
    </main>
  )
}
