import { http, HttpResponse } from 'msw'
import type {
  RegistrarMiembroRequest,
  MiembroDetalleResponse,
  CambiarEstadoMiembroRequest,
  ActualizarMiembroRequest,
  FichaInternaMiembroResponse,
} from './miembrosApi'

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

export const mockFichaMiembro: FichaInternaMiembroResponse = {
  id: 15,
  comunidadId: 3,
  nombreComunidad: 'Huerto Comunitario Allpa Viva',
  cedula: '1710034065',
  nombres: 'María Tránsito',
  apellidos: 'Amaguaña Alba',
  nombreCompleto: 'María Tránsito Amaguaña Alba',
  fechaNacimiento: '2010-03-25',
  edad: 16,
  genero: 'FEMENINO',
  telefono: '0981234567',
  nivel: 'APRENDIZ',
  puntosTotales: 45,
  activoEnComunidad: true,
  fechaIngreso: '2026-11-10',
  fechaBaja: null,
  motivoBaja: null,
}

export const mockMiembrosLista: MiembroDetalleResponse[] = [
  mockMiembroDetalle,
  {
    id: 16,
    comunidadId: 3,
    cedula: '1710034073',
    nombreCompleto: 'José Manuel Quishpe',
    nombres: 'José Manuel',
    apellidos: 'Quishpe',
    fechaNacimiento: '1995-08-14',
    edad: 31,
    genero: 'MASCULINO',
    telefono: '0998765432',
    nivel: 'OFICIAL',
    puntosTotales: 150,
    activoEnComunidad: false,
    fechaIngreso: '2026-10-01',
  },
]

export const miembrosHandlers = [
  http.get('*/api/comunidades/mi-comunidad/miembros', () => {
    return HttpResponse.json(mockMiembrosLista)
  }),
  http.get('*/api/comunidades/mi-comunidad/miembros/:miembroId', ({ params }) => {
    const { miembroId } = params
    if (miembroId === '99') {
      return HttpResponse.json(
        { detail: 'No se encontró la ficha del voluntario en tu comunidad' },
        { status: 404 },
      )
    }
    const idNum = Number(miembroId)
    if (idNum === 16) {
      return HttpResponse.json({
        ...mockFichaMiembro,
        id: 16,
        cedula: '1710034073',
        nombres: 'José Manuel',
        apellidos: 'Quishpe',
        nombreCompleto: 'José Manuel Quishpe',
        fechaNacimiento: '1995-08-14',
        edad: 31,
        genero: 'MASCULINO',
        telefono: '0998765432',
        nivel: 'OFICIAL',
        puntosTotales: 150,
        activoEnComunidad: false,
        fechaIngreso: '2026-10-01',
        fechaBaja: '2026-11-12',
        motivoBaja: 'Cambio de residencia',
      })
    }
    return HttpResponse.json(mockFichaMiembro)
  }),
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
  http.patch('*/api/comunidades/mi-comunidad/miembros/:miembroId/estado', async ({ params, request }) => {
    const { miembroId } = params
    if (miembroId === '99') {
      return HttpResponse.json(
        { detail: 'El miembro especificado no pertenece a tu comunidad' },
        { status: 404 },
      )
    }
    const body = (await request.json()) as CambiarEstadoMiembroRequest
    if (body.motivoBaja && body.motivoBaja.length > 255) {
      return HttpResponse.json(
        {
          detail: 'Datos no válidos',
          errores: { motivoBaja: 'El motivo de baja no puede superar 255 caracteres' },
        },
        { status: 400 },
      )
    }
    const idNum = Number(miembroId)
    const nombre = idNum === 16 ? 'José Manuel Quishpe' : 'María Tránsito Amaguaña Alba'
    return HttpResponse.json({
      miembroId: idNum,
      comunidadId: 3,
      nombreMiembro: nombre,
      activoEnComunidad: body.activo,
      fechaBaja: body.activo ? null : '2026-11-12',
      motivoBaja: body.activo ? null : (body.motivoBaja ?? null),
      mensaje: body.activo
        ? 'El miembro ha sido reactivado en la comunidad exitosamente'
        : 'El miembro ha sido dado de baja de la comunidad exitosamente',
    })
  }),
  http.put('*/api/comunidades/mi-comunidad/miembros/:miembroId', async ({ params, request }) => {
    const { miembroId } = params
    if (miembroId === '99') {
      return HttpResponse.json(
        { detail: 'El voluntario no pertenece a la comunidad administrada' },
        { status: 404 },
      )
    }
    const body = (await request.json()) as ActualizarMiembroRequest
    if (!body.telefono || !/^09[0-9]{8}$/.test(body.telefono)) {
      return HttpResponse.json(
        {
          detail: 'Datos no válidos',
          errores: { telefono: 'El teléfono debe iniciar con 09 y tener 10 dígitos' },
        },
        { status: 400 },
      )
    }
    const idNum = Number(miembroId)
    const base = mockMiembrosLista.find((m) => m.id === idNum) ?? mockMiembroDetalle
    return HttpResponse.json({
      ...base,
      genero: body.genero,
      telefono: body.telefono,
    })
  }),
]
