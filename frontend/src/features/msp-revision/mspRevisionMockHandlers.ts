import { http, HttpResponse } from 'msw'
import type { ActividadRevision, VerificarActividadMspRequest } from './mspRevisionApi'

export const mockActividadesPendientes: ActividadRevision[] = [
  {
    id: 21,
    comunidadId: 3,
    nombreComunidad: 'Huerto Comunitario Allpa Viva',
    tituloSaber: 'Ritual de siembra',
    fechaRealizacion: '2026-11-10',
    faseLunarEjecucion: 'PURA_KILLA_LLENA',
    descripcionBitacora: 'Ritual de siembra con chicha de jora y sahumerio',
    evidenciaFotoUrl: 'actividades/3f2a9b1c-4d5e-6f7a-8b9c-0d1e2f3a4b5c.jpg',
    evidenciaVideoUrl: 'https://evidencias.gob.ec/video-ritual.mp4',
    numeroIntento: 1,
    estado: 'PENDIENTE',
    participantes: [
      { miembroId: 7, nombreCompleto: 'María Tránsito Amaguaña Alba', horasTrabajadas: 4 },
    ],
  },
]

export const mspRevisionMockHandlers = [
  http.get('*/api/msp/actividades-ancestrales/pendientes', () =>
    HttpResponse.json(mockActividadesPendientes),
  ),
  http.post('*/api/msp/actividades-ancestrales/:id/verificar', async ({ params, request }) => {
    const datos = (await request.json()) as VerificarActividadMspRequest
    const requiereObservacion =
      datos.nuevoEstado === 'OBSERVADA_CORREGIR' || datos.nuevoEstado === 'RECHAZADA_DEFINITIVA'

    if (requiereObservacion && !datos.observacion) {
      return HttpResponse.json(
        { detail: 'Debe ingresar una observación detallada para justificar la observación o rechazo' },
        { status: 400 },
      )
    }

    return HttpResponse.json({
      actividadId: Number(params.id),
      estado: datos.nuevoEstado,
      puntosComunidadAcreditados: datos.nuevoEstado === 'APROBADA' ? 20 : 0,
      fechaVerificacion: '2026-11-12T15:00:00',
    })
  }),
]
