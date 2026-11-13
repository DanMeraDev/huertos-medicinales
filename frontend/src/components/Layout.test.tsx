import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router'
import { guardarSesion } from '../api/sesion'
import { Layout } from './Layout'

describe('Layout', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  it('muestra Hampi Muyu como nombre del sistema en el encabezado', () => {
    render(
      <MemoryRouter>
        <Layout>Contenido</Layout>
      </MemoryRouter>,
    )

    const encabezado = screen.getByRole('banner')
    expect(encabezado).toHaveTextContent('Hampi Muyu')
    expect(encabezado).toHaveTextContent('Huertos Medicinales Comunitarios')
    const logo = encabezado.querySelector('img[src$="/favicon.svg"]')
    expect(logo).toBeInTheDocument()
  })

  it('en modo público muestra solo las opciones públicas y el botón Iniciar sesión', () => {
    render(
      <MemoryRouter>
        <Layout>Contenido</Layout>
      </MemoryRouter>,
    )

    const navegacion = screen.getByRole('navigation', { name: /navegación principal/i })
    expect(navegacion).toBeInTheDocument()

    expect(screen.getByRole('link', { name: 'Inicio' })).toHaveAttribute('href', '/')
    expect(screen.getByRole('link', { name: 'Comunidades' })).toHaveAttribute('href', '/comunidades')
    expect(screen.getByRole('link', { name: 'Catálogo de Plantas' })).toHaveAttribute(
      'href',
      '/catalogo-plantas',
    )
    expect(screen.getByRole('link', { name: 'Saberes Ancestrales' })).toHaveAttribute(
      'href',
      '/saberes-ancestrales',
    )
    expect(screen.getByRole('link', { name: 'Calendario Lunar' })).toHaveAttribute(
      'href',
      '/calendario-lunar',
    )
    expect(screen.getByRole('link', { name: 'Rankings' })).toHaveAttribute(
      'href',
      '/ranking/comunidades',
    )
    expect(screen.getByRole('link', { name: 'Consultar Voluntario' })).toHaveAttribute(
      'href',
      '/consulta-miembro',
    )

    expect(screen.getByRole('link', { name: 'Iniciar sesión' })).toHaveAttribute('href', '/login')

    expect(screen.queryByRole('button', { name: /mi panel/i })).not.toBeInTheDocument()
    expect(screen.queryByRole('link', { name: 'Mi Comunidad' })).not.toBeInTheDocument()
    expect(screen.queryByRole('link', { name: 'Solicitudes' })).not.toBeInTheDocument()
  })

  it('oculta las opciones del rol hasta abrir Mi Panel para RESPONSABLE_COMUNIDAD', async () => {
    guardarSesion({
      token: 'token-responsable',
      rol: 'RESPONSABLE_COMUNIDAD',
      nombreCompleto: 'Rosa Quishpe',
    })

    render(
      <MemoryRouter>
        <Layout>Contenido</Layout>
      </MemoryRouter>,
    )

    expect(screen.getByText('Rosa Quishpe')).toBeInTheDocument()
    expect(screen.queryByRole('link', { name: 'Mi Comunidad' })).not.toBeInTheDocument()

    await userEvent.click(screen.getByRole('button', { name: /mi panel/i }))

    expect(screen.getByRole('link', { name: 'Mi Comunidad' })).toHaveAttribute(
      'href',
      '/mi-comunidad',
    )
    expect(screen.getByRole('link', { name: 'Miembros' })).toHaveAttribute(
      'href',
      '/comunidad/miembros',
    )
    expect(screen.getByRole('link', { name: 'Subhuertos' })).toHaveAttribute(
      'href',
      '/huerto/subhuertos',
    )
    expect(screen.getByRole('link', { name: 'Bodega' })).toHaveAttribute(
      'href',
      '/bodega/inventario',
    )
    expect(screen.getByRole('link', { name: 'Insignias y Talentos' })).toHaveAttribute(
      'href',
      '/comunidad/talentos',
    )
    expect(screen.getByRole('link', { name: 'Subsanar Evidencias' })).toHaveAttribute(
      'href',
      '/actividades-ancestrales/observadas',
    )
    expect(screen.queryByRole('link', { name: 'Solicitudes' })).not.toBeInTheDocument()
    expect(screen.queryByRole('link', { name: 'Crear Usuario' })).not.toBeInTheDocument()

    await userEvent.click(screen.getByRole('button', { name: 'Cerrar menú' }))
    expect(screen.queryByRole('link', { name: 'Mi Comunidad' })).not.toBeInTheDocument()
  })

  it('muestra las opciones de TECNICO_MSP al abrir Mi Panel', async () => {
    guardarSesion({
      token: 'token-msp',
      rol: 'TECNICO_MSP',
      nombreCompleto: 'Carlos Vega',
    })

    render(
      <MemoryRouter>
        <Layout>Contenido</Layout>
      </MemoryRouter>,
    )

    await userEvent.click(screen.getByRole('button', { name: /mi panel/i }))

    expect(screen.getByRole('link', { name: 'Solicitudes' })).toHaveAttribute('href', '/solicitudes')
    expect(screen.getByRole('link', { name: 'Revisión Actividades' })).toHaveAttribute(
      'href',
      '/msp/revision-actividades',
    )
    expect(screen.getByRole('link', { name: 'Nueva Planta' })).toHaveAttribute(
      'href',
      '/admin/plantas/nueva',
    )
    expect(screen.queryByRole('link', { name: 'Mi Comunidad' })).not.toBeInTheDocument()
    expect(screen.queryByRole('link', { name: 'Crear Usuario' })).not.toBeInTheDocument()
    expect(screen.queryByRole('link', { name: 'Configurar Huerto' })).not.toBeInTheDocument()
    expect(screen.queryByRole('link', { name: 'Subhuertos' })).not.toBeInTheDocument()
  })

  it('muestra las opciones de TECNICO_SIS y permite cerrar sesión', async () => {
    guardarSesion({
      token: 'token-sis',
      rol: 'TECNICO_SIS',
      nombreCompleto: 'Ana Torres',
    })

    render(
      <MemoryRouter>
        <Layout>Contenido</Layout>
      </MemoryRouter>,
    )

    await userEvent.click(screen.getByRole('button', { name: /mi panel/i }))
    expect(screen.getByRole('link', { name: 'Crear Usuario' })).toHaveAttribute(
      'href',
      '/usuarios/nuevo',
    )

    await userEvent.click(screen.getByRole('button', { name: 'Cerrar sesión' }))

    expect(localStorage.getItem('token')).toBeNull()
    expect(screen.getByRole('link', { name: 'Iniciar sesión' })).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /mi panel/i })).not.toBeInTheDocument()
  })

  it('renderiza el contenido hijo', () => {
    render(
      <MemoryRouter>
        <Layout>
          <p>Contenido de prueba</p>
        </Layout>
      </MemoryRouter>,
    )

    expect(screen.getByText('Contenido de prueba')).toBeInTheDocument()
  })
})
