import { apiGet } from '../../api/client'

export interface PingResponse {
  estado: string
  baseDeDatos: string
  fecha: string
}

export const obtenerPing = () => apiGet<PingResponse>('/ping')
