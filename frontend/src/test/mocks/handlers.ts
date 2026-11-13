import { http, HttpResponse } from 'msw'
import { authMockHandlers } from '../../features/auth/authMockHandlers'
import { solicitudesPublicoMockHandlers } from '../../features/solicitudes-publico/solicitudesPublicoMockHandlers'
import { usuariosMockHandlers } from '../../features/usuarios/usuariosMockHandlers'
import { rankingMockHandlers } from '../../features/ranking/rankingMockHandlers'
import { bodegaMockHandlers } from '../../features/bodega/bodegaMockHandlers'
import { calendarioLunarMockHandlers } from '../../features/calendario-lunar/calendarioLunarMockHandlers'
import { miembrosHandlers } from '../../features/miembros/miembrosMockHandlers'
import { miembrosPublicoMockHandlers } from '../../features/miembros-publico/miembrosPublicoMockHandlers'
import { talentosMockHandlers } from '../../features/talentos/talentosMockHandlers'
import { plantasHandlers } from '../../features/catalogo-plantas/plantasMockHandlers'
import { catalogoAdminHandlers } from '../../features/catalogo-admin/catalogoAdminMockHandlers'
import { solicitudesMockHandlers } from '../../features/solicitudes/solicitudesMockHandlers'
import { saberesMockHandlers } from '../../features/saberes-ancestrales/saberesMockHandlers'
import { actividadesAncestralesMockHandlers } from '../../features/actividades-ancestrales/actividadesAncestralesMockHandlers'
import type { ActualizarComunidadRequest, ComunidadDetalleResponse } from '../../features/comunidades/comunidadesApi'
import type { DirectorioComunidadResponse } from '../../features/comunidades-publico/comunidadesPublicoApi'

export const mockMiComunidad: ComunidadDetalleResponse = {
  id: 3,
  nombre: 'Huerto Comunitario Allpa Viva',
  tipoOrganizacion: 'BARRIO',
  provincia: 'Pichincha',
  parroquia: 'Calderón',
  direccion: 'Calle de las Rosas y Pasaje Los Pinos, lote 14',
  telefonoContacto: '0991234567',
  correoContacto: 'comunidad.allpa@huertos.gob.ec',
  responsableId: 10,
  nombreResponsable: 'Segundo Tránsito Amaguaña',
  puntosComunidad: 120,
  activa: true,
  creadoEn: '2026-10-15T09:30:00Z',
}

export const mockDirectorioComunidades: DirectorioComunidadResponse[] = [
  {
    id: 3,
    nombre: 'Huerto Comunitario Allpa Viva',
    nombreResponsable: 'Segundo Tránsito Amaguaña',
    telefonoContacto: '0991234567',
    correoContacto: 'comunidad.allpa@huertos.gob.ec',
    totalMiembros: 14,
    provincia: 'Pichincha',
    parroquia: 'Calderon',
    direccion: 'Calle de las Rosas y Pasaje Los Pinos, lote 14',
    latitud: -0.0987123,
    longitud: -78.4321456,
    altitudMsnm: 2650,
  },
]

