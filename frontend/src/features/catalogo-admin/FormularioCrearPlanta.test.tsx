import { render, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { MemoryRouter, Route, Routes, useLocation } from 'react-router'
import { server } from '../../test/mocks/server'
import { fotografiaSubidaMock } from './catalogoAdminMockHandlers'
import { FormularioCrearPlanta } from './FormularioCrearPlanta'

type Usuario = ReturnType<typeof userEvent.setup>

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

function DestinoCatalogo() {
  const { state } = useLocation()
  const mensaje = (state as { mensajeExito?: string } | null)?.mensajeExito
  return <p>Catálogo: {mensaje ?? 'sin mensaje'}</p>
}

function renderizar() {
  render(
    <MemoryRouter initialEntries={['/admin/plantas/nueva']}>
      <Routes>
        <Route path="/admin/plantas/nueva" element={<FormularioCrearPlanta />} />
        <Route path="/catalogo-plantas" element={<DestinoCatalogo />} />
      </Routes>
    </MemoryRouter>,
  )
}

function registrarPeticiones() {
  const peticiones = { orden: [] as string[], cuerpo: null as unknown }
  server.use(
    http.post('*/api/msp/plantas/fotografia', () => {
      peticiones.orden.push('fotografia')
      return undefined
    }),
    http.post('*/api/msp/plantas', async ({ request }) => {
      peticiones.orden.push('registro')
      peticiones.cuerpo = await request.clone().json()
      return undefined
    }),
  )
  return peticiones
}

function responderRegistro(estado: number, cuerpo: object) {
  server.use(http.post('*/api/msp/plantas', () => HttpResponse.json(cuerpo, { status: estado })))
}

function opcion(grupo: string, nombre: string) {
  return within(screen.getByRole('group', { name: grupo })).getByRole('button', { name: nombre })
}

function entradaFoto() {
  return screen.getByLabelText('Fotografía de la planta', { selector: 'input' })
}

function foto() {
  return new File([new Uint8Array([0xff, 0xd8, 0xff])], 'hierba.jpg', { type: 'image/jpeg' })
}

async function llenarIdentificacion(usuario: Usuario) {
  await usuario.type(screen.getByLabelText('Nombre común'), 'Hierba de prueba')
  await usuario.type(screen.getByLabelText('Nombre científico'), 'Herba ficticia')
  await usuario.type(screen.getByLabelText('Familia botánica'), 'Ficticiaceae')
}

async function llenarParametros(usuario: Usuario) {
  await usuario.click(opcion('Cualidad térmica', 'Fría'))
}

async function llenarUsos(usuario: Usuario) {
  await usuario.click(opcion('Categorías de uso', 'Sedante'))
  await usuario.type(screen.getByLabelText('Usos medicinales'), 'Alivia el dolor de estómago')
}

async function llenarObligatorios(usuario: Usuario) {
  await llenarIdentificacion(usuario)
  await llenarParametros(usuario)
  await llenarUsos(usuario)
}

const BOTON_GUARDAR_ESCRITORIO = 'Guardar Planta en Catálogo'

describe('FormularioCrearPlanta', () => {
  describe('en móvil y tablet', () => {
    beforeEach(() => simularPantalla(false))

    it('muestra el asistente en el paso 1 con la barra de progreso', () => {
      renderizar()

      expect(screen.getByRole('progressbar', { name: 'Paso 1 de 3' })).toBeInTheDocument()
      expect(screen.getByRole('heading', { name: 'Identificación y fotografía' })).toBeInTheDocument()
      expect(screen.getByLabelText('Nombre común')).toBeInTheDocument()
      expect(entradaFoto()).toBeInTheDocument()
      expect(screen.queryByRole('group', { name: 'Cualidad térmica' })).not.toBeInTheDocument()
      expect(screen.queryByLabelText('Usos medicinales')).not.toBeInTheDocument()
      expect(screen.getByRole('button', { name: 'Siguiente' })).toBeInTheDocument()
      expect(screen.queryByRole('button', { name: 'Guardar Planta' })).not.toBeInTheDocument()
    })

    it('no avanza con el paso inválido y marca solo los campos de ese paso', async () => {
      const usuario = userEvent.setup()
      renderizar()

      await usuario.click(screen.getByRole('button', { name: 'Siguiente' }))

      expect(screen.getByRole('progressbar', { name: 'Paso 1 de 3' })).toBeInTheDocument()
      expect(screen.getByLabelText('Nombre común')).toHaveAttribute('aria-invalid', 'true')
      expect(screen.getByText('El nombre común es obligatorio')).toBeInTheDocument()
      expect(screen.getByText('El nombre científico es obligatorio')).toBeInTheDocument()
      expect(screen.getByText('La familia botánica es obligatoria')).toBeInTheDocument()
      expect(screen.queryByText('La cualidad térmica es obligatoria')).not.toBeInTheDocument()
    })

    it('avanza y retrocede entre pasos conservando los datos', async () => {
      const usuario = userEvent.setup()
      renderizar()
      await llenarIdentificacion(usuario)

      await usuario.click(screen.getByRole('button', { name: 'Siguiente' }))

      expect(screen.getByRole('progressbar', { name: 'Paso 2 de 3' })).toBeInTheDocument()
      expect(screen.getByRole('heading', { name: 'Parámetros agronómicos' })).toBeInTheDocument()
      expect(screen.getByRole('group', { name: 'Cualidad térmica' })).toBeInTheDocument()
      expect(screen.getByLabelText('Altitud mínima (msnm)')).toBeInTheDocument()

      await usuario.click(screen.getByRole('button', { name: 'Atrás' }))

      expect(screen.getByRole('progressbar', { name: 'Paso 1 de 3' })).toBeInTheDocument()
      expect(screen.getByLabelText('Nombre común')).toHaveValue('Hierba de prueba')
    })

    it('el paso 2 exige la cualidad térmica antes de avanzar', async () => {
      const usuario = userEvent.setup()
      renderizar()
      await llenarIdentificacion(usuario)
      await usuario.click(screen.getByRole('button', { name: 'Siguiente' }))

      await usuario.click(screen.getByRole('button', { name: 'Siguiente' }))

      expect(screen.getByRole('progressbar', { name: 'Paso 2 de 3' })).toBeInTheDocument()
      expect(screen.getByText('La cualidad térmica es obligatoria')).toBeInTheDocument()
    })

    it('en el paso 3 guarda la planta y vuelve al catálogo con el mensaje de éxito', async () => {
      const usuario = userEvent.setup()
      const peticiones = registrarPeticiones()
      renderizar()
      await llenarIdentificacion(usuario)
      await usuario.click(screen.getByRole('button', { name: 'Siguiente' }))
      await llenarParametros(usuario)
      await usuario.click(screen.getByRole('button', { name: 'Siguiente' }))

      expect(screen.getByRole('progressbar', { name: 'Paso 3 de 3' })).toBeInTheDocument()
      expect(screen.getByRole('heading', { name: 'Usos y dosificación' })).toBeInTheDocument()
      expect(screen.queryByRole('button', { name: 'Siguiente' })).not.toBeInTheDocument()
      await llenarUsos(usuario)
      await usuario.click(screen.getByRole('button', { name: 'Guardar Planta' }))

      expect(await screen.findByText('Catálogo: Planta registrada exitosamente')).toBeInTheDocument()
      expect(peticiones.orden).toEqual(['registro'])
    })

    it('si el nombre científico ya existe vuelve al paso 1 y marca el campo', async () => {
      const usuario = userEvent.setup()
      responderRegistro(409, { status: 409, detail: 'El nombre científico ya existe en el catálogo' })
      renderizar()
      await llenarIdentificacion(usuario)
      await usuario.click(screen.getByRole('button', { name: 'Siguiente' }))
      await llenarParametros(usuario)
      await usuario.click(screen.getByRole('button', { name: 'Siguiente' }))
      await llenarUsos(usuario)

      await usuario.click(screen.getByRole('button', { name: 'Guardar Planta' }))

      expect(await screen.findByRole('progressbar', { name: 'Paso 1 de 3' })).toBeInTheDocument()
      expect(screen.getByLabelText('Nombre científico')).toHaveAttribute('aria-invalid', 'true')
      expect(screen.getByText('El nombre científico ya existe en el catálogo')).toBeInTheDocument()
    })
  })

  describe('en escritorio', () => {
    beforeEach(() => simularPantalla(true))

    it('muestra el formulario completo en dos columnas con las seis tarjetas', () => {
      renderizar()

      for (const titulo of [
        'Taxonomía y nombres',
        'Fotografía',
        'Condiciones de cultivo',
        'Usos terapéuticos MSP',
        'Modos de preparación y dosificación',
        'Contraindicaciones',
      ]) {
        expect(screen.getByRole('heading', { name: titulo })).toBeInTheDocument()
      }
      expect(screen.getByLabelText('Nombre común')).toBeInTheDocument()
      expect(screen.getByRole('group', { name: 'Cualidad térmica' })).toBeInTheDocument()
      expect(screen.getByLabelText('Usos medicinales')).toBeInTheDocument()
      expect(screen.queryByRole('progressbar')).not.toBeInTheDocument()
      expect(screen.queryByRole('button', { name: 'Siguiente' })).not.toBeInTheDocument()
      expect(screen.getByRole('button', { name: 'Cancelar' })).toBeInTheDocument()
      expect(screen.getByRole('button', { name: BOTON_GUARDAR_ESCRITORIO })).toBeInTheDocument()
    })

    it('valida todos los campos obligatorios sin enviar nada', async () => {
      const usuario = userEvent.setup()
      const peticiones = registrarPeticiones()
      renderizar()

      await usuario.click(screen.getByRole('button', { name: BOTON_GUARDAR_ESCRITORIO }))

      expect(screen.getByText('El nombre común es obligatorio')).toBeInTheDocument()
      expect(screen.getByText('El nombre científico es obligatorio')).toBeInTheDocument()
      expect(screen.getByText('La familia botánica es obligatoria')).toBeInTheDocument()
      expect(screen.getByText('La cualidad térmica es obligatoria')).toBeInTheDocument()
      expect(screen.getByText('Seleccione al menos una categoría de uso')).toBeInTheDocument()
      expect(screen.getByText('Los usos medicinales son obligatorios')).toBeInTheDocument()
      expect(screen.getByLabelText('Usos medicinales')).toHaveAttribute('aria-invalid', 'true')
      expect(peticiones.orden).toEqual([])
    })

    it('rechaza una altitud mínima mayor que la máxima sin enviar', async () => {
      const usuario = userEvent.setup()
      const peticiones = registrarPeticiones()
      renderizar()
      await llenarObligatorios(usuario)
      await usuario.type(screen.getByLabelText('Altitud mínima (msnm)'), '3500')
      await usuario.type(screen.getByLabelText('Altitud máxima (msnm)'), '2000')

      await usuario.click(screen.getByRole('button', { name: BOTON_GUARDAR_ESCRITORIO }))

      expect(screen.getByText('La altitud mínima no puede ser mayor que la altitud máxima')).toBeInTheDocument()
      expect(screen.getByLabelText('Altitud mínima (msnm)')).toHaveAttribute('aria-invalid', 'true')
      expect(peticiones.orden).toEqual([])
    })

    it('las categorías y las regiones son chips de selección múltiple', async () => {
      const usuario = userEvent.setup()
      renderizar()

      await usuario.click(opcion('Categorías de uso', 'Sedante'))
      await usuario.click(opcion('Categorías de uso', 'Diurética'))
      await usuario.click(opcion('Regiones de distribución', 'Sierra'))
      await usuario.click(opcion('Regiones de distribución', 'Galápagos'))
      await usuario.click(opcion('Regiones de distribución', 'Galápagos'))

      expect(opcion('Categorías de uso', 'Sedante')).toHaveAttribute('aria-pressed', 'true')
      expect(opcion('Categorías de uso', 'Diurética')).toHaveAttribute('aria-pressed', 'true')
      expect(opcion('Categorías de uso', 'Expectorante')).toHaveAttribute('aria-pressed', 'false')
      expect(opcion('Regiones de distribución', 'Sierra')).toHaveAttribute('aria-pressed', 'true')
      expect(opcion('Regiones de distribución', 'Galápagos')).toHaveAttribute('aria-pressed', 'false')
      expect(opcion('Regiones de distribución', 'Costa')).toHaveAttribute('aria-pressed', 'false')
      expect(opcion('Regiones de distribución', 'Amazonía')).toBeInTheDocument()
      expect(screen.queryByRole('combobox', { name: /categor/i })).not.toBeInTheDocument()
    })

    it('envía regiones y categorías en el orden fijo de las opciones sin importar el orden de los clics', async () => {
      const usuario = userEvent.setup()
      const peticiones = registrarPeticiones()
      renderizar()
      await llenarIdentificacion(usuario)
      await llenarParametros(usuario)
      await usuario.click(opcion('Regiones de distribución', 'Galápagos'))
      await usuario.click(opcion('Regiones de distribución', 'Sierra'))
      await usuario.click(opcion('Regiones de distribución', 'Costa'))
      await usuario.click(opcion('Regiones de distribución', 'Galápagos'))
      await usuario.click(opcion('Categorías de uso', 'Sedante'))
      await usuario.click(opcion('Categorías de uso', 'Expectorante'))
      await usuario.click(opcion('Categorías de uso', 'Antiespasmódica'))
      await usuario.type(screen.getByLabelText('Usos medicinales'), 'Alivia el dolor de estómago')

      await usuario.click(screen.getByRole('button', { name: BOTON_GUARDAR_ESCRITORIO }))

      await waitFor(() => expect(peticiones.orden).toEqual(['registro']))
      expect(peticiones.cuerpo).toMatchObject({
        regionesDistribucion: 'Costa, Sierra',
        categorias: ['ANTIESPASMODICA', 'EXPECTORANTE', 'SEDANTE'],
      })
    })

    it('sube primero la foto y después registra la planta con su ruta', async () => {
      const usuario = userEvent.setup()
      const peticiones = registrarPeticiones()
      renderizar()
      await usuario.upload(entradaFoto(), foto())
      await usuario.type(screen.getByLabelText('Nombre común'), '  Hierba de prueba ')
      await usuario.type(screen.getByLabelText('Nombre kichwa'), 'Yura kiwa')
      await usuario.type(screen.getByLabelText('Nombre científico'), 'Herba ficticia')
      await usuario.type(screen.getByLabelText('Familia botánica'), 'Ficticiaceae')
      await usuario.click(opcion('Cualidad térmica', 'Caliente'))
      await usuario.click(opcion('Ciclo de cultivo', 'Corto'))
      await usuario.selectOptions(screen.getByLabelText('Origen'), 'NATIVA')
      await usuario.type(screen.getByLabelText('Altitud mínima (msnm)'), '2000')
      await usuario.type(screen.getByLabelText('Altitud máxima (msnm)'), '3200')
      await usuario.click(opcion('Regiones de distribución', 'Sierra'))
      await usuario.click(opcion('Regiones de distribución', 'Costa'))
      await usuario.click(opcion('Categorías de uso', 'Sedante'))
      await usuario.click(opcion('Categorías de uso', 'Diurética'))
      await usuario.type(screen.getByLabelText('Usos medicinales'), 'Alivia el dolor de estómago')
      await usuario.type(screen.getByLabelText('Preparación y dosificación'), 'Infusión de 5 g en una taza')
      await usuario.type(screen.getByLabelText('Contraindicaciones'), 'No usar en embarazo')

      await usuario.click(screen.getByRole('button', { name: BOTON_GUARDAR_ESCRITORIO }))

      expect(await screen.findByText('Catálogo: Planta registrada exitosamente')).toBeInTheDocument()
      expect(peticiones.orden).toEqual(['fotografia', 'registro'])
      expect(peticiones.cuerpo).toEqual({
        nombreComun: 'Hierba de prueba',
        nombreKichwa: 'Yura kiwa',
        nombreCientifico: 'Herba ficticia',
        familiaBotanica: 'Ficticiaceae',
        cualidadTermica: 'CALIENTE',
        cicloCultivo: 'CORTO',
        origen: 'NATIVA',
        altitudMinMsnm: 2000,
        altitudMaxMsnm: 3200,
        regionesDistribucion: 'Costa, Sierra',
        usosMedicinales: 'Alivia el dolor de estómago',
        preparacionDosificacion: 'Infusión de 5 g en una taza',
        contraindicaciones: 'No usar en embarazo',
        categorias: ['DIURETICA', 'SEDANTE'],
        fotografiaUrl: fotografiaSubidaMock,
      })
    })

    it('sin foto registra solo con los campos obligatorios y los opcionales nulos', async () => {
      const usuario = userEvent.setup()
      const peticiones = registrarPeticiones()
      renderizar()
      await llenarObligatorios(usuario)

      await usuario.click(screen.getByRole('button', { name: BOTON_GUARDAR_ESCRITORIO }))

      expect(await screen.findByText('Catálogo: Planta registrada exitosamente')).toBeInTheDocument()
      expect(peticiones.orden).toEqual(['registro'])
      expect(peticiones.cuerpo).toEqual({
        nombreComun: 'Hierba de prueba',
        nombreKichwa: null,
        nombreCientifico: 'Herba ficticia',
        familiaBotanica: 'Ficticiaceae',
        cualidadTermica: 'FRIA',
        cicloCultivo: null,
        origen: null,
        altitudMinMsnm: null,
        altitudMaxMsnm: null,
        regionesDistribucion: null,
        usosMedicinales: 'Alivia el dolor de estómago',
        preparacionDosificacion: null,
        contraindicaciones: null,
        categorias: ['SEDANTE'],
        fotografiaUrl: null,
      })
    })

    it('si falla la subida de la foto muestra el error y no registra la planta', async () => {
      const usuario = userEvent.setup()
      const peticiones = registrarPeticiones()
      server.use(
        http.post('*/api/msp/plantas/fotografia', () =>
          HttpResponse.json({ status: 415, detail: 'Formato no admitido' }, { status: 415 }),
        ),
      )
      renderizar()
      await usuario.upload(entradaFoto(), foto())
      await llenarObligatorios(usuario)

      await usuario.click(screen.getByRole('button', { name: BOTON_GUARDAR_ESCRITORIO }))

      expect(await screen.findByText('Formato no admitido. Seleccione una imagen JPG, PNG o WEBP')).toBeInTheDocument()
      expect(peticiones.orden).not.toContain('registro')
      expect(screen.queryByText(/^Catálogo:/)).not.toBeInTheDocument()
    })

    it('deshabilita el botón de guardar mientras envía', async () => {
      const usuario = userEvent.setup()
      let liberar = () => {}
      const espera = new Promise<void>((resolver) => {
        liberar = resolver
      })
      server.use(
        http.post('*/api/msp/plantas', async () => {
          await espera
          return undefined
        }),
      )
      renderizar()
      await llenarObligatorios(usuario)

      await usuario.click(screen.getByRole('button', { name: BOTON_GUARDAR_ESCRITORIO }))

      await waitFor(() => expect(screen.getByRole('button', { name: BOTON_GUARDAR_ESCRITORIO })).toBeDisabled())
      liberar()
      expect(await screen.findByText('Catálogo: Planta registrada exitosamente')).toBeInTheDocument()
    })

    it('marca el nombre científico cuando ya existe en el catálogo', async () => {
      const usuario = userEvent.setup()
      responderRegistro(409, { status: 409, detail: 'El nombre científico ya existe en el catálogo' })
      renderizar()
      await llenarObligatorios(usuario)

      await usuario.click(screen.getByRole('button', { name: BOTON_GUARDAR_ESCRITORIO }))

      expect(await screen.findByText('El nombre científico ya existe en el catálogo')).toBeInTheDocument()
      expect(screen.getByLabelText('Nombre científico')).toHaveAttribute('aria-invalid', 'true')
    })

    it('marca los campos con los errores por campo del back', async () => {
      const usuario = userEvent.setup()
      responderRegistro(400, {
        status: 400,
        detail: 'Datos no válidos',
        errores: {
          familiaBotanica: 'La familia botánica no puede superar los 100 caracteres',
          usosMedicinales: 'Los usos medicinales son obligatorios',
        },
      })
      renderizar()
      await llenarObligatorios(usuario)

      await usuario.click(screen.getByRole('button', { name: BOTON_GUARDAR_ESCRITORIO }))

      expect(await screen.findByText('La familia botánica no puede superar los 100 caracteres')).toBeInTheDocument()
      expect(screen.getByLabelText('Familia botánica')).toHaveAttribute('aria-invalid', 'true')
      expect(screen.getByText('Los usos medicinales son obligatorios')).toBeInTheDocument()
      expect(screen.getByLabelText('Usos medicinales')).toHaveAttribute('aria-invalid', 'true')
    })

    it('muestra en los campos de altitud el 400 de altitud del back', async () => {
      const usuario = userEvent.setup()
      responderRegistro(400, {
        status: 400,
        detail: 'La altitud mínima no puede ser mayor que la altitud máxima',
      })
      renderizar()
      await llenarObligatorios(usuario)
      await usuario.type(screen.getByLabelText('Altitud mínima (msnm)'), '2000')
      await usuario.type(screen.getByLabelText('Altitud máxima (msnm)'), '3000')

      await usuario.click(screen.getByRole('button', { name: BOTON_GUARDAR_ESCRITORIO }))

      expect(
        await screen.findByText('La altitud mínima no puede ser mayor que la altitud máxima'),
      ).toBeInTheDocument()
      expect(screen.getByLabelText('Altitud mínima (msnm)')).toHaveAttribute('aria-invalid', 'true')
      expect(screen.getByLabelText('Altitud máxima (msnm)')).toHaveAttribute('aria-invalid', 'true')
    })

    it('con 403 pide iniciar sesión como Técnico MSP con un enlace al login', async () => {
      const usuario = userEvent.setup()
      responderRegistro(403, { status: 403, detail: 'Access Denied' })
      renderizar()
      await llenarObligatorios(usuario)

      await usuario.click(screen.getByRole('button', { name: BOTON_GUARDAR_ESCRITORIO }))

      expect(await screen.findByText(/Necesitas iniciar sesión como Técnico MSP/)).toBeInTheDocument()
      expect(screen.getByRole('link', { name: 'Iniciar sesión' })).toHaveAttribute('href', '/login')
    })

    it('Cancelar vuelve al catálogo sin guardar', async () => {
      const usuario = userEvent.setup()
      const peticiones = registrarPeticiones()
      renderizar()
      await llenarIdentificacion(usuario)

      await usuario.click(screen.getByRole('button', { name: 'Cancelar' }))

      expect(await screen.findByText('Catálogo: sin mensaje')).toBeInTheDocument()
      expect(peticiones.orden).toEqual([])
    })
  })
})
