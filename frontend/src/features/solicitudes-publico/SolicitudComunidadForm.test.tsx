import { fireEvent, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { MemoryRouter, Route, Routes } from 'react-router'
import { afterEach } from 'vitest'
import { server } from '../../test/mocks/server'
import { SolicitudComunidadForm } from './SolicitudComunidadForm'

function renderizar() {
  render(
    <MemoryRouter initialEntries={['/solicitar-comunidad']}>
      <Routes>
        <Route path="/solicitar-comunidad" element={<SolicitudComunidadForm />} />
        <Route path="/" element={<p>Página de inicio</p>} />
      </Routes>
    </MemoryRouter>,
  )
}

async function escribir(usuario: ReturnType<typeof userEvent.setup>, campo: HTMLElement, texto: string) {
  await usuario.click(campo)
  await usuario.paste(texto)
}

async function llenarFormulario({ cedula = '1710034065' } = {}) {
  const usuario = userEvent.setup()
  await escribir(usuario, screen.getByLabelText('Nombre de la comunidad'), 'Huerto Comunitario Allpa Viva')
  await usuario.selectOptions(screen.getByLabelText('Tipo de organización'), 'BARRIO')
  await usuario.selectOptions(screen.getByLabelText('Provincia'), 'Pichincha')
  await escribir(usuario, screen.getByLabelText('Parroquia'), 'Calderon')
  await escribir(usuario, screen.getByLabelText('Dirección exacta'), 'Calle de las Rosas y Pasaje Los Pinos')
  await escribir(usuario, screen.getByLabelText('Latitud'), '-0.0987123')
  await escribir(usuario, screen.getByLabelText('Longitud'), '-78.4321456')
  await usuario.selectOptions(screen.getByLabelText('Diseño principal del huerto'), 'EN_TIERRA')
  await usuario.click(screen.getByLabelText('Surcos (wachu)'))
  await escribir(usuario, screen.getByLabelText('Cédula de identidad'), cedula)
  await escribir(usuario, screen.getByLabelText('Nombre y apellido'), 'Segundo Tránsito Amaguaña')
  await usuario.type(screen.getByLabelText('Fecha de nacimiento'), '1990-05-14')
  await usuario.selectOptions(screen.getByLabelText('Género'), 'MASCULINO')
  await escribir(usuario, screen.getByLabelText('Correo electrónico'), 'comunidad.allpa@huertos.gob.ec')
  await escribir(usuario, screen.getByLabelText('Teléfono celular'), '0991234567')
  return usuario
}

const URL_GEOCODIFICACION_INVERSA = 'https://nominatim.openstreetmap.org/reverse'

function pulsarCentroDelMapa() {
  fireEvent.click(screen.getByRole('application', { name: 'Mapa para elegir la ubicación' }), {
    clientX: 0,
    clientY: 0,
  })
}

function simularGeolocalizacion(getCurrentPosition: Geolocation['getCurrentPosition']) {
  Object.defineProperty(navigator, 'geolocation', { value: { getCurrentPosition }, configurable: true })
}

describe('SolicitudComunidadForm', () => {
  afterEach(() => {
    Reflect.deleteProperty(navigator, 'geolocation')
  })

  it('agrupa los campos en las tarjetas de comunidad y de solicitante, con Kinti', () => {
    renderizar()

    expect(screen.getByAltText('Kinti Guía de sabiduría ancestral')).toBeInTheDocument()
    expect(screen.getByRole('group', { name: 'Datos de la comunidad y huerto' })).toBeInTheDocument()
    expect(screen.getByRole('group', { name: 'Datos del solicitante' })).toBeInTheDocument()
    expect(screen.getByLabelText('Altitud estimada (msnm)')).toHaveValue(2500)
    expect(screen.getByRole('option', { name: 'Comité local' })).toHaveValue('COMITE_LOCAL')
    expect(screen.getByRole('option', { name: 'Otro' })).toHaveValue('OTRO')
  })

  it('permite elegir hasta 3 subtipos de cultivo', async () => {
    renderizar()
    const usuario = userEvent.setup()

    await usuario.click(screen.getByLabelText('Surcos (wachu)'))
    await usuario.click(screen.getByLabelText('Terrazas andinas'))
    await usuario.click(screen.getByLabelText('Macetas en piso'))

    expect(screen.getByLabelText('Llantas recicladas')).toBeDisabled()
    expect(screen.getByLabelText('Surcos (wachu)')).toBeEnabled()
  })

  it('no pide el consentimiento si la cédula no es válida', async () => {
    renderizar()

    const usuario = await llenarFormulario({ cedula: '1710034066' })
    await usuario.click(screen.getByRole('button', { name: 'Enviar solicitud de huerto' }))

    expect(screen.getByText('La cédula no es válida')).toBeInTheDocument()
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
  })

  it('pide el consentimiento LOPDP y solo deja continuar con la casilla marcada', async () => {
    renderizar()

    const usuario = await llenarFormulario()
    await usuario.click(screen.getByRole('button', { name: 'Enviar solicitud de huerto' }))

    expect(screen.getByRole('dialog', { name: 'Protección de Datos Personales (LOPDP Ecuador)' })).toBeInTheDocument()
    const aceptar = screen.getByRole('button', { name: 'Aceptar y Continuar' })
    expect(aceptar).toBeDisabled()
    await usuario.click(screen.getByRole('checkbox', { name: /Declaro que soy mayor de 18 años/ }))
    expect(aceptar).toBeEnabled()
  })

  it('cierra el consentimiento con Cancelar sin enviar la solicitud', async () => {
    let peticiones = 0
    server.use(http.post('*/api/publico/solicitudes-comunidad', () => {
      peticiones++
      return HttpResponse.json({}, { status: 201 })
    }))
    renderizar()

    const usuario = await llenarFormulario()
    await usuario.click(screen.getByRole('button', { name: 'Enviar solicitud de huerto' }))
    await usuario.click(screen.getByRole('button', { name: 'Cancelar' }))

    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
    expect(screen.getByLabelText('Nombre de la comunidad')).toHaveValue('Huerto Comunitario Allpa Viva')
    expect(peticiones).toBe(0)
  })

  it('envía la solicitud tras el consentimiento y felicita al solicitante', async () => {
    renderizar()

    const usuario = await llenarFormulario()
    await usuario.click(screen.getByRole('button', { name: 'Enviar solicitud de huerto' }))
    await usuario.click(screen.getByRole('checkbox', { name: /Declaro que soy mayor de 18 años/ }))
    await usuario.click(screen.getByRole('button', { name: 'Aceptar y Continuar' }))

    const confirmacion = await screen.findByRole('dialog', { name: '¡Felicidades por sembrar esperanza!' })
    expect(confirmacion).toHaveTextContent('Huerto Comunitario Allpa Viva')
    expect(confirmacion).toHaveTextContent('comunidad.allpa@huertos.gob.ec')
    expect(screen.getByAltText('Kinti Guía celebrando logro')).toBeInTheDocument()

    await usuario.click(screen.getByRole('button', { name: 'Entendido' }))
    expect(screen.getByText('Página de inicio')).toBeInTheDocument()
  })

  it('muestra el conflicto del servidor y conserva los datos', async () => {
    server.use(http.post('*/api/publico/solicitudes-comunidad', () =>
      HttpResponse.json(
        {
          status: 409,
          detail: 'Ya existe una solicitud pendiente o una comunidad registrada con el nombre o la cédula indicada',
        },
        { status: 409 },
      ),
    ))
    renderizar()

    const usuario = await llenarFormulario()
    await usuario.click(screen.getByRole('button', { name: 'Enviar solicitud de huerto' }))
    await usuario.click(screen.getByRole('checkbox', { name: /Declaro que soy mayor de 18 años/ }))
    await usuario.click(screen.getByRole('button', { name: 'Aceptar y Continuar' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('Ya existe una solicitud pendiente')
    expect(screen.getByLabelText('Cédula de identidad')).toHaveValue('1710034065')
  })

  it('ofrece las 24 provincias del Ecuador en una lista', () => {
    renderizar()

    const provincia = screen.getByLabelText('Provincia')
    expect(provincia.tagName).toBe('SELECT')
    expect(screen.getByRole('option', { name: 'Pichincha' })).toHaveValue('Pichincha')
    expect(screen.getByRole('option', { name: 'Santo Domingo de los Tsáchilas' })).toBeInTheDocument()
    expect(screen.getByRole('option', { name: 'Galápagos' })).toBeInTheDocument()
    expect(provincia.querySelectorAll('option[value]:not([value=""])')).toHaveLength(24)
  })

  it('al marcar en el mapa llena latitud, longitud, provincia y parroquia', async () => {
    renderizar()
    const usuario = userEvent.setup()

    await usuario.click(screen.getByRole('button', { name: 'Seleccionar en el mapa' }))
    pulsarCentroDelMapa()

    expect(await screen.findByDisplayValue('Calderón')).toBe(screen.getByLabelText('Parroquia'))
    expect(screen.getByLabelText('Provincia')).toHaveValue('Pichincha')
    expect(Number((screen.getByLabelText('Latitud') as HTMLInputElement).value)).toBeCloseTo(-1.5, 1)
    expect(Number((screen.getByLabelText('Longitud') as HTMLInputElement).value)).toBeCloseTo(-78.5, 1)
  })

  it('no acepta un punto del mapa fuera de Ecuador', async () => {
    server.use(
      http.get(URL_GEOCODIFICACION_INVERSA, () =>
        HttpResponse.json({
          address: { city: 'Tumbes', state: 'Tumbes', country: 'Perú', country_code: 'pe' },
        }),
      ),
    )
    renderizar()
    const usuario = userEvent.setup()

    await usuario.click(screen.getByRole('button', { name: 'Seleccionar en el mapa' }))
    pulsarCentroDelMapa()

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'Ese punto está fuera de Ecuador. Elige un lugar dentro del país.',
    )
    expect(screen.getByLabelText('Latitud')).toHaveValue(null)
    expect(screen.getByLabelText('Provincia')).toHaveValue('')
  })

  it('con la provincia elegida centra el mapa en ella y no acepta puntos de otra provincia', async () => {
    const consultados: URL[] = []
    server.use(
      http.get(URL_GEOCODIFICACION_INVERSA, ({ request }) => {
        consultados.push(new URL(request.url))
        return HttpResponse.json({
          address: { municipality: 'Calderón', state: 'Pichincha', country: 'Ecuador', country_code: 'ec' },
        })
      }),
    )
    renderizar()
    const usuario = userEvent.setup()

    await usuario.selectOptions(screen.getByLabelText('Provincia'), 'Azuay')
    await usuario.click(screen.getByRole('button', { name: 'Seleccionar en el mapa' }))
    pulsarCentroDelMapa()

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'Ese punto está fuera de Azuay. Elige un lugar dentro de la provincia o cambia la provincia.',
    )
    expect(Number(consultados[0].searchParams.get('lat'))).toBeCloseTo(-3.09, 1)
    expect(Number(consultados[0].searchParams.get('lon'))).toBeCloseTo(-79.09, 1)
    expect(screen.getByLabelText('Latitud')).toHaveValue(null)
    expect(screen.getByLabelText('Provincia')).toHaveValue('Azuay')
  })

  it('con la provincia elegida acepta un punto dentro de ella', async () => {
    renderizar()
    const usuario = userEvent.setup()

    await usuario.selectOptions(screen.getByLabelText('Provincia'), 'Pichincha')
    await usuario.click(screen.getByRole('button', { name: 'Seleccionar en el mapa' }))
    pulsarCentroDelMapa()

    expect(await screen.findByDisplayValue('Calderón')).toBe(screen.getByLabelText('Parroquia'))
    expect(Number((screen.getByLabelText('Latitud') as HTMLInputElement).value)).toBeCloseTo(-0.197, 1)
    expect(screen.queryByRole('alert')).not.toBeInTheDocument()
  })

  it('usa la ubicación actual del dispositivo para llenar las coordenadas', async () => {
    simularGeolocalizacion((exito) =>
      exito({ coords: { latitude: -0.0987123, longitude: -78.4321456 } } as GeolocationPosition),
    )
    renderizar()

    await userEvent.setup().click(screen.getByRole('button', { name: 'Usar mi ubicación actual' }))

    expect(await screen.findByDisplayValue('Calderón')).toBe(screen.getByLabelText('Parroquia'))
    expect(screen.getByLabelText('Latitud')).toHaveValue(-0.0987123)
    expect(screen.getByLabelText('Longitud')).toHaveValue(-78.4321456)
  })

  it('avisa cuando no puede obtener la ubicación actual', async () => {
    simularGeolocalizacion((_exito, error) => error?.({ code: 1 } as GeolocationPositionError))
    renderizar()

    await userEvent.setup().click(screen.getByRole('button', { name: 'Usar mi ubicación actual' }))

    expect(screen.getByRole('alert')).toHaveTextContent(
      'No se pudo obtener tu ubicación. Revisa el permiso de ubicación del navegador o márcala en el mapa.',
    )
  })
})
