import { render, screen, within } from '@testing-library/react'
import { MemoryRouter } from 'react-router'
import { FichaTecnicaView } from './FichaTecnicaView'
import { achicoriaSinDatosDetalle, colaDeCaballoDetalle } from './plantasMockHandlers'
import type { PlantaDetalle } from './types'

function simularPantalla(esEscritorio: boolean) {
  Object.defineProperty(window, 'matchMedia', {
    configurable: true,
    writable: true,
    value: (consulta: string) => ({
      matches: esEscritorio && consulta === '(min-width: 1024px)',
      media: consulta,
      addEventListener: () => {},
      removeEventListener: () => {},
    }),
  })
}

function renderizar(planta: PlantaDetalle) {
  return render(
    <MemoryRouter>
      <FichaTecnicaView planta={planta} />
    </MemoryRouter>,
  )
}

function valorDe(parametro: string) {
  return screen.getByText(parametro, { selector: 'dt' }).nextElementSibling
}

describe('FichaTecnicaView', () => {
  beforeEach(() => simularPantalla(false))

  it('muestra el nombre común como título, el kichwa entre comillas y el científico en cursiva', () => {
    renderizar(colaDeCaballoDetalle)

    expect(screen.getByRole('heading', { level: 1, name: 'Cola de caballo' })).toBeInTheDocument()
    expect(screen.getByText('«Caballo chupa»')).toHaveAttribute('lang', 'qu')
    expect(screen.getByText('Equisetum giganteum').tagName).toBe('EM')
  })

  it('muestra la fotografía con la insignia térmica', () => {
    renderizar(colaDeCaballoDetalle)

    expect(screen.getByRole('img', { name: 'Fotografía de Cola de caballo' })).toHaveAttribute(
      'src',
      '/api/publico/fotos/plantas/cola-de-caballo.jpg',
    )
    expect(screen.getByText('Fría')).toHaveClass('insignia-exito')
  })

  it('muestra los parámetros de cultivo con sus etiquetas en español', () => {
    renderizar(colaDeCaballoDetalle)

    expect(valorDe('Altitud')).toHaveTextContent('0 – 3.500 msnm')
    expect(valorDe('Ciclo de cultivo')).toHaveTextContent('Largo')
    expect(valorDe('Familia botánica')).toHaveTextContent('Equisetaceae')
    expect(valorDe('Origen')).toHaveTextContent('Nativa')
    expect(valorDe('Regiones')).toHaveTextContent('Sierra, Amazonía')
  })

  it('muestra los usos terapéuticos con sus categorías', () => {
    renderizar(colaDeCaballoDetalle)

    const usos = within(screen.getByRole('region', { name: 'Usos terapéuticos' }))
    expect(usos.getByText('Diurética')).toHaveClass('insignia')
    expect(usos.getByText(colaDeCaballoDetalle.usosMedicinales)).toBeInTheDocument()
  })

  it('muestra la preparación tal cual, con sus saltos de línea', () => {
    renderizar(colaDeCaballoDetalle)

    const preparacion = screen.getByRole('region', { name: 'Preparación y dosificación' })
    const texto = within(preparacion).getByText(/Hervir el tallo/)
    expect(texto.textContent).toBe(colaDeCaballoDetalle.preparacionDosificacion)
    expect(texto).toHaveClass('ficha-tecnica-texto-largo')
  })

  it('muestra las contraindicaciones en un aside que no interrumpe al lector de pantalla', () => {
    renderizar(colaDeCaballoDetalle)

    const contraindicaciones = screen.getByRole('complementary', { name: 'Contraindicaciones y precauciones' })
    expect(contraindicaciones.tagName).toBe('ASIDE')
    expect(contraindicaciones).not.toHaveAttribute('role')
    expect(contraindicaciones).toHaveTextContent(colaDeCaballoDetalle.contraindicaciones ?? '')
    expect(screen.queryByRole('alert')).not.toBeInTheDocument()
  })

  describe('con campos sin registrar', () => {
    it('muestra "No registrado" y no inventa datos', () => {
      renderizar(achicoriaSinDatosDetalle)

      expect(valorDe('Altitud')).toHaveTextContent('No registrado')
      expect(valorDe('Ciclo de cultivo')).toHaveTextContent('No registrado')
      expect(valorDe('Origen')).toHaveTextContent('No registrado')
      expect(valorDe('Regiones')).toHaveTextContent('No registrado')
      expect(
        within(screen.getByRole('region', { name: 'Preparación y dosificación' })).getByText('No registrado'),
      ).toBeInTheDocument()
    })

    it('no muestra comillas de nombre kichwa', () => {
      renderizar(achicoriaSinDatosDetalle)

      expect(screen.getByRole('heading', { level: 1 })).toHaveTextContent('Achicoria')
      expect(screen.queryByText(/[«»]/)).not.toBeInTheDocument()
    })

    it('avisa que no hay contraindicaciones registradas', () => {
      renderizar(achicoriaSinDatosDetalle)

      expect(screen.getByRole('complementary', { name: 'Contraindicaciones y precauciones' })).toHaveTextContent(
        'Sin información registrada sobre contraindicaciones',
      )
    })

    it('muestra el marcador de fotografía', () => {
      renderizar(achicoriaSinDatosDetalle)

      expect(screen.getByRole('img', { name: 'Sin fotografía de Achicoria' })).toBeInTheDocument()
    })
  })

  it.each([
    [2200, 3000, '2.200 – 3.000 msnm'],
    [2200, null, 'Desde 2.200 msnm'],
    [null, 3000, 'Hasta 3.000 msnm'],
    [null, null, 'No registrado'],
  ])('con altitud mínima %s y máxima %s muestra "%s"', (altitudMinMsnm, altitudMaxMsnm, esperado) => {
    renderizar({ ...colaDeCaballoDetalle, altitudMinMsnm, altitudMaxMsnm })

    expect(valorDe('Altitud')).toHaveTextContent(esperado)
  })

  describe('en móvil', () => {
    it('muestra una sola columna con el botón circular y el botón inferior de volver', () => {
      const { container } = renderizar(colaDeCaballoDetalle)

      expect(container.querySelector('.ficha-tecnica-movil')).toBeInTheDocument()
      expect(container.querySelectorAll('.ficha-tecnica-columna')).toHaveLength(0)
      expect(screen.queryByRole('navigation', { name: 'Migas de pan' })).not.toBeInTheDocument()
      const volver = screen.getAllByRole('link', { name: 'Volver al catálogo' })
      expect(volver).toHaveLength(2)
      expect(volver[0]).toHaveClass('ficha-tecnica-volver-circular')
      expect(volver[1]).toHaveClass('ficha-tecnica-volver-inferior')
      volver.forEach((enlace) => expect(enlace).toHaveAttribute('href', '/catalogo-plantas'))
    })

    it('muestra los parámetros de cultivo en una fila deslizable', () => {
      const { container } = renderizar(colaDeCaballoDetalle)

      expect(container.querySelector('.ficha-tecnica-parametros-fila')).toBeInTheDocument()
    })
  })

  describe('en escritorio', () => {
    beforeEach(() => simularPantalla(true))

    it('muestra dos columnas con las migas de pan y el botón de volver', () => {
      const { container } = renderizar(colaDeCaballoDetalle)

      expect(container.querySelector('.ficha-tecnica-escritorio')).toBeInTheDocument()
      expect(container.querySelectorAll('.ficha-tecnica-columna')).toHaveLength(2)
      const migas = within(screen.getByRole('navigation', { name: 'Migas de pan' }))
      expect(migas.getByRole('link', { name: 'Catálogo de Plantas' })).toHaveAttribute('href', '/catalogo-plantas')
      expect(migas.getByText('Cola de caballo')).toHaveAttribute('aria-current', 'page')
      const volver = screen.getByRole('link', { name: 'Volver al catálogo' })
      expect(volver).toHaveAttribute('href', '/catalogo-plantas')
      expect(volver).toHaveClass('boton', 'boton-secundario')
    })

    it('muestra los parámetros de cultivo en una cuadrícula dentro de una tarjeta', () => {
      const { container } = renderizar(colaDeCaballoDetalle)

      expect(container.querySelector('.tarjeta .ficha-tecnica-parametros-cuadricula')).toBeInTheDocument()
      expect(container.querySelector('.ficha-tecnica-parametros-fila')).not.toBeInTheDocument()
    })

    it('ubica la foto y el cultivo a la izquierda y la ficha médica a la derecha', () => {
      const { container } = renderizar(colaDeCaballoDetalle)

      const [izquierda, derecha] = container.querySelectorAll<HTMLElement>('.ficha-tecnica-columna')
      expect(within(izquierda).getByRole('img', { name: 'Fotografía de Cola de caballo' })).toBeInTheDocument()
      expect(within(izquierda).getByText('Altitud')).toBeInTheDocument()
      expect(within(derecha).getByRole('heading', { level: 1 })).toBeInTheDocument()
      expect(within(derecha).getByRole('region', { name: 'Usos terapéuticos' })).toBeInTheDocument()
      expect(
        within(derecha).getByRole('complementary', { name: 'Contraindicaciones y precauciones' }),
      ).toBeInTheDocument()
    })
  })
})
