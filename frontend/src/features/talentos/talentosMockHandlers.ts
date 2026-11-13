import { http, HttpResponse } from 'msw'
import type {
  ActividadOpcion,
  CrearTalentoComunidadRequest,
  TalentoComunidadResponse,
} from './talentosApi'

export const mockActividadesParaTalentos: ActividadOpcion[] = [
  { id: 1, nombre: 'Riego matutino de eras' },
  { id: 2, nombre: 'Abonar camas de siembra' },
]

export const mockTalentos: TalentoComunidadResponse[] = [
  {
    id: 1,
    comunidadId: 3,
    actividadComunidadId: 2,
    nombreActividad: 'Abonar camas de siembra',
    nombreInsignia: 'Maestro del Abonaje 1',
    descripcion: 'Otorgada al miembro voluntario que complete 50 sesiones de preparación y distribución de compost',
    metaRepeticiones: 50,
    iconoCodigo: 'insignia-semilla',
    activo: true,
    creadoEn: '2026-11-10T12:00:00',
  },
]

export const talentosMockHandlers = [
  http.get('*/api/comunidades/mi-comunidad/talentos', () => {
    return HttpResponse.json(mockTalentos)
  }),
  http.get('*/api/comunidad/actividades-catalogo', () => {
    return HttpResponse.json(mockActividadesParaTalentos)
  }),
  http.post('*/api/comunidades/mi-comunidad/talentos', async ({ request }) => {
    const body = (await request.json()) as CrearTalentoComunidadRequest
    if (body.nombreInsignia === 'Maestro del Abonaje 1' || body.nombreInsignia === 'Talento Duplicado') {
      return HttpResponse.json(
        { detail: 'Ya existe un talento o medalla registrada con este nombre en tu comunidad' },
        { status: 409 },
      )
    }
    if (body.actividadComunidadId === 99) {
      return HttpResponse.json(
        { detail: 'La actividad comunitaria especificada no existe o no pertenece a tu comunidad' },
        { status: 404 },
      )
    }
    const nueva: TalentoComunidadResponse = {
      id: mockTalentos.length + 1,
      comunidadId: 3,
      actividadComunidadId: body.actividadComunidadId,
      nombreActividad:
        mockActividadesParaTalentos.find((a) => a.id === body.actividadComunidadId)?.nombre ?? 'Actividad',
      nombreInsignia: body.nombreInsignia,
      descripcion: body.descripcion,
      metaRepeticiones: body.metaRepeticiones,
      iconoCodigo: 'insignia-semilla',
      activo: true,
      creadoEn: '2026-11-12T12:00:00',
    }
    return HttpResponse.json(nueva, { status: 201 })
  }),
]
