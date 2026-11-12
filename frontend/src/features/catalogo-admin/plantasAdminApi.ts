import { ApiError, apiGet, apiPost, apiPostFormData, apiPut } from '../../api/client'
import { MENSAJE_FORMATO_NO_ADMITIDO, MENSAJE_IMAGEN_DEMASIADO_GRANDE } from '../../components/CargadorImagen'
import type {
  ActualizarPlantaRequest,
  CrearPlantaRequest,
  FotografiaSubidaResponse,
  PlantaDetalleResponse,
} from './types'

const MENSAJE_ERROR_SUBIDA = 'No se pudo subir la fotografía. Intente nuevamente'

export function subirFotografiaPlanta(archivo: File) {
  const datos = new FormData()
  datos.append('archivo', archivo)
  return apiPostFormData<FotografiaSubidaResponse>('/msp/plantas/fotografia', datos)
}

export function mensajeErrorFotografia(error: unknown) {
  if (error instanceof ApiError && error.status === 415) return MENSAJE_FORMATO_NO_ADMITIDO
  if (error instanceof ApiError && error.status === 413) return MENSAJE_IMAGEN_DEMASIADO_GRANDE
  return MENSAJE_ERROR_SUBIDA
}

export function registrarPlanta(datos: CrearPlantaRequest) {
  return apiPost<PlantaDetalleResponse>('/msp/plantas', datos)
}

export function obtenerPlantaParaEditar(id: number) {
  return apiGet<PlantaDetalleResponse>(`/publico/plantas/${id}`)
}

export function actualizarPlanta(id: number, datos: ActualizarPlantaRequest) {
  return apiPut<PlantaDetalleResponse>(`/msp/plantas/${id}`, datos)
}
