import { render, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { MemoryRouter, Route, Routes, useLocation, useParams } from 'react-router'
import { server } from '../../test/mocks/server'
import { fotografiaSubidaMock, plantaEditableMock } from './catalogoAdminMockHandlers'
import { FormularioEditarPlanta } from './FormularioEditarPlanta'

const MENSAJE_EXITO = 'Ficha técnica actualizada exitosamente'
const SOLICITUD_SIN_CAMBIOS = {
  nombreComun: 'Hierba editable',
  nombreKichwa: 'Yura kiwa',
  nombreCientifico: 'Herba editabilis',
  familiaBotanica: 'Ficticiaceae',
  cualidadTermica: 'FRIA',
  cicloCultivo: 'CORTO',
  origen: 'NATIVA',
  altitudMinMsnm: 2000,
  altitudMaxMsnm: 3200,
  regionesDistribucion: 'Costa, Sierra',
  usosMedicinales: 'Alivia el dolor de estómago',
  preparacionDosificacion: 'Infusión de 5 g en una taza',
  contraindicaciones: 'No usar en embarazo',
  categorias: ['DIURETICA', 'SEDANTE'],
  fotografiaUrl: 'plantas/hierba-editable.jpg',
}

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

function DestinoFicha() {
  const { id } = useParams()
  const { state } = useLocation()
  const mensaje = (state as { mensajeExito?: string } | null)?.mensajeExito
  return (
    <p>
      Ficha {id}: {mensaje ?? 'sin mensaje'}
    </p>
  )
}

function renderizar() {
  render(
    <MemoryRouter initialEntries={['/admin/plantas/7/editar']}>
      <Routes>
        <Route path="/admin/plantas/:id/editar" element={<FormularioEditarPlanta planta={plantaEditableMock} />} />
        <Route path="/catalogo-plantas/:id" element={<DestinoFicha />} />
      </Routes>
    </MemoryRouter>,
  )
}

function registrarPeticiones() {
  const peticiones = { orden: [] as string[], cuerpo: null as unknown, ruta: '' }
  server.use(
    http.post('*/api/msp/plantas/fotografia', () => {
      peticiones.orden.push('fotografia')
      return undefined
    }),
    http.put('*/api/msp/plantas/:id', async ({ request }) => {
      peticiones.orden.push('actualizacion')
      peticiones.ruta = new URL(request.url).pathname
      peticiones.cuerpo = await request.clone().json()
      return undefined
    }),
  )
  return peticiones
}

function responderActualizacion(estado: number, cuerpo: object) {
  server.use(http.put('*/api/msp/plantas/:id', () => HttpResponse.json(cuerpo, { status: estado })))
}

function opcion(grupo: string, nombre: string) {
  return within(screen.getByRole('group', { name: grupo })).getByRole('button', { name: nombre })
}

function acordeon(nombre: string) {
  return screen.getByRole('button', { name: nombre })
}

function entradaFoto() {
  return screen.getByLabelText('Fotografía de la planta', { selector: 'input' })
}

function foto() {
  return new File([new Uint8Array([0xff, 0xd8, 0xff])], 'hierba-nueva.jpg', { type: 'image/jpeg' })
}

describe('FormularioEditarPlanta', () => {
  describe('en móvil y tablet', () => {
    beforeEach(() => simularPantalla(false))

    it('muestra tres acordeones con el de botánica abierto', () => {
      renderizar()

      expect(acordeon('Botánica')).toHaveAttribute('aria-expanded', 'true')
      expect(acordeon('Agronomía')).toHaveAttribute('aria-expanded', 'false')
      expect(acordeon('Medicina/Dosificación')).toHaveAttribute('aria-expanded', 'false')
      expect(screen.getByLabelText('Nombre común')).toHaveValue('Hierba editable')
      expect(screen.getByRole('img', { name: 'Fotografía de la planta' })).toBeInTheDocument()
      expect(screen.queryByRole('group', { name: 'Cualidad térmica' })).not.toBeInTheDocument()
      expect(screen.queryByRole('textbox', { name: 'Usos medicinales' })).not.toBeInTheDocument()
    })

    it('abre y cierra los acordeones al tocarlos', async () => {
      const usuario = userEvent.setup()
      renderizar()

      await usuario.click(acordeon('Agronomía'))

      expect(acordeon('Agronomía')).toHaveAttribute('aria-expanded', 'true')
      expect(opcion('Cualidad térmica', 'Fría')).toHaveAttribute('aria-pressed', 'true')
      expect(screen.getByLabelText('Altitud mínima (msnm)')).toHaveValue(2000)

      await usuario.click(acordeon('Agronomía'))
      await usuario.click(acordeon('Botánica'))

      expect(acordeon('Agronomía')).toHaveAttribute('aria-expanded', 'false')
      expect(acordeon('Botánica')).toHaveAttribute('aria-expanded', 'false')
      expect(screen.queryByRole('group', { name: 'Cualidad térmica' })).not.toBeInTheDocument()
      expect(screen.queryByLabelText('Nombre común')).not.toBeInTheDocument()
    })

    it('conserva lo editado al cerrar y volver a abrir un acordeón', async () => {
      const usuario = userEvent.setup()
      renderizar()
      await usuario.clear(screen.getByLabelText('Nombre común'))
      await usuario.type(screen.getByLabelText('Nombre común'), 'Hierba editada')

      await usuario.click(acordeon('Botánica'))
      await usuario.click(acordeon('Botánica'))

      expect(screen.getByLabelText('Nombre común')).toHaveValue('Hierba editada')
    })

    it('tiene la barra inferior con Descartar y Guardar Cambios', () => {
      renderizar()

      expect(screen.getByRole('button', { name: 'Descartar' })).toBeInTheDocument()
      expect(screen.getByRole('button', { name: 'Guardar Cambios' })).toBeInTheDocument()
    })

    it('abre el acordeón que contiene el primer campo con error', async () => {
      const usuario = userEvent.setup()
      const peticiones = registrarPeticiones()
      renderizar()
      await usuario.click(acordeon('Medicina/Dosificación'))
      await usuario.click(opcion('Categorías de uso', 'Sedante'))
      await usuario.click(opcion('Categorías de uso', 'Diurética'))
      await usuario.click(acordeon('Medicina/Dosificación'))

      await usuario.click(screen.getByRole('button', { name: 'Guardar Cambios' }))

      expect(acordeon('Medicina/Dosificación')).toHaveAttribute('aria-expanded', 'true')
      expect(screen.getByText('Seleccione al menos una categoría de uso')).toBeInTheDocument()
      expect(peticiones.orden).toEqual([])
    })

    it('abre el acordeón del campo marcado por el 400 del back', async () => {
      const usuario = userEvent.setup()
      responderActualizacion(400, {
        status: 400,
        detail: 'Datos no válidos',
        errores: { usosMedicinales: 'Los usos medicinales son obligatorios' },
      })
      renderizar()

      await usuario.click(screen.getByRole('button', { name: 'Guardar Cambios' }))

      expect(await screen.findByText('Los usos medicinales son obligatorios')).toBeInTheDocument()
      expect(acordeon('Medicina/Dosificación')).toHaveAttribute('aria-expanded', 'true')
      expect(screen.getByLabelText('Usos medicinales')).toHaveAttribute('aria-invalid', 'true')
    })

    it('guarda y vuelve a la ficha con el mensaje de éxito', async () => {
      const usuario = userEvent.setup()
      const peticiones = registrarPeticiones()
      renderizar()

      await usuario.click(screen.getByRole('button', { name: 'Guardar Cambios' }))

      expect(await screen.findByText(`Ficha 7: ${MENSAJE_EXITO}`)).toBeInTheDocument()
      expect(peticiones.orden).toEqual(['actualizacion'])
    })

    it('Descartar vuelve a la ficha sin guardar', async () => {
      const usuario = userEvent.setup()
      const peticiones = registrarPeticiones()
      renderizar()

      await usuario.click(screen.getByRole('button', { name: 'Descartar' }))

      expect(await screen.findByText('Ficha 7: sin mensaje')).toBeInTheDocument()
      expect(peticiones.orden).toEqual([])
    })
  })

  describe('en escritorio', () => {
    beforeEach(() => simularPantalla(true))

    it('muestra las tres secciones abiertas en dos columnas sin acordeones', () => {
      renderizar()

      const izquierda = screen.getByRole('heading', { name: 'Taxonomía y nombres' }).closest('.editar-planta-columna')
      const derecha = screen.getByRole('heading', { name: 'Usos terapéuticos MSP' }).closest('.editar-planta-columna')
      expect(izquierda).not.toBeNull()
      expect(derecha).not.toBeNull()
      expect(izquierda).not.toBe(derecha)
      for (const titulo of ['Fotografía', 'Condiciones de cultivo']) {
        expect(izquierda).toContainElement(screen.getByRole('heading', { name: titulo }))
      }
      for (const titulo of ['Modos de preparación y dosificación', 'Contraindicaciones']) {
        expect(derecha).toContainElement(screen.getByRole('heading', { name: titulo }))
      }
      expect(screen.queryByRole('button', { expanded: false })).not.toBeInTheDocument()
      expect(screen.queryByRole('button', { expanded: true })).not.toBeInTheDocument()
      expect(screen.getByRole('button', { name: 'Descartar Cambios' })).toHaveClass('boton-secundario')
      expect(screen.getByRole('button', { name: 'Guardar Cambios' })).toHaveClass('boton-primario')
    })

    it('precarga todos los datos de la planta', () => {
      renderizar()

      expect(screen.getByLabelText('Nombre común')).toHaveValue('Hierba editable')
      expect(screen.getByLabelText('Nombre kichwa')).toHaveValue('Yura kiwa')
      expect(screen.getByLabelText('Nombre científico')).toHaveValue('Herba editabilis')
      expect(screen.getByLabelText('Familia botánica')).toHaveValue('Ficticiaceae')
      expect(opcion('Cualidad térmica', 'Fría')).toHaveAttribute('aria-pressed', 'true')
      expect(opcion('Ciclo de cultivo', 'Corto')).toHaveAttribute('aria-pressed', 'true')
      expect(screen.getByLabelText('Origen')).toHaveValue('NATIVA')
      expect(screen.getByLabelText('Altitud mínima (msnm)')).toHaveValue(2000)
      expect(screen.getByLabelText('Altitud máxima (msnm)')).toHaveValue(3200)
      expect(opcion('Regiones de distribución', 'Costa')).toHaveAttribute('aria-pressed', 'true')
      expect(opcion('Regiones de distribución', 'Sierra')).toHaveAttribute('aria-pressed', 'true')
      expect(opcion('Regiones de distribución', 'Amazonía')).toHaveAttribute('aria-pressed', 'false')
      expect(opcion('Categorías de uso', 'Sedante')).toHaveAttribute('aria-pressed', 'true')
      expect(opcion('Categorías de uso', 'Diurética')).toHaveAttribute('aria-pressed', 'true')
      expect(opcion('Categorías de uso', 'Expectorante')).toHaveAttribute('aria-pressed', 'false')
      expect(screen.getByLabelText('Usos medicinales')).toHaveValue('Alivia el dolor de estómago')
      expect(screen.getByLabelText('Preparación y dosificación')).toHaveValue('Infusión de 5 g en una taza')
      expect(screen.getByLabelText('Contraindicaciones')).toHaveValue('No usar en embarazo')
      expect(screen.getByRole('img', { name: 'Fotografía de la planta' })).toHaveAttribute(
        'src',
        '/api/publico/fotos/plantas/hierba-editable.jpg',
      )
    })

    it('sin foto nueva envía la fotografía actual y vuelve a la ficha con el mensaje', async () => {
      const usuario = userEvent.setup()
      const peticiones = registrarPeticiones()
      renderizar()
      await usuario.clear(screen.getByLabelText('Nombre común'))
      await usuario.type(screen.getByLabelText('Nombre común'), '  Hierba editada ')
      await usuario.click(opcion('Categorías de uso', 'Expectorante'))

      await usuario.click(screen.getByRole('button', { name: 'Guardar Cambios' }))

      expect(await screen.findByText(`Ficha 7: ${MENSAJE_EXITO}`)).toBeInTheDocument()
      expect(peticiones.orden).toEqual(['actualizacion'])
      expect(peticiones.ruta).toBe('/api/msp/plantas/7')
      expect(peticiones.cuerpo).toEqual({
        ...SOLICITUD_SIN_CAMBIOS,
        nombreComun: 'Hierba editada',
        categorias: ['DIURETICA', 'EXPECTORANTE', 'SEDANTE'],
      })
    })

    it('con foto nueva la sube primero y actualiza con la nueva ruta', async () => {
      const usuario = userEvent.setup()
      const peticiones = registrarPeticiones()
      renderizar()
      await usuario.upload(entradaFoto(), foto())

      await usuario.click(screen.getByRole('button', { name: 'Guardar Cambios' }))

      expect(await screen.findByText(`Ficha 7: ${MENSAJE_EXITO}`)).toBeInTheDocument()
      expect(peticiones.orden).toEqual(['fotografia', 'actualizacion'])
      expect(peticiones.cuerpo).toEqual({ ...SOLICITUD_SIN_CAMBIOS, fotografiaUrl: fotografiaSubidaMock })
    })

    it('al quitar la foto actual envía la fotografía nula', async () => {
      const usuario = userEvent.setup()
      const peticiones = registrarPeticiones()
      renderizar()
      await usuario.click(screen.getByRole('button', { name: 'Quitar' }))

      await usuario.click(screen.getByRole('button', { name: 'Guardar Cambios' }))

      expect(await screen.findByText(`Ficha 7: ${MENSAJE_EXITO}`)).toBeInTheDocument()
      expect(peticiones.cuerpo).toEqual({ ...SOLICITUD_SIN_CAMBIOS, fotografiaUrl: null })
    })

    it('si falla la subida de la foto muestra el error y no actualiza la planta', async () => {
      const usuario = userEvent.setup()
      const peticiones = registrarPeticiones()
      server.use(
        http.post('*/api/msp/plantas/fotografia', () =>
          HttpResponse.json({ status: 413, detail: 'Imagen demasiado grande' }, { status: 413 }),
        ),
      )
      renderizar()
      await usuario.upload(entradaFoto(), foto())

      await usuario.click(screen.getByRole('button', { name: 'Guardar Cambios' }))

      expect(
        await screen.findByText('La imagen supera el límite de 5 MB. Intente reducir la resolución'),
      ).toBeInTheDocument()
      expect(peticiones.orden).not.toContain('actualizacion')
      expect(screen.queryByText(/^Ficha 7:/)).not.toBeInTheDocument()
    })

    it('valida los campos antes de enviar', async () => {
      const usuario = userEvent.setup()
      const peticiones = registrarPeticiones()
      renderizar()
      await usuario.clear(screen.getByLabelText('Nombre común'))
      await usuario.clear(screen.getByLabelText('Altitud mínima (msnm)'))
      await usuario.type(screen.getByLabelText('Altitud mínima (msnm)'), '3500')

      await usuario.click(screen.getByRole('button', { name: 'Guardar Cambios' }))

      expect(screen.getByText('El nombre común es obligatorio')).toBeInTheDocument()
      expect(screen.getByText('La altitud mínima no puede ser mayor que la altitud máxima')).toBeInTheDocument()
      expect(peticiones.orden).toEqual([])
    })

    it('deshabilita Guardar Cambios mientras envía', async () => {
      const usuario = userEvent.setup()
      let liberar = () => {}
      const espera = new Promise<void>((resolver) => {
        liberar = resolver
      })
      server.use(
        http.put('*/api/msp/plantas/:id', async () => {
          await espera
          return undefined
        }),
      )
      renderizar()

      await usuario.click(screen.getByRole('button', { name: 'Guardar Cambios' }))

      await waitFor(() => expect(screen.getByRole('button', { name: 'Guardar Cambios' })).toBeDisabled())
      liberar()
      expect(await screen.findByText(`Ficha 7: ${MENSAJE_EXITO}`)).toBeInTheDocument()
    })

    it('marca los campos con los errores por campo del back', async () => {
      const usuario = userEvent.setup()
      responderActualizacion(400, {
        status: 400,
        detail: 'Datos no válidos',
        errores: { familiaBotanica: 'La familia botánica no puede superar los 100 caracteres' },
      })
      renderizar()

      await usuario.click(screen.getByRole('button', { name: 'Guardar Cambios' }))

      expect(await screen.findByText('La familia botánica no puede superar los 100 caracteres')).toBeInTheDocument()
      expect(screen.getByLabelText('Familia botánica')).toHaveAttribute('aria-invalid', 'true')
    })

    it('muestra en los campos de altitud el 400 de altitud del back', async () => {
      const usuario = userEvent.setup()
      responderActualizacion(400, {
        status: 400,
        detail: 'La altitud mínima no puede ser mayor que la altitud máxima',
      })
      renderizar()

      await usuario.click(screen.getByRole('button', { name: 'Guardar Cambios' }))

      expect(
        await screen.findByText('La altitud mínima no puede ser mayor que la altitud máxima'),
      ).toBeInTheDocument()
      expect(screen.getByLabelText('Altitud mínima (msnm)')).toHaveAttribute('aria-invalid', 'true')
      expect(screen.getByLabelText('Altitud máxima (msnm)')).toHaveAttribute('aria-invalid', 'true')
    })

    it('con 409 marca el nombre científico y avisa que pertenece a otra planta', async () => {
      const usuario = userEvent.setup()
      responderActualizacion(409, { status: 409, detail: 'Nombre científico ya registrado en otra planta' })
      renderizar()

      await usuario.click(screen.getByRole('button', { name: 'Guardar Cambios' }))

      expect(await screen.findByRole('alert')).toHaveTextContent('Nombre científico ya registrado en otra planta')
      expect(screen.getByRole('alert')).toHaveClass('notificacion-error')
      expect(screen.getByLabelText('Nombre científico')).toHaveAttribute('aria-invalid', 'true')
      expect(screen.queryByText(/^Ficha 7:/)).not.toBeInTheDocument()
    })

    it('con 404 avisa que la planta no existe y ofrece volver al catálogo', async () => {
      const usuario = userEvent.setup()
      responderActualizacion(404, { status: 404, detail: 'La planta no existe o fue dada de baja' })
      renderizar()

      await usuario.click(screen.getByRole('button', { name: 'Guardar Cambios' }))

      expect(await screen.findByRole('alert')).toHaveTextContent('La planta no existe o fue dada de baja')
      expect(screen.getByRole('link', { name: 'Volver al catálogo' })).toHaveAttribute('href', '/catalogo-plantas')
    })

    it('con 403 pide iniciar sesión como Técnico MSP con un enlace al login', async () => {
      const usuario = userEvent.setup()
      responderActualizacion(403, { status: 403, detail: 'Access Denied' })
      renderizar()

      await usuario.click(screen.getByRole('button', { name: 'Guardar Cambios' }))

      expect(await screen.findByRole('alert')).toHaveTextContent('Necesitas iniciar sesión como Técnico MSP')
      expect(screen.getByRole('link', { name: 'Iniciar sesión' })).toHaveAttribute('href', '/login')
    })

    it('con 401 también pide iniciar sesión', async () => {
      const usuario = userEvent.setup()
      responderActualizacion(401, { status: 401, detail: 'Unauthorized' })
      renderizar()

      await usuario.click(screen.getByRole('button', { name: 'Guardar Cambios' }))

      expect(await screen.findByRole('alert')).toHaveTextContent('Necesitas iniciar sesión como Técnico MSP')
      expect(screen.getByRole('link', { name: 'Iniciar sesión' })).toHaveAttribute('href', '/login')
    })

    it('Descartar Cambios vuelve a la ficha sin guardar', async () => {
      const usuario = userEvent.setup()
      const peticiones = registrarPeticiones()
      renderizar()
      await usuario.clear(screen.getByLabelText('Nombre común'))

      await usuario.click(screen.getByRole('button', { name: 'Descartar Cambios' }))

      expect(await screen.findByText('Ficha 7: sin mensaje')).toBeInTheDocument()
      expect(peticiones.orden).toEqual([])
    })
  })
})
