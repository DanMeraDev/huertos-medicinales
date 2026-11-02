import type { ReactNode } from 'react'
import './Layout.css'

export interface LayoutProps {
  children?: ReactNode
}

const MODULOS = [
  { nombre: 'Acceso', ruta: '#acceso' },
  { nombre: 'Comunidades y Miembros', ruta: '#comunidades' },
  { nombre: 'Huerto Real', ruta: '#huerto-real' },
  { nombre: 'Catálogo de Plantas', ruta: '#plantas' },
  { nombre: 'Bodega', ruta: '#bodega' },
]

export function Layout({ children }: LayoutProps) {
  return (
    <div className="layout">
      <header className="layout-encabezado">
        <div className="layout-barra">
          <div className="layout-marca">
            <span className="layout-logo" aria-hidden="true">🌿</span>
            <span className="layout-titulo">Huertos Medicinales Comunitarios</span>
          </div>
          <nav className="layout-nav" aria-label="Navegación principal">
            <ul className="layout-menu">
              {MODULOS.map((modulo) => (
                <li key={modulo.nombre}>
                  <a href={modulo.ruta} className="layout-enlace">
                    {modulo.nombre}
                  </a>
                </li>
              ))}
            </ul>
          </nav>
        </div>
      </header>
      <main className="layout-contenido">{children}</main>
    </div>
  )
}
