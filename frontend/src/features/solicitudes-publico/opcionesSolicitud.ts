export const MAXIMO_SUBTIPOS = 3

export const OPCIONES_TIPO_ORGANIZACION = [
  { valor: 'INDIVIDUAL', etiqueta: 'Individual' },
  { valor: 'FAMILIA', etiqueta: 'Familia' },
  { valor: 'CONDOMINIO', etiqueta: 'Condominio' },
  { valor: 'BARRIO', etiqueta: 'Barrio' },
  { valor: 'COMITE_LOCAL', etiqueta: 'Comité local' },
]

export const OPCIONES_DISENO = [
  { valor: 'EN_TIERRA', etiqueta: 'En tierra' },
  { valor: 'VERTICAL', etiqueta: 'Vertical' },
  { valor: 'COLGANTE', etiqueta: 'Colgante' },
  { valor: 'EN_PISO', etiqueta: 'En piso' },
]

export const OPCIONES_SUBTIPO = [
  { valor: 'SURCOS_WACHU', etiqueta: 'Surcos (wachu)' },
  { valor: 'TERRAZAS_ANTI', etiqueta: 'Terrazas andinas' },
  { valor: 'CAMELLONES_INKA_WACHU', etiqueta: 'Camellones (inka wachu)' },
  { valor: 'VERTICAL_PARED', etiqueta: 'Vertical en pared' },
  { valor: 'COLGANTE_TECHO', etiqueta: 'Colgante en techo' },
  { valor: 'MACETAS_PISO', etiqueta: 'Macetas en piso' },
  { valor: 'LLANTAS_RECICLADAS', etiqueta: 'Llantas recicladas' },
]

export const OPCIONES_GENERO = [
  { valor: 'FEMENINO', etiqueta: 'Femenino' },
  { valor: 'MASCULINO', etiqueta: 'Masculino' },
  { valor: 'OTRO', etiqueta: 'Otro' },
]

export const OPCIONES_PROVINCIA = [
  'Azuay',
  'Bolívar',
  'Cañar',
  'Carchi',
  'Chimborazo',
  'Cotopaxi',
  'El Oro',
  'Esmeraldas',
  'Galápagos',
  'Guayas',
  'Imbabura',
  'Loja',
  'Los Ríos',
  'Manabí',
  'Morona Santiago',
  'Napo',
  'Orellana',
  'Pastaza',
  'Pichincha',
  'Santa Elena',
  'Santo Domingo de los Tsáchilas',
  'Sucumbíos',
  'Tungurahua',
  'Zamora Chinchipe',
].map((provincia) => ({ valor: provincia, etiqueta: provincia }))
