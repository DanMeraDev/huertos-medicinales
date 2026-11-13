import { http, HttpResponse } from 'msw'
import type {
  ActividadAncestralResponse,
  ActividadParaCorregirResponse,
  FotografiaSubidaResponse,
  MiembroComunidadOpcion,
  RegistrarActividadAncestralRequest,
  ReenviarEvidenciasRequest,
} from './actividadesAncestralesApi'

export const mockMiembrosComunidad: MiembroComunidadOpcion[] = [
  { id: 7, nombreCompleto: 'María Tránsito Amaguaña Alba' },
  { id: 8, nombreCompleto: 'Segundo Quishpe Cueva' },
]

export const mockActividadObservada: ActividadParaCorregirResponse = {
  id: 21,
  tituloSaber: 'Ritual de siembra',
  estado: 'OBSERVADA_CORREGIR',
  numeroIntento: 1,
  observacionMsp: 'La fotografía no muestra el ritual completo',
  descripcionBitacora: 'Ritual de siembra con chicha de jora',
  evidenciaFotoUrl: 'actividades/3f2a9b1c-4d5e-6f7a-8b9c-0d1e2f3a4b5c.jpg',
  evidenciaVideoUrl: null,
}

export const actividadesAncestralesMockHandlers = [
  http.get('*/api/comunidades/mi-comunidad/miembros/activos', () =>
    HttpResponse.json(mockMiembrosComunidad),
  ),
  http.post('*/api/actividades-ancestrales/fotografia', async () => {
    const respuesta: FotografiaSubidaResponse = {
      fotografiaUrl: 'actividades/3f2a9b1c-4d5e-6f7a-8b9c-0d1e2f3a4b5c.jpg',
    }
    return HttpResponse.json(respuesta, { status: 200 })
  }),
  http.post('*/api/actividades-ancestrales', async ({ request }) => {
    const datos = (await request.json()) as RegistrarActividadAncestralRequest

    if (!datos.participantes || datos.participantes.length === 0) {
      return HttpResponse.json(
        { detail: 'Debe registrar al menos un miembro participante en la actividad ancestral' },
        { status: 400 },
      )
    }

    if (!datos.evidenciaFotoUrl) {
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
  http.get('*/api/actividades-ancestrales/:id', ({ params }) =>
    HttpResponse.json({ ...mockActividadObservada, id: Number(params.id) }),
  ),
  http.put('*/api/actividades-ancestrales/:id/reenviar', async ({ request }) => {
    const datos = (await request.json()) as ReenviarEvidenciasRequest

    if (!datos.descripcionBitacora || !datos.evidenciaFotoUrl) {
      return HttpResponse.json(
        { detail: 'La descripción de la bitácora y la fotografía son obligatorias' },
        { status: 400 },
      )
    }

    const respuesta: ActividadAncestralResponse = {
      id: 21,
      comunidadId: 3,
      tituloSaber: 'Ritual de siembra',
      estado: 'PENDIENTE',
      numeroIntento: 2,
      fechaRealizacion: '2026-11-10',
      participantes: [],
    }
    return HttpResponse.json(respuesta, { status: 200 })
  }),
]
