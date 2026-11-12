import { http, HttpResponse } from 'msw'
import type { RankingComunidad } from './types'

export const rankingMock: RankingComunidad[] = [
  {
    posicion: 1,
    comunidadId: 1,
    nombreComunidad: 'Huerto Comunitario Allpa Kawsay',
    tipoOrganizacion: 'BARRIO',
    provincia: 'Pichincha',
    parroquia: 'Calderón',
    puntosComunidad: 380,
  },
  {
    posicion: 2,
    comunidadId: 2,
    nombreComunidad: 'Chakra Ancestral Sumak Muyu',
    tipoOrganizacion: 'COMITE_LOCAL',
    provincia: 'Imbabura',
    parroquia: 'Otavalo',
    puntosComunidad: 320,
  },
  {
    posicion: 3,
    comunidadId: 3,
    nombreComunidad: 'Huerto Medicinal Yaku Allpa',
    tipoOrganizacion: 'FAMILIA',
    provincia: 'Azuay',
    parroquia: 'El Valle',
    puntosComunidad: 275,
  },
  {
    posicion: 4,
    comunidadId: 4,
    nombreComunidad: 'Huerto Verde Esperanza',
    tipoOrganizacion: 'BARRIO',
    provincia: 'Guayas',
    parroquia: 'Chongón',
    puntosComunidad: 210,
  },
  {
    posicion: 5,
    comunidadId: 5,
    nombreComunidad: 'Jardín Medicinal Chibuleo',
    tipoOrganizacion: 'COMITE_LOCAL',
    provincia: 'Tungurahua',
    parroquia: 'Pilahuín',
    puntosComunidad: 190,
  },
  {
    posicion: 6,
    comunidadId: 6,
    nombreComunidad: 'Huerto Agroecológico Sacha Ñan',
    tipoOrganizacion: 'INDIVIDUAL',
    provincia: 'Pastaza',
    parroquia: 'Puyo',
    puntosComunidad: 140,
  },
  {
    posicion: 7,
    comunidadId: 7,
    nombreComunidad: 'Semillas Ancestrales de Vilcabamba',
    tipoOrganizacion: 'CONDOMINIO',
    provincia: 'Loja',
    parroquia: 'Vilcabamba',
    puntosComunidad: 110,
  },
]

export const rankingMockHandlers = [
  http.get('*/api/publico/ranking/comunidades', () => HttpResponse.json(rankingMock)),
]
