import { act, fireEvent, render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { PanelFiltrosPlantas } from './PanelFiltrosPlantas'
import type { FiltrosPlanta } from './types'

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

function renderizar(filtros: FiltrosPlanta = {}) {
  const alCambiar = vi.fn()
  const usuario = userEvent.setup({ advanceTimers: vi.advanceTimersByTime })
  render(<PanelFiltrosPlantas filtros={filtros} alCambiar={alCambiar} />)
  return { alCambiar, usuario }
}

function grupo(nombre: string) {
  return within(screen.getByRole('group', { name: nombre }))
}

function avanzar(milisegundos: number) {
  act(() => {
    vi.advanceTimersByTime(milisegundos)
  })
}

describe('PanelFiltrosPlantas', () => {
  beforeEach(() => {
    vi.useFakeTimers({ shouldAdvanceTime: true })
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  describe('en móvil y tablet', () => {
    beforeEach(() => simularPantalla(false))

    it('muestra el botón Filtros sin barra lateral ni panel abierto', () => {
      renderizar()

      expect(screen.getByRole('button', { name: 'Filtros' })).toBeInTheDocument()
      expect(screen.queryByRole('complementary')).not.toBeInTheDocument()
      expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
    })

    it('muestra en el botón el número de filtros activos', () => {
      renderizar({ cualidad: 'FRIA', altitud: 2800 })

      const boton = screen.getByRole('button', { name: 'Filtros (2 activos)' })
      expect(within(boton).getByText('2')).toHaveClass('insignia')
    })

    it('abre los filtros en un panel inferior con las secciones', async () => {
      const { usuario } = renderizar({ ciclo: 'LARGO' })

      await usuario.click(screen.getByRole('button', { name: 'Filtros (1 activo)' }))

      const dialogo = within(screen.getByRole('dialog', { name: 'Filtros' }))
      expect(dialogo.getByRole('group', { name: 'Cualidad térmica' })).toBeInTheDocument()
      expect(dialogo.getByRole('spinbutton', { name: 'Altitud (msnm)' })).toBeInTheDocument()
      expect(dialogo.getByRole('slider', { name: 'Deslizador de altitud' })).toBeInTheDocument()
      expect(grupo('Ciclo de cultivo').getByRole('button', { name: 'Largo' })).toHaveAttribute('aria-pressed', 'true')
      expect(dialogo.getByRole('group', { name: 'Región' })).toBeInTheDocument()
      expect(dialogo.getByRole('group', { name: 'Categoría de uso' })).toBeInTheDocument()
    })

    it('no aplica el borrador hasta pulsar Aplicar filtros', async () => {
      const { alCambiar, usuario } = renderizar()
      await usuario.click(screen.getByRole('button', { name: 'Filtros' }))

      await usuario.click(grupo('Cualidad térmica').getByRole('button', { name: 'Fría' }))
      await usuario.click(grupo('Región').getByRole('button', { name: 'Amazonía' }))
      expect(grupo('Cualidad térmica').getByRole('button', { name: 'Fría' })).toHaveAttribute('aria-pressed', 'true')
      expect(alCambiar).not.toHaveBeenCalled()

      await usuario.click(screen.getByRole('button', { name: 'Aplicar filtros' }))

      expect(alCambiar).toHaveBeenCalledTimes(1)
      expect(alCambiar).toHaveBeenCalledWith({ cualidad: 'FRIA', region: 'Amazonía' })
      expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
    })

    it('descarta el borrador al cerrar sin aplicar', async () => {
      const { alCambiar, usuario } = renderizar()
      await usuario.click(screen.getByRole('button', { name: 'Filtros' }))
      await usuario.click(grupo('Cualidad térmica').getByRole('button', { name: 'Fría' }))

      await usuario.keyboard('{Escape}')
      await usuario.click(screen.getByRole('button', { name: 'Filtros' }))

      expect(alCambiar).not.toHaveBeenCalled()
      expect(grupo('Cualidad térmica').getByRole('button', { name: 'Todas' })).toHaveAttribute('aria-pressed', 'true')
      expect(grupo('Cualidad térmica').getByRole('button', { name: 'Fría' })).toHaveAttribute('aria-pressed', 'false')
    })

    it('Limpiar todo vacía el borrador', async () => {
      const { alCambiar, usuario } = renderizar({ cualidad: 'FRIA', altitud: 2800 })
      await usuario.click(screen.getByRole('button', { name: 'Filtros (2 activos)' }))

      await usuario.click(screen.getByRole('button', { name: 'Limpiar todo' }))

      expect(grupo('Cualidad térmica').getByRole('button', { name: 'Todas' })).toHaveAttribute('aria-pressed', 'true')
      expect(screen.getByRole('spinbutton', { name: 'Altitud (msnm)' })).toHaveValue(null)
      expect(alCambiar).not.toHaveBeenCalled()

      await usuario.click(screen.getByRole('button', { name: 'Aplicar filtros' }))

      expect(alCambiar).toHaveBeenCalledWith({})
    })

    it('no permite aplicar una altitud fuera de rango', async () => {
      const { usuario } = renderizar()
      await usuario.click(screen.getByRole('button', { name: 'Filtros' }))

      await usuario.type(screen.getByRole('spinbutton', { name: 'Altitud (msnm)' }), '7000')

      expect(screen.getByText('La altitud debe estar entre 0 y 6000 msnm')).toBeInTheDocument()
      expect(screen.getByRole('button', { name: 'Aplicar filtros' })).toBeDisabled()
    })
  })

  describe('en escritorio', () => {
    beforeEach(() => simularPantalla(true))

    it('muestra la barra lateral sin botón Filtros ni panel inferior', () => {
      renderizar()

      const lateral = screen.getByRole('complementary', { name: 'Filtros' })
      expect(lateral.tagName).toBe('ASIDE')
      expect(lateral).toHaveClass('catalogo-sidebar')
      expect(screen.getByRole('heading', { name: 'Filtros' })).toBeInTheDocument()
      expect(screen.queryByRole('button', { name: /^Filtros/ })).not.toBeInTheDocument()
      expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
    })

    it('marca con aria-pressed la opción elegida de cada grupo', () => {
      renderizar({ cualidad: 'CALIENTE', ciclo: 'CORTO', region: 'Sierra', categoria: 'SEDANTE' })

      expect(grupo('Cualidad térmica').getByRole('button', { name: 'Caliente' })).toHaveAttribute('aria-pressed', 'true')
      expect(grupo('Cualidad térmica').getByRole('button', { name: 'Todas' })).toHaveAttribute('aria-pressed', 'false')
      expect(grupo('Ciclo de cultivo').getByRole('button', { name: 'Corto' })).toHaveAttribute('aria-pressed', 'true')
      expect(grupo('Ciclo de cultivo').getByRole('button', { name: 'Largo' })).toHaveAttribute('aria-pressed', 'false')
      expect(grupo('Región').getByRole('button', { name: 'Sierra' })).toHaveAttribute('aria-pressed', 'true')
      expect(grupo('Categoría de uso').getByRole('button', { name: 'Sedante' })).toHaveAttribute('aria-pressed', 'true')
      expect(grupo('Categoría de uso').getByRole('button', { name: 'Diurética' })).toHaveAttribute('aria-pressed', 'false')
    })

    it('aplica al momento cada opción elegida', async () => {
      const { alCambiar, usuario } = renderizar({ ciclo: 'LARGO' })

      await usuario.click(grupo('Cualidad térmica').getByRole('button', { name: 'Fría' }))
      expect(alCambiar).toHaveBeenLastCalledWith({ ciclo: 'LARGO', cualidad: 'FRIA' })

      await usuario.click(grupo('Región').getByRole('button', { name: 'Amazonía' }))
      expect(alCambiar).toHaveBeenLastCalledWith({ ciclo: 'LARGO', region: 'Amazonía' })

      await usuario.click(grupo('Categoría de uso').getByRole('button', { name: 'Diurética' }))
      expect(alCambiar).toHaveBeenLastCalledWith({ ciclo: 'LARGO', categoria: 'DIURETICA' })

      await usuario.click(grupo('Ciclo de cultivo').getByRole('button', { name: 'Todos' }))
      expect(alCambiar).toHaveBeenLastCalledWith({ ciclo: undefined })
    })

    it('Limpiar todo quita todos los filtros', async () => {
      const { alCambiar, usuario } = renderizar({ cualidad: 'FRIA', categoria: 'SEDANTE', altitud: 2800 })

      await usuario.click(screen.getByRole('button', { name: 'Limpiar todo' }))

      expect(alCambiar).toHaveBeenCalledWith({})
    })

    it('aplica la altitud escrita después de una pausa y la sincroniza con el deslizador', async () => {
      const { alCambiar, usuario } = renderizar()

      await usuario.type(screen.getByRole('spinbutton', { name: 'Altitud (msnm)' }), '2800')
      expect(screen.getByRole('slider', { name: 'Deslizador de altitud' })).toHaveValue('2800')
      expect(alCambiar).not.toHaveBeenCalled()

      avanzar(500)

      expect(alCambiar).toHaveBeenCalledTimes(1)
      expect(alCambiar).toHaveBeenCalledWith({ altitud: 2800 })
    })

    it('aplica la altitud elegida con el deslizador y la muestra en el campo', () => {
      const { alCambiar } = renderizar()

      fireEvent.change(screen.getByRole('slider', { name: 'Deslizador de altitud' }), { target: { value: '3600' } })
      expect(screen.getByRole('spinbutton', { name: 'Altitud (msnm)' })).toHaveValue(3600)

      avanzar(500)

      expect(alCambiar).toHaveBeenCalledWith({ altitud: 3600 })
    })

    it('borrar la altitud quita el filtro', async () => {
      const { alCambiar, usuario } = renderizar({ altitud: 2800 })

      await usuario.clear(screen.getByRole('spinbutton', { name: 'Altitud (msnm)' }))
      avanzar(500)

      expect(alCambiar).toHaveBeenCalledWith({ altitud: undefined })
    })

    it('avisa y no aplica una altitud fuera de rango', async () => {
      const { alCambiar, usuario } = renderizar()
      const campo = screen.getByRole('spinbutton', { name: 'Altitud (msnm)' })

      await usuario.type(campo, '7000')
      avanzar(500)

      expect(screen.getByText('La altitud debe estar entre 0 y 6000 msnm')).toBeInTheDocument()
      expect(campo).toHaveAttribute('aria-invalid', 'true')
      expect(alCambiar).not.toHaveBeenCalled()
    })
  })
})
