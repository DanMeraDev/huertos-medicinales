import { apiGet, apiPost } from '../../api/client'

export type SubtipoSubhuerto =
  | 'SURCOS_WACHU'
  | 'TERRAZAS_ANTI'
  | 'CAMELLONES_INKA_WACHU'
  | 'VERTICAL_PARED'
  | 'COLGANTE_TECHO'
  | 'MACETAS_PISO'
  | 'LLANTAS_RECICLADAS'

export interface Subhuerto {
  id: number
  huertoId: number
  nombreZona: string
  subtipo: SubtipoSubhuerto
  areaM2: number
  activo: boolean
}

export interface CrearSubhuertoPayload {
  nombreZona: string
  subtipo: SubtipoSubhuerto
  areaM2: number
}

interface HuertoResumen {
  areaTotalM2: number | null
}

export async function obtenerAreaTotalHuerto(): Promise<number | null> {
  try {
    const huerto = await apiGet<HuertoResumen>('/comunidad/huerto')
    return huerto.areaTotalM2 ?? null
  } catch {
    return null
  }
}

export async function obtenerSubhuertos(): Promise<Subhuerto[]> {
  return apiGet<Subhuerto[]>('/comunidad/subhuertos')
}

export async function crearSubhuerto(datos: CrearSubhuertoPayload): Promise<Subhuerto> {
  return apiPost<Subhuerto>('/comunidad/subhuertos', datos)
}