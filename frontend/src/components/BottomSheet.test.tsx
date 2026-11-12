import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { useState } from 'react'
import { BottomSheet } from './BottomSheet'

function PanelDePrueba() {
  const [abierto, setAbierto] = useState(false)
  return (
    <>
      <button onClick={() => setAbierto(true)}>Abrir filtros</button>
      <BottomSheet
        abierto={abierto}
        titulo="Filtros"
        alCerrar={() => setAbierto(false)}
        pie={<button>Aplicar filtros</button>}
      >
        <p>Contenido del panel</p>
      </BottomSheet>
    </>
  )
}

async function abrir() {
  render(<PanelDePrueba />)
  await userEvent.click(screen.getByRole('button', { name: 'Abrir filtros' }))
  return screen.getByRole('dialog', { name: 'Filtros' })
}

describe('BottomSheet', () => {
  it('no muestra nada cuando está cerrado', () => {
    render(<PanelDePrueba />)

    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
  })

  it('muestra el diálogo modal con su título, contenido y pie cuando está abierto', async () => {
    const dialogo = await abrir()

    expect(dialogo).toHaveAttribute('aria-modal', 'true')
    expect(screen.getByRole('heading', { name: 'Filtros' })).toBeInTheDocument()
    expect(screen.getByText('Contenido del panel')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Aplicar filtros' })).toBeInTheDocument()
  })

  it('lleva el foco adentro al abrir', async () => {
    const dialogo = await abrir()

    expect(dialogo).toContainElement(document.activeElement as HTMLElement)
  })

  it('se cierra con Escape y devuelve el foco al botón que lo abrió', async () => {
    await abrir()

    await userEvent.keyboard('{Escape}')

    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Abrir filtros' })).toHaveFocus()
  })

  it('se cierra al tocar el fondo pero no al tocar el panel', async () => {
    const dialogo = await abrir()

    await userEvent.click(screen.getByText('Contenido del panel'))
    expect(screen.getByRole('dialog')).toBeInTheDocument()

    await userEvent.click(dialogo.parentElement as HTMLElement)
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Abrir filtros' })).toHaveFocus()
  })

  it('se cierra con el botón Cerrar', async () => {
    await abrir()

    await userEvent.click(screen.getByRole('button', { name: 'Cerrar' }))

    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
  })

  it('bloquea el desplazamiento de la página mientras está abierto', async () => {
    await abrir()
    expect(document.body.style.overflow).toBe('hidden')

    await userEvent.keyboard('{Escape}')

    expect(document.body.style.overflow).toBe('')
  })
})
