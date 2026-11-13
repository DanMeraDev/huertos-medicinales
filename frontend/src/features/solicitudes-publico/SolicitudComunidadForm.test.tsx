import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { MemoryRouter, Route, Routes } from 'react-router'
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
  await escribir(usuario, screen.getByLabelText('Provincia'), 'Pichincha')
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

describe('SolicitudComunidadForm', () => {
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
})
