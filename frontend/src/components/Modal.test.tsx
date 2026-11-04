import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { Modal } from './Modal'

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

  it('no muestra nada cuando está cerrado', () => {
    render(
      <Modal abierto={false} titulo="Registrar miembro" alCancelar={vi.fn()}>
        <p>Formulario</p>
      </Modal>
    )

    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
  })
})
