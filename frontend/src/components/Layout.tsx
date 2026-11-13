import { useEffect, useState, type ReactNode } from 'react'
import { Link, NavLink, useLocation, useNavigate } from 'react-router'
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
    { nombre: 'Subsanar Evidencias', ruta: '/actividades-ancestrales/observadas' },
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

function claseEnlaceDrawer({ isActive }: { isActive: boolean }) {
  return isActive ? 'layout-drawer-enlace layout-drawer-enlace-activo' : 'layout-drawer-enlace'
}

export function Layout({ children }: LayoutProps) {
  const sesion = useSesion()
  const navegar = useNavigate()
  const ubicacion = useLocation()
  const [rutaPanel, setRutaPanel] = useState<string | null>(null)
  const panelAbierto = rutaPanel === ubicacion.pathname

  useEffect(() => {
    if (!panelAbierto) {
      return
    }
    function alPresionarTecla(evento: KeyboardEvent) {
      if (evento.key === 'Escape') {
        setRutaPanel(null)
      }
    }
    window.addEventListener('keydown', alPresionarTecla)
    return () => window.removeEventListener('keydown', alPresionarTecla)
  }, [panelAbierto])

  function manejarCerrarSesion() {
    setRutaPanel(null)
    cerrarSesion()
    navegar('/')
  }

  const opcionesRol = sesion ? OPCIONES_POR_ROL[sesion.rol] : []
  const base = (import.meta.env.BASE_URL ?? '/').replace(/\/$/, '')

  return (
    <div className="layout">
      <header className="layout-encabezado">
        <div className="layout-barra">
          <Link to="/" className="layout-marca">
            <img
              src={`${base}/favicon.svg`}
              alt=""
              aria-hidden="true"
              className="layout-logo-imagen"
            />
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
                <button
                  type="button"
                  className="layout-boton-panel"
                  aria-expanded={panelAbierto}
                  aria-controls="drawer-panel-rol"
                  onClick={() =>
                    setRutaPanel((actual) =>
                      actual === ubicacion.pathname ? null : ubicacion.pathname,
                    )
                  }
                >
                  <span className="layout-panel-icono" aria-hidden="true">
                    ☰
                  </span>
                  <span>Mi Panel</span>
                </button>
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
      </header>

      {sesion && panelAbierto && opcionesRol.length > 0 && (
        <div
          className="layout-drawer-fondo"
          onClick={() => setRutaPanel(null)}
        >
          <aside
            id="drawer-panel-rol"
            className="layout-drawer"
            role="dialog"
            aria-modal="true"
            aria-label={`Panel de ${ETIQUETA_ROL[sesion.rol]}`}
            onClick={(evento) => evento.stopPropagation()}
          >
            <div className="layout-drawer-cabecera">
              <div className="layout-drawer-identidad">
                <span className="layout-drawer-rol">{ETIQUETA_ROL[sesion.rol]}</span>
                <p className="layout-drawer-usuario">
                  {sesion.nombreCompleto ?? ETIQUETA_ROL[sesion.rol]}
                </p>
              </div>
              <button
                type="button"
                className="layout-drawer-cerrar"
                aria-label="Cerrar menú"
                onClick={() => setRutaPanel(null)}
              >
                ×
              </button>
            </div>

            <nav className="layout-drawer-nav" aria-label="Navegación por rol">
              <ul className="layout-drawer-lista">
                {opcionesRol.map((opcion) => (
                  <li key={opcion.ruta}>
                    <NavLink
                      to={opcion.ruta}
                      className={claseEnlaceDrawer}
                      onClick={() => setRutaPanel(null)}
                    >
                      {opcion.nombre}
                    </NavLink>
                  </li>
                ))}
              </ul>
            </nav>
          </aside>
        </div>
      )}

      <main className="layout-contenido">{children}</main>
    </div>
  )
}
