import type { NivelMiembro } from '../miembros/miembrosApi'

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

export interface RankingMiembro {
  posicion: number
  miembroId: number
  nombres: string
  apellidos: string
  nivel: NivelMiembro
  puntosTotales: number
  totalInsignias: number
}

export const ETIQUETAS_TIPO_ORGANIZACION: Record<TipoOrganizacion, string> = {
  INDIVIDUAL: 'Individual',
  FAMILIA: 'Familiar',
  CONDOMINIO: 'Condominio',
  BARRIO: 'Barrio',
  COMITE_LOCAL: 'Comité Local',
}

export const ETIQUETAS_NIVEL_MIEMBRO: Record<NivelMiembro, string> = {
  APRENDIZ: 'Aprendiz',
  OFICIAL: 'Oficial',
  MAESTRO: 'Maestro',
}

export const TONO_NIVEL_MIEMBRO: Record<NivelMiembro, 'neutro' | 'exito' | 'advertencia'> = {
  APRENDIZ: 'neutro',
  OFICIAL: 'advertencia',
  MAESTRO: 'exito',
}

