import { render, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { useState } from 'react'
import { MemoryRouter } from 'react-router'
import { server } from '../../test/mocks/server'
import { ModalConfirmacionBaja } from './ModalConfirmacionBaja'

const TITULO = 'Confirmar baja de planta medicinal'
const MOTIVO_OBLIGATORIO = 'El motivo de la baja es obligatorio'

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

interface EspiasConfirmacion {
  alCerrar: () => void
  alDarDeBaja: () => void
}

function ConfirmacionDePrueba({ alCerrar, alDarDeBaja }: EspiasConfirmacion) {
  const [abierto, setAbierto] = useState(true)
  return (
    <>
      <button onClick={() => setAbierto(true)}>Abrir confirmación</button>
      <ModalConfirmacionBaja
        plantaId={7}
        nombrePlanta="Hierba editable"
        abierto={abierto}
        alCerrar={() => {
          alCerrar()
          setAbierto(false)
        }}
        alDarDeBaja={alDarDeBaja}
      />
    </>
  )
}

function renderizar() {
  const espias = { alCerrar: vi.fn(), alDarDeBaja: vi.fn() }
  render(
    <MemoryRouter>
      <ConfirmacionDePrueba {...espias} />
    </MemoryRouter>,
  )
  return espias
}

function registrarPeticiones() {
  const peticiones = { cantidad: 0, ruta: '', cuerpo: null as unknown }
  server.use(
    http.patch('*/api/msp/plantas/:id/baja', async ({ request }) => {
      peticiones.cantidad += 1
      peticiones.ruta = new URL(request.url).pathname
      peticiones.cuerpo = await request.clone().json()
      return undefined
    }),
  )
  return peticiones
}

function responderBaja(estado: number, cuerpo: object) {
  server.use(http.patch('*/api/msp/plantas/:id/baja', () => HttpResponse.json(cuerpo, { status: estado })))
}

function dialogo() {
  return screen.getByRole('dialog', { name: TITULO })
}

function campoMotivo() {
  return screen.getByRole('textbox', { name: 'Motivo de la baja' })
}

function fondo() {
  const elemento = dialogo().parentElement
  if (!elemento) throw new Error('El diálogo no tiene fondo')
  return elemento
}

async function confirmarConMotivo(motivo: string) {
  const usuario = userEvent.setup()
  await usuario.type(campoMotivo(), motivo)
  await usuario.click(screen.getByRole('button', { name: 'Sí, dar de baja' }))
}

describe('ModalConfirmacionBaja', () => {
  describe('en escritorio', () => {
    beforeEach(() => simularPantalla(true))

    it('muestra un modal centrado de 480 px con título, icono, contenido y botones', () => {
      renderizar()

      expect(dialogo()).toHaveClass('modal', 'confirmar-baja-planta')
      expect(within(dialogo()).getByRole('img', { name: 'Advertencia' })).toBeInTheDocument()
      expect(within(dialogo()).getByText('Hierba editable')).toBeInTheDocument()
      expect(
        within(dialogo()).getByText(
          'Las siembras y el material de bodega existentes se conservan, y la planta deja de aparecer en el catálogo público.',
        ),
      ).toBeInTheDocument()
      expect(campoMotivo()).toBeInTheDocument()
      const acciones = within(dialogo()).getAllByRole('button')
      expect(acciones.map((boton) => boton.textContent)).toEqual(['Cancelar', 'Sí, dar de baja'])
      expect(acciones[0]).toHaveClass('boton-secundario')
      expect(acciones[1]).toHaveClass('boton-peligro')
    })

    it('mientras envía deshabilita Sí, dar de baja y no se cierra con Escape', async () => {
      const usuario = userEvent.setup()
      let liberar = () => {}
      const espera = new Promise<void>((resolver) => {
        liberar = resolver
      })
      server.use(
        http.patch('*/api/msp/plantas/:id/baja', async () => {
          await espera
          return undefined
        }),
      )
      const { alCerrar, alDarDeBaja } = renderizar()

      await confirmarConMotivo('Registro duplicado')
      await waitFor(() => expect(screen.getByRole('button', { name: 'Sí, dar de baja' })).toBeDisabled())
      await usuario.keyboard('{Escape}')

      expect(alCerrar).not.toHaveBeenCalled()
      liberar()
      await waitFor(() => expect(alDarDeBaja).toHaveBeenCalledOnce())
    })
  })

  describe('en móvil', () => {
    beforeEach(() => simularPantalla(false))

    it('muestra un panel inferior con los botones apilados', () => {
      renderizar()

      expect(dialogo()).toHaveClass('bottom-sheet')
      expect(dialogo()).not.toHaveClass('modal')
      expect(within(dialogo()).getByText('Hierba editable')).toBeInTheDocument()
      const botones = screen.getByRole('button', { name: 'Sí, dar de baja' }).parentElement
      expect(botones).toHaveClass('confirmar-baja-planta-botones')
      const apilados = within(botones as HTMLElement).getAllByRole('button')
      expect(apilados.map((boton) => boton.textContent)).toEqual(['Sí, dar de baja', 'Cancelar'])
      expect(apilados[0]).toHaveClass('boton-peligro')
      expect(apilados[1]).toHaveClass('boton-secundario')
    })

    it('deshabilita los dos botones mientras envía', async () => {
      let liberar = () => {}
      const espera = new Promise<void>((resolver) => {
        liberar = resolver
      })
      server.use(
        http.patch('*/api/msp/plantas/:id/baja', async () => {
          await espera
          return undefined
        }),
      )
      const { alDarDeBaja } = renderizar()

      await confirmarConMotivo('Registro duplicado')

      await waitFor(() => expect(screen.getByRole('button', { name: 'Sí, dar de baja' })).toBeDisabled())
      expect(screen.getByRole('button', { name: 'Cancelar' })).toBeDisabled()
      liberar()
      await waitFor(() => expect(alDarDeBaja).toHaveBeenCalledOnce())
    })
  })

  describe.each([
    ['escritorio', true],
    ['móvil', false],
  ])('cierre sin enviar en %s', (_pantalla, esEscritorio) => {
    beforeEach(() => simularPantalla(esEscritorio))

    it.each([
      ['Cancelar', () => userEvent.click(screen.getByRole('button', { name: 'Cancelar' }))],
      ['Escape', () => userEvent.keyboard('{Escape}')],
      ['clic en el fondo', () => userEvent.click(fondo())],
    ])('con %s se cierra sin enviar y limpia el motivo', async (_forma, cerrar) => {
      const peticiones = registrarPeticiones()
      const { alCerrar, alDarDeBaja } = renderizar()
      await userEvent.type(campoMotivo(), 'Registro duplicado')

      await cerrar()

      expect(alCerrar).toHaveBeenCalledOnce()
      expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
      await userEvent.click(screen.getByRole('button', { name: 'Abrir confirmación' }))
      expect(campoMotivo()).toHaveValue('')
      expect(peticiones.cantidad).toBe(0)
      expect(alDarDeBaja).not.toHaveBeenCalled()
    })
  })

  describe('motivo y envío', () => {
    beforeEach(() => simularPantalla(true))

    it.each(['', '   '])('con el motivo "%s" marca el campo sin enviar', async (motivo) => {
      const usuario = userEvent.setup()
      const peticiones = registrarPeticiones()
      const { alDarDeBaja } = renderizar()
      if (motivo) await usuario.type(campoMotivo(), motivo)

      await usuario.click(screen.getByRole('button', { name: 'Sí, dar de baja' }))

      expect(campoMotivo()).toHaveAttribute('aria-invalid', 'true')
      expect(campoMotivo()).toHaveAccessibleDescription(MOTIVO_OBLIGATORIO)
      expect(peticiones.cantidad).toBe(0)
      expect(alDarDeBaja).not.toHaveBeenCalled()
    })

    it('muestra un contador de caracteres con máximo de 300', async () => {
      const usuario = userEvent.setup()
      renderizar()

      expect(screen.getByText('0/300')).toBeInTheDocument()
      await usuario.type(campoMotivo(), 'Hola')
      expect(screen.getByText('4/300')).toBeInTheDocument()

      expect(campoMotivo()).toHaveAttribute('maxLength', '300')
      await usuario.clear(campoMotivo())
      await usuario.click(campoMotivo())
      await usuario.paste('a'.repeat(305))
      expect(campoMotivo()).toHaveValue('a'.repeat(300))
      expect(screen.getByText('300/300')).toBeInTheDocument()
    })

    it('envía el motivo recortado y avisa la baja', async () => {
      const peticiones = registrarPeticiones()
      const { alCerrar, alDarDeBaja } = renderizar()

      await confirmarConMotivo('  Registro duplicado  ')

      await waitFor(() => expect(alDarDeBaja).toHaveBeenCalledOnce())
      expect(peticiones.ruta).toBe('/api/msp/plantas/7/baja')
      expect(peticiones.cuerpo).toEqual({ motivo: 'Registro duplicado' })
      expect(alCerrar).not.toHaveBeenCalled()
    })

    it('marca el campo con el error por campo del back', async () => {
      responderBaja(400, {
        status: 400,
        detail: 'Datos no válidos',
        errores: { motivo: 'El motivo de la baja no puede superar los 300 caracteres' },
      })
      const { alDarDeBaja } = renderizar()

      await confirmarConMotivo('Registro duplicado')

      await waitFor(() =>
        expect(campoMotivo()).toHaveAccessibleDescription('El motivo de la baja no puede superar los 300 caracteres'),
      )
      expect(screen.queryByRole('alert')).not.toBeInTheDocument()
      expect(alDarDeBaja).not.toHaveBeenCalled()
    })

    it.each([
      [400, 'La planta ya está dada de baja'],
      [404, 'La planta no existe'],
    ])('con un %s muestra el mensaje del back dentro del diálogo', async (estado, detalle) => {
      responderBaja(estado, { status: estado, detail: detalle })
      const { alDarDeBaja } = renderizar()

      await confirmarConMotivo('Registro duplicado')

      expect(await within(dialogo()).findByRole('alert')).toHaveTextContent(detalle)
      expect(alDarDeBaja).not.toHaveBeenCalled()
      expect(screen.getByRole('button', { name: 'Sí, dar de baja' })).toBeEnabled()
    })

    it.each([401, 403])('con un %s pide iniciar sesión como Técnico MSP', async (estado) => {
      responderBaja(estado, { status: estado })
      const { alDarDeBaja } = renderizar()

      await confirmarConMotivo('Registro duplicado')

      expect(await within(dialogo()).findByRole('alert')).toHaveTextContent(
        'Necesitas iniciar sesión como Técnico MSP',
      )
      expect(within(dialogo()).getByRole('link', { name: 'Iniciar sesión' })).toHaveAttribute('href', '/login')
      expect(alDarDeBaja).not.toHaveBeenCalled()
    })

    it('con un error inesperado muestra un mensaje genérico', async () => {
      server.use(http.patch('*/api/msp/plantas/:id/baja', () => new HttpResponse(null, { status: 500 })))
      const { alDarDeBaja } = renderizar()

      await confirmarConMotivo('Registro duplicado')

      expect(await within(dialogo()).findByRole('alert')).toHaveTextContent(
        'No se pudo dar de baja la planta. Intente nuevamente',
      )
      expect(alDarDeBaja).not.toHaveBeenCalled()
    })
  })
})
