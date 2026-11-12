import { apiGet } from '../../api/client'

export interface DirectorioComunidadResponse {
  id: number
  nombre: string
  nombreResponsable: string
  telefonoContacto: string
  correoContacto: string
  totalMiembros: number
  provincia: string
  parroquia: string
  direccion: string
  latitud: number | null
  longitud: number | null
  altitudMsnm: number | null
}

export function obtenerDirectorioComunidades(): Promise<DirectorioComunidadResponse[]> {
  return apiGet<DirectorioComunidadResponse[]>('/publico/comunidades')
}
