import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { Modal } from './Modal'

function fondo() {
  const elemento = screen.getByRole('dialog').parentElement
  if (!elemento) throw new Error('El diálogo no tiene fondo')
  return elemento
}

describe('Modal', () => {
  it('muestra el título, el contenido y las acciones cuando está abierto', () => {
    render(
      <Modal abierto titulo="Registrar miembro" alCancelar={vi.fn()} acciones={<button>Guardar</button>}>
        <p>Formulario</p>
      </Modal>
    )

    expect(screen.getByRole('dialog', { name: 'Registrar miembro' })).toBeInTheDocument()
    expect(screen.getByText('Formulario')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Guardar' })).toBeInTheDocument()
  })

  it('avisa al pulsar Cancelar', async () => {
    const alCancelar = vi.fn()
    render(
      <Modal abierto titulo="Registrar miembro" alCancelar={alCancelar}>
        <p>Formulario</p>
      </Modal>
    )

    await userEvent.click(screen.getByRole('button', { name: 'Cancelar' }))

    expect(alCancelar).toHaveBeenCalledOnce()
  })

  it('sin las opciones de cierre no avisa con Escape ni al tocar el fondo', async () => {
    const alCancelar = vi.fn()
    render(
      <Modal abierto titulo="Registrar miembro" alCancelar={alCancelar}>
        <p>Formulario</p>
      </Modal>
    )

    await userEvent.keyboard('{Escape}')
    await userEvent.click(fondo())

    expect(alCancelar).not.toHaveBeenCalled()
    expect(screen.getByRole('dialog', { name: 'Registrar miembro' })).toBeInTheDocument()
  })

  it('con cerrarConEscape avisa al presionar Escape', async () => {
    const alCancelar = vi.fn()
    render(
      <Modal abierto titulo="Registrar miembro" alCancelar={alCancelar} cerrarConEscape>
        <p>Formulario</p>
      </Modal>
    )

    await userEvent.keyboard('{Escape}')

    expect(alCancelar).toHaveBeenCalledOnce()
  })

  it('con cerrarConEscape no avisa con otras teclas', async () => {
    const alCancelar = vi.fn()
    render(
      <Modal abierto titulo="Registrar miembro" alCancelar={alCancelar} cerrarConEscape>
        <p>Formulario</p>
      </Modal>
    )

    await userEvent.keyboard('{Enter}a')

    expect(alCancelar).not.toHaveBeenCalled()
  })

  it('con cerrarConEscape deja de escuchar la tecla al cerrarse', async () => {
    const alCancelar = vi.fn()
    const { rerender } = render(
      <Modal abierto titulo="Registrar miembro" alCancelar={alCancelar} cerrarConEscape>
        <p>Formulario</p>
      </Modal>
    )
    rerender(
      <Modal abierto={false} titulo="Registrar miembro" alCancelar={alCancelar} cerrarConEscape>
        <p>Formulario</p>
      </Modal>
    )

    await userEvent.keyboard('{Escape}')

    expect(alCancelar).not.toHaveBeenCalled()
  })

  it('con cerrarAlTocarFondo avisa al hacer clic en el fondo', async () => {
    const alCancelar = vi.fn()
    render(
      <Modal abierto titulo="Registrar miembro" alCancelar={alCancelar} cerrarAlTocarFondo>
        <p>Formulario</p>
      </Modal>
    )

    await userEvent.click(fondo())

    expect(alCancelar).toHaveBeenCalledOnce()
  })

  it('con cerrarAlTocarFondo no avisa al hacer clic dentro del diálogo', async () => {
    const alCancelar = vi.fn()
    render(
      <Modal abierto titulo="Registrar miembro" alCancelar={alCancelar} cerrarAlTocarFondo>
        <p>Formulario</p>
      </Modal>
    )

    await userEvent.click(screen.getByText('Formulario'))
    await userEvent.click(screen.getByRole('dialog'))

    expect(alCancelar).not.toHaveBeenCalled()
  })

  it('no muestra nada cuando está cerrado', () => {
    render(
      <Modal abierto={false} titulo="Registrar miembro" alCancelar={vi.fn()}>
        <p>Formulario</p>
      </Modal>
    )

    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
  })
})
