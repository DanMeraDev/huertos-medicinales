import { render, screen } from '@testing-library/react'
import { Layout } from './Layout'

describe('Layout', () => {
  it('muestra el nombre del sistema en el encabezado', () => {
    render(<Layout>Contenido</Layout>)

    expect(screen.getByRole('banner')).toHaveTextContent('Huertos Medicinales Comunitarios')
  })

  it('muestra los modulos del incremento 1 en el menu de navegacion', () => {
    render(<Layout>Contenido</Layout>)

    const navegacion = screen.getByRole('navigation', { name: /navegación principal/i })
    expect(navegacion).toBeInTheDocument()

    expect(screen.getByRole('link', { name: 'Acceso' })).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Comunidades y Miembros' })).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Huerto Real' })).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Catálogo de Plantas' })).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Bodega' })).toBeInTheDocument()
  })

  it('renderiza el contenido hijo', () => {
    render(
      <Layout>
        <p>Contenido de prueba</p>
      </Layout>
    )

    expect(screen.getByText('Contenido de prueba')).toBeInTheDocument()
  })
})
