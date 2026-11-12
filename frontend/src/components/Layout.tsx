import type { ReactNode } from 'react'
import { Link } from 'react-router'
import './Layout.css'

export interface LayoutProps {
  children?: ReactNode
}

const MODULOS = [
  { nombre: 'Acceso', ruta: '/' },
  { nombre: 'Mi Comunidad', ruta: '/mi-comunidad' },
  { nombre: 'Comunidades y Miembros', ruta: '/comunidades' },
  { nombre: 'Huerto Real', ruta: '/huerto-real' },
  { nombre: 'Catálogo de Plantas', ruta: '/catalogo-plantas' },
  { nombre: 'Bodega', ruta: '/bodega' },
  { nombre: 'Saberes Ancestrales', ruta: '/saberes-ancestrales' },
  { nombre: 'Calendario Lunar', ruta: '/calendario-lunar' },
  { nombre: 'Rankings', ruta: '/ranking/comunidades' },
  { nombre: 'Insignias y Talentos', ruta: '/comunidad/talentos' },
  { nombre: 'Solicitudes', ruta: '/solicitudes' },
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
                  <Link to={modulo.ruta} className="layout-enlace">
                    {modulo.nombre}
                  </Link>
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
