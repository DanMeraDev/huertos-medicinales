import { http, HttpResponse } from 'msw'
import type {
  MaterialVegetalResponse,
  RegistrarBajaMaterialRequest,
  RegistrarMaterialVegetalRequest,
} from './types'

export const mockInventarioBodega: MaterialVegetalResponse[] = [
  {
    id: 1,
    comunidadId: 1,
    plantaId: 1,
    nombreComunPlanta: 'Manzanilla',
    nombreCientificoPlanta: 'Matricaria chamomilla',
    tipoMaterial: 'SEMILLAS',
    cantidadDisponible: 500,
    unidadMedida: 'GRAMOS',
    loteOrigen: 'Lote MZ-2026-A',
    fechaIngreso: '2026-11-10',
  },
  {
    id: 2,
    comunidadId: 1,
    plantaId: 2,
    nombreComunPlanta: 'Menta',
    nombreCientificoPlanta: 'Mentha spicata',
    tipoMaterial: 'ESQUEJES',
    cantidadDisponible: 25,
    unidadMedida: 'UNIDADES',
    loteOrigen: 'Donación vivero local',
    fechaIngreso: '2026-11-11',
  },
]

export const bodegaMockHandlers = [
  http.get('*/api/bodega/comunidades/:comunidadId/material-vegetal', ({ params }) => {
    const comunidadId = Number(params.comunidadId)
    if (comunidadId === 999) {
      return HttpResponse.json(
        { detail: 'No tiene permisos para consultar el inventario de otra comunidad' },
        { status: 403 },
      )
    }
    if (comunidadId === 2) {
      return HttpResponse.json([])
    }
    return HttpResponse.json(
      mockInventarioBodega.map((item) => ({ ...item, comunidadId })),
    )
  }),

  http.post('*/api/bodega/material-vegetal', async ({ request }) => {
    const cuerpo = (await request.json()) as RegistrarMaterialVegetalRequest

    if (!cuerpo.cantidad || cuerpo.cantidad <= 0) {
      return HttpResponse.json(
        {
          detail: 'La cantidad debe ser mayor a cero',
          errores: { cantidad: 'La cantidad debe ser mayor a cero' },
        },
        { status: 400 },
      )
    }

    if (cuerpo.plantaId === 999) {
      return HttpResponse.json(
        { detail: 'La planta seleccionada no existe en el catálogo' },
        { status: 404 },
      )
    }

    return HttpResponse.json(
      {
        id: 101,
        comunidadId: 1,
        plantaId: cuerpo.plantaId,
        nombreComunPlanta: cuerpo.plantaId === 2 ? 'Menta' : 'Manzanilla',
        nombreCientificoPlanta: cuerpo.plantaId === 2 ? 'Mentha spicata' : 'Matricaria chamomilla',
        tipoMaterial: cuerpo.tipoMaterial,
        cantidadDisponible: cuerpo.cantidad,
        unidadMedida: cuerpo.unidadMedida,
        loteOrigen: cuerpo.loteOrigen ?? '',
        fechaIngreso: cuerpo.fechaIngreso,
      },
      { status: 201 },
    )
  }),

  http.post('*/api/bodega/material-vegetal/:id/bajas', async ({ params, request }) => {
    const id = Number(params.id)
    const cuerpo = (await request.json()) as RegistrarBajaMaterialRequest

    if (!cuerpo.cantidadBaja || cuerpo.cantidadBaja <= 0) {
      return HttpResponse.json(
        { detail: 'La cantidad a dar de baja debe ser mayor a cero' },
        { status: 400 },
      )
    }

    if (!cuerpo.causaFracaso) {
      return HttpResponse.json(
        { detail: 'Debe seleccionar una causa de fracaso válida para el registro de la baja' },
        { status: 400 },
      )
    }

    if (id === 999) {
      return HttpResponse.json(
        { detail: 'El material vegetal indicado no existe en bodega' },
        { status: 404 },
      )
    }

    const itemExistente = mockInventarioBodega.find((item) => item.id === id)
    if (itemExistente && cuerpo.cantidadBaja > itemExistente.cantidadDisponible) {
      return HttpResponse.json(
        { detail: 'La cantidad a dar de baja supera el stock disponible en bodega' },
        { status: 400 },
      )
    }

    return HttpResponse.json(
      {
        id: 501,
        bodegaMaterialId: id,
        cantidadBaja: cuerpo.cantidadBaja,
        causaFracaso: cuerpo.causaFracaso,
        observacion: cuerpo.observacion,
        fechaBaja: '2026-11-12T10:00:00',
      },
      { status: 201 },
    )
  }),
]
