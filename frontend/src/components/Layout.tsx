import type { ReactNode } from 'react'
import { Link, NavLink, useNavigate } from 'react-router'
import { cerrarSesion, useSesion, type RolUsuario } from '../api/sesion'
import './Layout.css'

interface LayoutProps {
  children: ReactNode
}

interface OpcionMenu {
  nombre: string
  ruta: string
}

const OPCIONES_PUBLICAS: OpcionMenu[] = [
  { nombre: 'Inicio', ruta: '/' },
  { nombre: 'Comunidades', ruta: '/comunidades' },
  { nombre: 'Catálogo de Plantas', ruta: '/catalogo-plantas' },
  { nombre: 'Saberes Ancestrales', ruta: '/saberes-ancestrales' },
  { nombre: 'Calendario Lunar', ruta: '/calendario-lunar' },
  { nombre: 'Rankings', ruta: '/ranking/comunidades' },
  { nombre: 'Consultar Voluntario', ruta: '/consulta-miembro' },
]

const OPCIONES_POR_ROL: Record<RolUsuario, OpcionMenu[]> = {
  RESPONSABLE_COMUNIDAD: [
    { nombre: 'Mi Comunidad', ruta: '/mi-comunidad' },
    { nombre: 'Miembros', ruta: '/comunidad/miembros' },
    { nombre: 'Configurar Huerto', ruta: '/huerto/configuracion' },
    { nombre: 'Subhuertos', ruta: '/huerto/subhuertos' },
    { nombre: 'Registrar Siembra', ruta: '/huerto/siembras/nueva' },
    { nombre: 'Bodega', ruta: '/bodega/inventario' },
    { nombre: 'Ingreso Material', ruta: '/bodega/ingreso-material' },
    { nombre: 'Catálogo de Labores', ruta: '/comunidad/actividades-catalogo' },
    { nombre: 'Registrar Labor', ruta: '/comunidad/actividades-miembros/nueva' },
    { nombre: 'Saberes del Huerto', ruta: '/actividades-ancestrales/nueva' },
    { nombre: 'Insignias y Talentos', ruta: '/comunidad/talentos' },
  ],
  TECNICO_MSP: [
    { nombre: 'Solicitudes', ruta: '/solicitudes' },
    { nombre: 'Revisión Actividades', ruta: '/msp/revision-actividades' },
    { nombre: 'Nueva Planta', ruta: '/admin/plantas/nueva' },
    { nombre: 'Bodega', ruta: '/bodega/inventario' },
    { nombre: 'Configurar Huerto', ruta: '/huerto/configuracion' },
    { nombre: 'Subhuertos', ruta: '/huerto/subhuertos' },
  ],
  TECNICO_SIS: [{ nombre: 'Crear Usuario', ruta: '/usuarios/nuevo' }],
}

const ETIQUETA_ROL: Record<RolUsuario, string> = {
  RESPONSABLE_COMUNIDAD: 'Responsable de Comunidad',
  TECNICO_MSP: 'Técnico MSP',
  TECNICO_SIS: 'Técnico del Sistema',
}

function claseEnlace({ isActive }: { isActive: boolean }) {
  return isActive ? 'layout-enlace layout-enlace-activo' : 'layout-enlace'
}

export function Layout({ children }: LayoutProps) {
  const sesion = useSesion()
  const navegar = useNavigate()

  function manejarCerrarSesion() {
    cerrarSesion()
    navegar('/')
  }

  const opcionesRol = sesion ? OPCIONES_POR_ROL[sesion.rol] : []

  return (
    <div className="layout">
      <header className="layout-encabezado">
        <div className="layout-barra">
          <Link to="/" className="layout-marca">
            <span className="layout-logo" aria-hidden="true">
              🌿
            </span>
            <span className="layout-marca-textos">
              <span className="layout-titulo">Hampi Muyu</span>
              <span className="layout-subtitulo">Huertos Medicinales Comunitarios</span>
            </span>
          </Link>

          <nav className="layout-nav" aria-label="Navegación principal">
            <ul className="layout-menu">
              {OPCIONES_PUBLICAS.map((modulo) => (
                <li key={modulo.ruta}>
                  <NavLink to={modulo.ruta} end={modulo.ruta === '/'} className={claseEnlace}>
                    {modulo.nombre}
                  </NavLink>
                </li>
              ))}
            </ul>
          </nav>

          <div className="layout-acciones">
            {sesion ? (
              <div className="layout-sesion">
                <div className="layout-usuario">
                  <span className="layout-usuario-nombre">
                    {sesion.nombreCompleto ?? ETIQUETA_ROL[sesion.rol]}
                  </span>
                  <span className="layout-usuario-rol">{ETIQUETA_ROL[sesion.rol]}</span>
                </div>
                <button
                  type="button"
                  className="layout-boton-salir"
                  onClick={manejarCerrarSesion}
                >
                  Cerrar sesión
                </button>
              </div>
            ) : (
              <Link to="/login" className="layout-boton-login">
                Iniciar sesión
              </Link>
            )}
          </div>
        </div>

        {sesion && opcionesRol.length > 0 && (
          <div className="layout-barra-rol">
            <nav className="layout-nav-rol" aria-label="Navegación por rol">
              <span className="layout-rol-etiqueta">{ETIQUETA_ROL[sesion.rol]}</span>
              <ul className="layout-menu">
                {opcionesRol.map((opcion) => (
                  <li key={opcion.ruta}>
                    <NavLink to={opcion.ruta} className={claseEnlace}>
                      {opcion.nombre}
                    </NavLink>
                  </li>
                ))}
              </ul>
            </nav>
          </div>
        )}
      </header>
      <main className="layout-contenido">{children}</main>
    </div>
  )
}