export const handlers = [
  http.get('*/api/ping', () =>
    HttpResponse.json({ estado: 'ok', baseDeDatos: '11.4.8-MariaDB', fecha: '2026-10-31T12:00:00Z' }),
  ),
  ...bodegaMockHandlers,
  ...plantasHandlers,
  ...catalogoAdminHandlers,
  ...authMockHandlers,
  ...rankingMockHandlers,
  ...calendarioLunarMockHandlers,
  ...miembrosHandlers,
  ...miembrosPublicoMockHandlers,
  ...talentosMockHandlers,
  ...saberesMockHandlers,
  ...actividadesAncestralesMockHandlers,
  ...solicitudesMockHandlers,

  http.get('*/api/comunidades/mi-comunidad', () => HttpResponse.json(mockMiComunidad)),

  http.put('*/api/comunidades/mi-comunidad', async ({ request }) => {
    const datos = (await request.json()) as ActualizarComunidadRequest
    return HttpResponse.json({
      ...mockMiComunidad,
      ...datos,
    })
  }),
  ...usuariosMockHandlers,
  ...solicitudesPublicoMockHandlers,

  http.get('*/api/comunidad/huerto', () =>
    HttpResponse.json({
      id: 10,
      comunidadId: 3,
      nombreHuerto: 'Huerto Comunitario Allpa Viva',
      latitud: -0.0987123,
      longitud: -78.4321456,
      altitudMsnm: 2650,
      areaTotalM2: 120.0,
      tipoHuerto: 'BASICO',
      disenoPrincipal: 'CIRCULAR',
      puntosHuerto: 50,
      activo: true,
    }),
  ),

  http.get('*/api/comunidad/subhuertos', () =>
    HttpResponse.json([
      {
        id: 1,
        huertoId: 10,
        nombreZona: 'Surcos Norte',
        subtipo: 'SURCOS_WACHU',
        areaM2: 30.0,
        activo: true,
      },
    ]),
  ),

  http.post('*/api/comunidad/siembras', async ({ request }) => {
    const payload = (await request.json()) as {
      subhuertoId: number
      bodegaMaterialId: number
      cantidadSembrada: number
      fechaSiembra: string
      faseLunarSiembra: string
      aceptaRiesgoAltitud: boolean
    }
    return HttpResponse.json(
      {
        id: 1,
        subhuertoId: payload.subhuertoId,
        nombreSubhuerto: 'Surcos Norte',
        bodegaMaterialId: payload.bodegaMaterialId,
        plantaId: 1,
        nombreComunPlanta: 'Manzanilla',
        cualidadTermica: 'CALIENTE',
        cantidadSembrada: payload.cantidadSembrada,
        fechaSiembra: payload.fechaSiembra,
        faseLunarSiembra: payload.faseLunarSiembra,
        tuvoAlertaAltitud: payload.aceptaRiesgoAltitud,
        estadoCultivo: 'SEMBRADO',
      },
      { status: 201 },
    )
  }),

  http.get('*/api/comunidad/actividades-catalogo', () =>
    HttpResponse.json([
      {
        id: 1,
        nombre: 'Riego matutino de eras',
        categoria: 'RIEGO',
        puntosPorHora: 1,
        activa: true,
      },
      {
        id: 2,
        nombre: 'Deshierbe manual',
        categoria: 'LIMPIEZA_DESHIERBE',
        puntosPorHora: 1,
        activa: true,
      },
    ]),
  ),

  http.post('*/api/comunidad/actividades-catalogo', async ({ request }) => {
    const payload = (await request.json()) as { nombre: string; categoria: string }
    return HttpResponse.json(
      {
        id: 3,
        nombre: payload.nombre,
        categoria: payload.categoria,
        puntosPorHora: 1,
        activa: true,
      },
      { status: 201 },
    )
  }),

  http.get('*/api/comunidad/actividades-miembros/miembros/:id/insignias', () =>
    HttpResponse.json([
      {
        id: 1,
        codigo: 'TALENTO_RIEGO_1',
        nombre: 'Guardián del Agua',
        tipo: 'TALENTO_COMUNIDAD',
        descripcion: 'Realizó labores de riego',
        fechaObtencion: '2026-11-12',
      },
    ]),
  ),

  http.post('*/api/comunidad/actividades-miembros', async ({ request }) => {
    const payload = (await request.json()) as {
      miembroId: number
      actividadId: number
      horasTrabajadas: number
      fechaLabor: string
      observaciones?: string
    }
    const puntos = Math.round(payload.horasTrabajadas * 1)
    return HttpResponse.json(
      {
        registroId: 10,
        miembroId: payload.miembroId,
        nombreMiembro: 'Segundo Tránsito Amaguaña',
        rangoActual: 'OFICIAL',
        rangoAnterior: 'APRENDIZ',
        subioDeNivel: true,
        puntosAcreditados: puntos,
        puntosTotalesActuales: 50,
        nuevosTalentosDesbloqueados: [],
        mensajeCelebracion: '¡Felicitaciones! Has ascendido al rango OFICIAL.',
      },
      { status: 201 },
    )
  }),

  http.get('*/api/publico/comunidades', () => HttpResponse.json(mockDirectorioComunidades)),
]
