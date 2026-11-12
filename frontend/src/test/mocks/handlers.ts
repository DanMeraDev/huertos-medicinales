import { http, HttpResponse } from 'msw'
import { plantasHandlers } from '../../features/catalogo-plantas/plantasMockHandlers'
import { authMockHandlers } from '../../features/auth/authMockHandlers'
import { solicitudesPublicoMockHandlers } from '../../features/solicitudes-publico/solicitudesPublicoMockHandlers'
import { usuariosMockHandlers } from '../../features/usuarios/usuariosMockHandlers'
import type { ActualizarComunidadRequest, ComunidadDetalleResponse } from '../../features/comunidades/comunidadesApi'

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

export const handlers = [
  http.get('*/api/ping', () =>
    HttpResponse.json({ estado: 'ok', baseDeDatos: '11.4.8-MariaDB', fecha: '2026-10-31T12:00:00Z' }),
  ),
  ...plantasHandlers,
  ...authMockHandlers,

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
]
