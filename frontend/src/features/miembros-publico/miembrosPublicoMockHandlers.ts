import { http, HttpResponse } from 'msw'
import type { PerfilPublicoMiembroResponse } from './miembrosPublicoApi'

export const mockPerfilPublicoMiembro: PerfilPublicoMiembroResponse = {
  nombreCompleto: 'María Tránsito Amaguaña Alba',
  nivel: 'OFICIAL',
  puntosTotales: 120,
  comunidades: ['Huerto Comunitario Allpa Viva', 'Sembradores de San Antonio'],
  insignias: [
    {
      nombre: 'Maestro del Abonaje 1',
      descripcion: 'Completó 50 sesiones de preparación y distribución de compost',
      tipo: 'TALENTO_COMUNIDAD',
      fechaObtencion: '2026-11-05',
    },
    {
      nombre: 'Guardián de la Chakra Kiwa',
      descripcion: 'Reconocimiento oficial MSP en cultivo y cosecha de plantas medicinales',
      tipo: 'OFICIAL_SABER_MSP',
      fechaObtencion: '2026-10-20',
    },
  ],
}

export const miembrosPublicoMockHandlers = [
  http.get('*/api/publico/miembros/cedula/:cedula', ({ params }) => {
    const cedula = String(params.cedula ?? '')
    if (!/^[0-9]{10}$/.test(cedula)) {
      return HttpResponse.json(
        { detail: 'La cédula debe contener exactamente 10 dígitos numéricos' },
        { status: 400 },
      )
    }
    if (cedula === '1710034065') {
      return HttpResponse.json(mockPerfilPublicoMiembro)
    }
    if (cedula === '1710034073') {
      return HttpResponse.json({
        nombreCompleto: 'José Manuel Quispe',
        nivel: 'APRENDIZ',
        puntosTotales: 0,
        comunidades: [],
        insignias: [],
      } satisfies PerfilPublicoMiembroResponse)
    }
    return HttpResponse.json(
      {
        detail:
          'No se encontró ningún voluntario registrado con la cédula ingresada. Verifica el número o anímate a registrar una nueva comunidad',
      },
      { status: 404 },
    )
  }),
]
