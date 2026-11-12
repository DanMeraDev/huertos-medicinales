import { ApiError, apiGet, apiPost } from '../../api/client'

export type CategoriaActividad =
  | 'LIMPIEZA_DESHIERBE'
  | 'RIEGO'
  | 'ABONO_COMPOST'
  | 'REPLANTE_APORQUE'
  | 'CUIDADO_GENERAL'

export interface ActividadCatalogo {
  id: number
  nombre: string
  categoria: CategoriaActividad
  puntosPorHora: number
  activa: boolean
}

export interface CrearActividadCatalogoPayload {
  nombre: string
  categoria: CategoriaActividad
}

export async function listarActividades(): Promise<ActividadCatalogo[]> {
  return apiGet<ActividadCatalogo[]>('/comunidad/actividades-catalogo')
}

export async function crearActividad(
  datos: CrearActividadCatalogoPayload
): Promise<ActividadCatalogo> {
  try {
    return await apiPost<ActividadCatalogo>('/comunidad/actividades-catalogo', datos)
  } catch (err) {
    if (err instanceof ApiError && err.status === 409) {
      throw new Error('Ya existe una actividad con este nombre en tu comunidad')
    }
    throw err
  }
}