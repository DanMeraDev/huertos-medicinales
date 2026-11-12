import { http, HttpResponse } from 'msw'
import type { RegistrarMiembroRequest, MiembroDetalleResponse } from './miembrosApi'

export const mockMiembroDetalle: MiembroDetalleResponse = {
  id: 15,
  comunidadId: 3,
  cedula: '1710034065',
  nombreCompleto: 'María Tránsito Amaguaña Alba',
  nombres: 'María Tránsito',
  apellidos: 'Amaguaña Alba',
  fechaNacimiento: '2010-03-25',
  edad: 16,
  genero: 'FEMENINO',
  telefono: '0981234567',
  nivel: 'APRENDIZ',
  puntosTotales: 0,
  activoEnComunidad: true,
  fechaIngreso: '2026-11-12',
}

export const miembrosHandlers = [
  http.post('*/api/comunidades/mi-comunidad/miembros', async ({ request }) => {
    const datos = (await request.json()) as RegistrarMiembroRequest
    if (datos.cedula === '1712345675') {
      return HttpResponse.json(
        { detail: 'El voluntario con la cédula ingresada ya se encuentra registrado y activo en esta comunidad' },
        { status: 409 },
      )
    }
    return HttpResponse.json(
      {
        ...mockMiembroDetalle,
        ...datos,
        id: 15,
        comunidadId: 3,
        nombreCompleto: `${datos.nombres} ${datos.apellidos}`,
        edad: 16,
        puntosTotales: 0,
        activoEnComunidad: true,
        fechaIngreso: '2026-11-12',
      },
      { status: 201 },
    )
  }),
]
