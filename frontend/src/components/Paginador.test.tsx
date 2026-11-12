import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { Paginador } from './Paginador'

function renderizar(pagina: number, totalPaginas: number) {
  const alCambiarPagina = vi.fn()
  const alCambiarTamano = vi.fn()
  render(
    <Paginador
      pagina={pagina}
      totalPaginas={totalPaginas}
      tamano={12}
      alCambiarPagina={alCambiarPagina}
      alCambiarTamano={alCambiarTamano}
    />,
  )
  return { alCambiarPagina, alCambiarTamano }
}

describe('Paginador', () => {
  it('muestra un botón por página y marca la actual', () => {
    renderizar(1, 3)

    expect(screen.getByRole('navigation', { name: 'Paginación' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Página 1' })).not.toHaveAttribute('aria-current')
    expect(screen.getByRole('button', { name: 'Página 2' })).toHaveAttribute('aria-current', 'page')
    expect(screen.getByRole('button', { name: 'Página 3' })).toBeInTheDocument()
  })

  it('avisa la página elegida empezando en cero', async () => {
    const { alCambiarPagina } = renderizar(0, 3)

    await userEvent.click(screen.getByRole('button', { name: 'Página 3' }))

    expect(alCambiarPagina).toHaveBeenCalledWith(2)
  })

  it('avisa la página anterior y la siguiente', async () => {
    const { alCambiarPagina } = renderizar(1, 3)

    await userEvent.click(screen.getByRole('button', { name: 'Anterior' }))
    await userEvent.click(screen.getByRole('button', { name: 'Siguiente' }))

    expect(alCambiarPagina).toHaveBeenNthCalledWith(1, 0)
    expect(alCambiarPagina).toHaveBeenNthCalledWith(2, 2)
  })

  it('deshabilita Anterior en la primera página y Siguiente en la última', () => {
    renderizar(0, 1)

    expect(screen.getByRole('button', { name: 'Anterior' })).toBeDisabled()
    expect(screen.getByRole('button', { name: 'Siguiente' })).toBeDisabled()
  })

  it('ofrece 12, 24 y 48 elementos por página y avisa el tamaño elegido', async () => {
    const { alCambiarTamano } = renderizar(0, 2)

    const selector = screen.getByLabelText('Por página')
    expect(selector).toHaveValue('12')
    expect(screen.getAllByRole('option', { name: /^\d+$/ }).map((opcion) => opcion.textContent)).toEqual([
      '12',
      '24',
      '48',
    ])

    await userEvent.selectOptions(selector, '24')

    expect(alCambiarTamano).toHaveBeenCalledWith(24)
  })
})
