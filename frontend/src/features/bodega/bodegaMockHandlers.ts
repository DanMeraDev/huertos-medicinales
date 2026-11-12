import { http, HttpResponse } from 'msw'
import type { RegistrarMaterialVegetalRequest } from './types'

export const bodegaMockHandlers = [
  http.get('*/api/publico/plantas', () =>
    HttpResponse.json({
      contenido: [
        { id: 1, nombreComun: 'Manzanilla' },
        { id: 2, nombreComun: 'Menta' },
        { id: 3, nombreComun: 'Toronjil' },
      ],
      pagina: 0,
      tamanio: 10,
      totalElementos: 3,
      totalPaginas: 1,
    }),
  ),

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
]
