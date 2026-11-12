import { apiGet, apiPut } from '../../api/client'

export type DisenoPrincipal = 'EN_TIERRA' | 'VERTICAL' | 'COLGANTE' | 'EN_PISO'
export type TipoSuelo = 'ARCILLOSO' | 'LIMOSO' | 'ARENOSO' | 'MARGOSO' | 'GREDOSO' | 'PANTANOSO'
export type FuenteAgua = 'RIO' | 'CANAL' | 'GOTEO' | 'VERTIENTE_LLUVIA' | 'GRIFO_REPOSADO'
export type TipoAbono =
  | 'BARBECHO'
  | 'RESTOS_VEGETALES'
  | 'HOJAS_NITROGENO'
  | 'COMPOST'
  | 'PURIN_LIQUIDO'
  | 'ESTIERCOL_SECADO'
export type TipoCerca =
  | 'CERCA_VIVA'
  | 'MATERIAL_NATURAL'
  | 'MATERIAL_RECICLADO'
  | 'MATERIAL_METALICO'
  | 'CONVENCIONAL'

export interface ActualizarHuertoRequest {
  latitud: number
  longitud: number
  altitudMsnm: number
  orientacion: string | null
  areaTotalM2: number
  disenoPrincipal: DisenoPrincipal
  tipoSuelo: TipoSuelo | null
  fuenteAgua: FuenteAgua | null
  tipoAbono: TipoAbono | null
  tipoCerca: TipoCerca | null
  detalleCerca: string | null
}

export interface HuertoDetalleResponse {
  id: number
  comunidadId: number
  nombreHuerto: string
  latitud: number | null
  longitud: number | null
  altitudMsnm: number | null
  orientacion: string | null
  areaTotalM2: number | null
  disenoPrincipal: DisenoPrincipal | null
  tipoSuelo: TipoSuelo | null
  fuenteAgua: FuenteAgua | null
  tipoAbono: TipoAbono | null
  tipoCerca: TipoCerca | null
  detalleCerca: string | null
  tipoHuerto: string
  puntosHuerto: number
  areaOcupadaSubhuertosM2: number
}

export function obtenerHuertoComunidad(): Promise<HuertoDetalleResponse> {
  return apiGet<HuertoDetalleResponse>('/comunidad/huerto')
}

export function actualizarDatosOperativos(datos: ActualizarHuertoRequest): Promise<HuertoDetalleResponse> {
  return apiPut<HuertoDetalleResponse>('/comunidad/huerto', datos)
}