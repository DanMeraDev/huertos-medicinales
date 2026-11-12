export type TipoOrganizacion = 'INDIVIDUAL' | 'FAMILIA' | 'CONDOMINIO' | 'BARRIO' | 'COMITE_LOCAL'

export interface RankingComunidad {
  posicion: number
  comunidadId: number
  nombreComunidad: string
  tipoOrganizacion: TipoOrganizacion
  provincia: string
  parroquia: string
  puntosComunidad: number
}

export const ETIQUETAS_TIPO_ORGANIZACION: Record<TipoOrganizacion, string> = {
  INDIVIDUAL: 'Individual',
  FAMILIA: 'Familiar',
  CONDOMINIO: 'Condominio',
  BARRIO: 'Barrio',
  COMITE_LOCAL: 'Comité Local',
}
