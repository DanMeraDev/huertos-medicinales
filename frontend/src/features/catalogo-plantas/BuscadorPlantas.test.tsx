import { act, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { BuscadorPlantas } from './BuscadorPlantas'

function renderizar() {
  const alBuscar = vi.fn()
  const usuario = userEvent.setup({ advanceTimers: vi.advanceTimersByTime })
  render(
    <>
      <input aria-label="Otro campo" />
      <BuscadorPlantas alBuscar={alBuscar} />
    </>,
  )
  return { alBuscar, usuario, buscador: screen.getByRole('searchbox', { name: 'Buscar planta' }) }
}

function avanzar(milisegundos: number) {
  act(() => {
    vi.advanceTimersByTime(milisegundos)
  })
}

describe('BuscadorPlantas', () => {
  beforeEach(() => {
    vi.useFakeTimers({ shouldAdvanceTime: true })
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('muestra el campo de búsqueda con su teclado y su guía de uso', () => {
    const { buscador } = renderizar()

    expect(buscador).toHaveAttribute('type', 'search')
    expect(buscador).toHaveAttribute('enterKeyHint', 'search')
    expect(buscador).toHaveAttribute('placeholder', 'Nombre común, científico o kichwa')
    expect(screen.queryByRole('button', { name: 'Borrar búsqueda' })).not.toBeInTheDocument()
  })

  it('avisa el criterio solo después de 300 ms sin escribir', async () => {
    const { alBuscar, usuario, buscador } = renderizar()

    await usuario.type(buscador, 'mat')
    avanzar(200)
    expect(alBuscar).not.toHaveBeenCalled()

    avanzar(100)
    expect(alBuscar).toHaveBeenCalledTimes(1)
    expect(alBuscar).toHaveBeenCalledWith('mat')
  })

  it('reinicia la espera con cada tecla y avisa solo el último criterio', async () => {
    const { alBuscar, usuario, buscador } = renderizar()

    await usuario.type(buscador, 'ma')
    avanzar(200)
    await usuario.type(buscador, 't')
    avanzar(200)
    expect(alBuscar).not.toHaveBeenCalled()

    avanzar(100)
    expect(alBuscar).toHaveBeenCalledTimes(1)
    expect(alBuscar).toHaveBeenCalledWith('mat')
  })

  it('con un solo carácter muestra la guía y no busca', async () => {
    const { alBuscar, usuario, buscador } = renderizar()

    await usuario.type(buscador, 'm')
    avanzar(300)

    expect(screen.getByText('Escribe al menos 2 caracteres...')).toBeInTheDocument()
    expect(alBuscar).not.toHaveBeenCalled()
  })

  it('oculta la guía al llegar a dos caracteres', async () => {
    const { usuario, buscador } = renderizar()

    await usuario.type(buscador, 'ma')

    expect(screen.queryByText('Escribe al menos 2 caracteres...')).not.toBeInTheDocument()
  })

  it('avisa el criterio sin espacios al inicio ni al final', async () => {
    const { alBuscar, usuario, buscador } = renderizar()

    await usuario.type(buscador, '  chupa  ')
    avanzar(300)

    expect(alBuscar).toHaveBeenCalledWith('chupa')
  })

  it('avisa un criterio vacío al borrar todo el texto', async () => {
    const { alBuscar, usuario, buscador } = renderizar()
    await usuario.type(buscador, 'ma')
    avanzar(300)

    await usuario.clear(buscador)
    avanzar(300)

    expect(alBuscar).toHaveBeenLastCalledWith('')
  })

  it('la X limpia el texto y avisa un criterio vacío', async () => {
    const { alBuscar, usuario, buscador } = renderizar()
    await usuario.type(buscador, 'matico')
    avanzar(300)

    await usuario.click(screen.getByRole('button', { name: 'Borrar búsqueda' }))

    expect(buscador).toHaveValue('')
    expect(alBuscar).toHaveBeenLastCalledWith('')
    expect(screen.queryByRole('button', { name: 'Borrar búsqueda' })).not.toBeInTheDocument()
    avanzar(300)
    expect(alBuscar).toHaveBeenLastCalledWith('')
  })

  it('enfoca el buscador al pulsar / desde la página sin escribir la barra', async () => {
    const { usuario, buscador } = renderizar()

    await usuario.keyboard('/')

    expect(buscador).toHaveFocus()
    expect(buscador).toHaveValue('')
  })

  it('no enfoca el buscador si se pulsa / con Ctrl', async () => {
    const { usuario, buscador } = renderizar()

    await usuario.keyboard('{Control>}/{/Control}')

    expect(buscador).not.toHaveFocus()
  })

  it('no roba el foco al escribir / dentro de otro campo', async () => {
    const { usuario, buscador } = renderizar()
    const otroCampo = screen.getByRole('textbox', { name: 'Otro campo' })

    await usuario.type(otroCampo, 'a/b')

    expect(otroCampo).toHaveFocus()
    expect(otroCampo).toHaveValue('a/b')
    expect(buscador).toHaveValue('')
  })
})
