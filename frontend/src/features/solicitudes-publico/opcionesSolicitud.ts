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

export const LIMITES_PROVINCIA: Record<string, [[number, number], [number, number]]> = {
  'Azuay': [
    [-3.6284, -79.7637],
    [-2.5544, -78.4236],
  ],
  'Bolívar': [
    [-2.2031, -79.3935],
    [-1.1498, -78.844],
  ],
  'Cañar': [
    [-2.8367, -79.5275],
    [-2.216, -78.5679],
  ],
  'Carchi': [
    [0.3567, -78.5494],
    [1.1979, -77.5255],
  ],
  'Chimborazo': [
    [-2.5673, -79.2544],
    [-1.4301, -78.3585],
  ],
  'Cotopaxi': [
    [-1.22, -79.3393],
    [-0.3308, -78.3809],
  ],
  'El Oro': [
    [-3.8902, -80.4678],
    [-3.0471, -79.3654],
  ],
  'Esmeraldas': [
    [-0.0396, -80.1025],
    [1.4696, -78.4267],
  ],
  'Galápagos': [
    [-1.6122, -92.2072],
    [1.8836, -89.0382],
  ],
  'Guayas': [
    [-3.0643, -80.5633],
    [-0.8367, -79.0981],
  ],
  'Imbabura': [
    [0.1229, -79.2758],
    [0.8766, -77.8104],
  ],
  'Loja': [
    [-4.7489, -80.4847],
    [-3.3296, -79.1007],
  ],
  'Los Ríos': [
    [-2.133, -79.8782],
    [-0.5389, -79.0816],
  ],
  'Manabí': [
    [-1.9515, -81.0847],
    [0.3757, -79.4065],
  ],
  'Morona Santiago': [
    [-3.5877, -78.9535],
    [-1.4483, -76.6928],
  ],
  'Napo': [
    [-1.2487, -78.4273],
    [0.0343, -77.027],
  ],
  'Orellana': [
    [-1.5642, -77.6281],
    [-0.0378, -75.1925],
  ],
  'Pastaza': [
    [-2.609, -78.18],
    [-1.0038, -75.585],
  ],
  'Pichincha': [
    [-0.7211, -79.3709],
    [0.3273, -77.8397],
  ],
  'Santa Elena': [
    [-2.5075, -81.0114],
    [-1.6687, -80.1999],
  ],
  'Santo Domingo de los Tsáchilas': [
    [-0.6987, -79.6162],
    [0.0264, -78.7461],
  ],
  'Sucumbíos': [
    [-0.6562, -77.9795],
    [0.6624, -75.2233],
  ],
  'Tungurahua': [
    [-1.5231, -78.938],
    [-0.9881, -78.1102],
  ],
  'Zamora Chinchipe': [
    [-5.0159, -79.4306],
    [-3.3419, -78.3677],
  ],
}

export const OPCIONES_PROVINCIA = Object.keys(LIMITES_PROVINCIA).map((provincia) => ({
  valor: provincia,
  etiqueta: provincia,
}))
