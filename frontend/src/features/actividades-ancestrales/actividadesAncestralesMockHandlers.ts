import { http, HttpResponse } from 'msw'
import type {
  ActividadAncestralResponse,
  MiembroComunidadOpcion,
  RegistrarActividadAncestralRequest,
} from './actividadesAncestralesApi'

export const mockMiembrosComunidad: MiembroComunidadOpcion[] = [
  { id: 7, nombreCompleto: 'María Tránsito Amaguaña Alba' },
  { id: 8, nombreCompleto: 'Segundo Quishpe Cueva' },
]

export const actividadesAncestralesMockHandlers = [
  http.get('*/api/comunidades/mi-comunidad/miembros', () =>
    HttpResponse.json(mockMiembrosComunidad),
  ),
  http.post('*/api/actividades-ancestrales', async ({ request }) => {
    const datos = (await request.json()) as RegistrarActividadAncestralRequest

    if (!datos.participantes || datos.participantes.length === 0) {
      return HttpResponse.json(
        { detail: 'Debe registrar al menos un miembro participante en la actividad ancestral' },
        { status: 400 },
      )
    }

    if (datos.evidenciaFotoUrl === 'https://error.gob.ec/foto.jpg') {
      return HttpResponse.json(
        { detail: 'La fotografía de evidencia es obligatoria para la revisión del MSP' },
        { status: 400 },
      )
    }

    const respuesta: ActividadAncestralResponse = {
      id: 21,
      comunidadId: 3,
      tituloSaber: 'Ritual de siembra',
      estado: 'PENDIENTE',
      numeroIntento: 1,
      fechaRealizacion: datos.fechaRealizacion,
      participantes: datos.participantes,
    }
    return HttpResponse.json(respuesta, { status: 201 })
  }),
]
