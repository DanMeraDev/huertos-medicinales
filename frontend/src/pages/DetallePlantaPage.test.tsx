import { act, render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { Link, MemoryRouter, Route, Routes } from 'react-router'
import { colaDeCaballoDetalle } from '../features/catalogo-plantas/plantasMockHandlers'
import { server } from '../test/mocks/server'
import { CatalogoPlantasPage } from './CatalogoPlantasPage'
import { DetallePlantaPage } from './DetallePlantaPage'

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

function renderizarEn(ruta: string) {
  render(
    <MemoryRouter initialEntries={[ruta]}>
      <Link to="/catalogo-plantas/14">Ir a Achicoria</Link>
      <Routes>
        <Route path="/catalogo-plantas/:id" element={<DetallePlantaPage />} />
      </Routes>
    </MemoryRouter>,
  )
}

function renderizarConEstado(estado: unknown) {
  render(
    <MemoryRouter initialEntries={[{ pathname: '/catalogo-plantas/17', state: estado }]}>
      <Routes>
        <Route path="/catalogo-plantas/:id" element={<DetallePlantaPage />} />
      </Routes>
    </MemoryRouter>,
  )
}

function renderizarConCatalogo(ruta: string) {
  render(
    <MemoryRouter initialEntries={[ruta]}>
      <Routes>
        <Route path="/catalogo-plantas" element={<CatalogoPlantasPage />} />
        <Route path="/catalogo-plantas/:id" element={<DetallePlantaPage />} />
      </Routes>
    </MemoryRouter>,
  )
}

function base64Url(datos: object) {
  return btoa(JSON.stringify(datos)).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '')
}

function iniciarSesionComo(rol: string) {
  const payload = { sub: 'usuario@huertos.gob.ec', rol, exp: Math.floor(Date.now() / 1000) + 3600 }
  localStorage.setItem('token', `${base64Url({ alg: 'HS256' })}.${base64Url(payload)}.firma`)
}

function contarPeticionesDeDetalle() {
  const contador = { peticiones: 0 }
  server.use(
    http.get('*/api/publico/plantas/:id', () => {
      contador.peticiones += 1
      return undefined
    }),
  )
  return contador
}

