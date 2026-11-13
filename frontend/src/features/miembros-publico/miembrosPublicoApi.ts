import { apiGet } from '../../api/client'

export type NivelMiembro = 'APRENDIZ' | 'OFICIAL' | 'MAESTRO'

export interface InsigniaObtenidaDto {
  nombre: string
  descripcion: string
  tipo: string
  fechaObtencion: string
}

export interface PerfilPublicoMiembroResponse {
  nombreCompleto: string
  nivel: NivelMiembro
  puntosTotales: number
  comunidades: string[]
  insignias: InsigniaObtenidaDto[]
}

export function consultarPerfilPorCedula(cedula: string): Promise<PerfilPublicoMiembroResponse> {
  return apiGet<PerfilPublicoMiembroResponse>(`/publico/miembros/cedula/${encodeURIComponent(cedula)}`)
}