describe('DetallePlantaPage', () => {
  beforeEach(() => simularPantalla(false))

  it('avisa mientras carga la ficha', () => {
    renderizarEn('/catalogo-plantas/17')

    expect(screen.getByText('Cargando ficha…')).toBeInTheDocument()
  })

  it('carga la ficha técnica de la planta desde el servidor', async () => {
    renderizarEn('/catalogo-plantas/17')

    expect(await screen.findByRole('heading', { level: 1, name: 'Cola de caballo' })).toBeInTheDocument()
    expect(screen.getByText(colaDeCaballoDetalle.usosMedicinales)).toBeInTheDocument()
    expect(screen.queryByText('Cargando ficha…')).not.toBeInTheDocument()
  })

  it('con un id desconocido avisa que la ficha no está disponible y ofrece volver', async () => {
    renderizarEn('/catalogo-plantas/999')

    const alerta = await screen.findByRole('alert')
    expect(alerta).toHaveTextContent('La ficha solicitada no está disponible')
    expect(alerta).toHaveClass('notificacion-error')
    expect(screen.getByRole('link', { name: 'Volver al catálogo' })).toHaveAttribute('href', '/catalogo-plantas')
  })

  it.each(['abc', '0', '-3', '1.5'])('con el id "%s" avisa que no está disponible sin consultar', async (id) => {
    const contador = contarPeticionesDeDetalle()
    renderizarEn(`/catalogo-plantas/${id}`)

    expect(await screen.findByRole('alert')).toHaveTextContent('La ficha solicitada no está disponible')
    expect(screen.getByRole('link', { name: 'Volver al catálogo' })).toHaveAttribute('href', '/catalogo-plantas')
    await act(async () => {
      await new Promise((resolver) => setTimeout(resolver, 50))
    })
    expect(contador.peticiones).toBe(0)
  })

  it('con un error del servidor muestra un mensaje genérico y ofrece volver', async () => {
    server.use(http.get('*/api/publico/plantas/:id', () => new HttpResponse(null, { status: 500 })))
    renderizarEn('/catalogo-plantas/17')

    const alerta = await screen.findByRole('alert')
    expect(alerta).toHaveTextContent('No se pudo cargar la ficha. Intente nuevamente más tarde')
    expect(screen.getByRole('link', { name: 'Volver al catálogo' })).toHaveAttribute('href', '/catalogo-plantas')
  })

  it('descarta la respuesta de la planta anterior al cambiar de id', async () => {
    let responderAnterior: () => void = () => {}
    server.use(
      http.get('*/api/publico/plantas/17', async () => {
        await new Promise<void>((resolver) => {
          responderAnterior = resolver
        })
        return HttpResponse.json(colaDeCaballoDetalle)
      }),
    )
    renderizarEn('/catalogo-plantas/17')

    await userEvent.click(screen.getByRole('link', { name: 'Ir a Achicoria' }))
    expect(await screen.findByRole('heading', { level: 1, name: 'Achicoria' })).toBeInTheDocument()
    await act(async () => {
      responderAnterior()
      await new Promise((resolver) => setTimeout(resolver, 50))
    })

    expect(screen.getByRole('heading', { level: 1, name: 'Achicoria' })).toBeInTheDocument()
    expect(screen.queryByRole('heading', { level: 1, name: 'Cola de caballo' })).not.toBeInTheDocument()
  })

  describe('acceso a la edición de la ficha', () => {
    afterEach(() => localStorage.clear())

    it('muestra Editar ficha al técnico MSP con el enlace a la edición', async () => {
      iniciarSesionComo('TECNICO_MSP')
      renderizarEn('/catalogo-plantas/17')

      expect(await screen.findByRole('link', { name: 'Editar ficha' })).toHaveAttribute(
        'href',
        '/admin/plantas/17/editar',
      )
      expect(screen.getByRole('link', { name: 'Editar ficha' })).toHaveClass('boton', 'boton-primario')
    })

    it('no muestra Editar ficha sin sesión', async () => {
      renderizarEn('/catalogo-plantas/17')

      expect(await screen.findByRole('heading', { level: 1, name: 'Cola de caballo' })).toBeInTheDocument()
      expect(screen.queryByRole('link', { name: 'Editar ficha' })).not.toBeInTheDocument()
    })

    it('no muestra Editar ficha al responsable de comunidad', async () => {
      iniciarSesionComo('RESPONSABLE_COMUNIDAD')
      renderizarEn('/catalogo-plantas/17')

      expect(await screen.findByRole('heading', { level: 1, name: 'Cola de caballo' })).toBeInTheDocument()
      expect(screen.queryByRole('link', { name: 'Editar ficha' })).not.toBeInTheDocument()
    })
  })

  describe('baja de la planta', () => {
    afterEach(() => localStorage.clear())

    it('muestra Dar de baja al técnico MSP junto a Editar ficha', async () => {
      iniciarSesionComo('TECNICO_MSP')
      renderizarEn('/catalogo-plantas/17')

      const darDeBaja = await screen.findByRole('button', { name: 'Dar de baja' })
      expect(darDeBaja).toHaveClass('boton', 'boton-peligro')
      expect(darDeBaja.parentElement).toHaveClass('detalle-planta-acciones')
      expect(darDeBaja.parentElement).toContainElement(screen.getByRole('link', { name: 'Editar ficha' }))
    })

    it('no muestra Dar de baja sin sesión', async () => {
      renderizarEn('/catalogo-plantas/17')

      expect(await screen.findByRole('heading', { level: 1, name: 'Cola de caballo' })).toBeInTheDocument()
      expect(screen.queryByRole('button', { name: 'Dar de baja' })).not.toBeInTheDocument()
    })

    it('no muestra Dar de baja al responsable de comunidad', async () => {
      iniciarSesionComo('RESPONSABLE_COMUNIDAD')
      renderizarEn('/catalogo-plantas/17')

      expect(await screen.findByRole('heading', { level: 1, name: 'Cola de caballo' })).toBeInTheDocument()
      expect(screen.queryByRole('button', { name: 'Dar de baja' })).not.toBeInTheDocument()
    })

    it('abre la confirmación con el nombre de la planta', async () => {
      iniciarSesionComo('TECNICO_MSP')
      renderizarEn('/catalogo-plantas/17')

      await userEvent.click(await screen.findByRole('button', { name: 'Dar de baja' }))

      const confirmacion = screen.getByRole('dialog', { name: 'Confirmar baja de planta medicinal' })
      expect(within(confirmacion).getByText('Cola de caballo')).toBeInTheDocument()
    })

    it('tras confirmar la baja vuelve al catálogo y avisa que la planta fue retirada', async () => {
      const usuario = userEvent.setup()
      iniciarSesionComo('TECNICO_MSP')
      renderizarConCatalogo('/catalogo-plantas/17')

      await usuario.click(await screen.findByRole('button', { name: 'Dar de baja' }))
      await usuario.type(screen.getByRole('textbox', { name: 'Motivo de la baja' }), 'Registro duplicado')
      await usuario.click(screen.getByRole('button', { name: 'Sí, dar de baja' }))

      expect(await screen.findByRole('heading', { level: 1, name: 'Catálogo de Plantas' })).toBeInTheDocument()
      expect(screen.getByRole('status')).toHaveTextContent('Planta retirada del catálogo')
      expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
    })
  })

  describe('mensaje de ficha actualizada', () => {
    afterEach(() => vi.useRealTimers())

    it('muestra el mensaje recibido al volver de la edición y se cierra con el botón', async () => {
      renderizarConEstado({ mensajeExito: 'Ficha técnica actualizada exitosamente' })

      expect(screen.getByRole('status')).toHaveTextContent('Ficha técnica actualizada exitosamente')
      expect(await screen.findByRole('heading', { level: 1, name: 'Cola de caballo' })).toBeInTheDocument()

      await userEvent.click(screen.getByRole('button', { name: 'Cerrar notificación' }))

      expect(screen.queryByText('Ficha técnica actualizada exitosamente')).not.toBeInTheDocument()
    })

    it('oculta el mensaje solo después de unos segundos', () => {
      vi.useFakeTimers()
      renderizarConEstado({ mensajeExito: 'Ficha técnica actualizada exitosamente' })
      expect(screen.getByText('Ficha técnica actualizada exitosamente')).toBeInTheDocument()

      act(() => vi.advanceTimersByTime(5000))

      expect(screen.queryByText('Ficha técnica actualizada exitosamente')).not.toBeInTheDocument()
    })

    it('sin mensaje en la navegación no muestra la notificación', async () => {
      renderizarEn('/catalogo-plantas/17')

      expect(await screen.findByRole('heading', { level: 1, name: 'Cola de caballo' })).toBeInTheDocument()
      expect(screen.queryByRole('status')).not.toBeInTheDocument()
    })
  })
})
