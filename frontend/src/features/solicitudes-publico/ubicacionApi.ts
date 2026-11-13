export interface DivisionPolitica {
  provincia: string | null
  parroquia: string | null
}

interface RespuestaGeocodificacion {
  address?: {
    state?: string
    city_district?: string
    municipality?: string
    town?: string
    village?: string
    city?: string
  }
}

const URL_GEOCODIFICACION_INVERSA = 'https://nominatim.openstreetmap.org/reverse'

export async function buscarDivisionPolitica(latitud: number, longitud: number): Promise<DivisionPolitica> {
  const parametros = new URLSearchParams({
    format: 'jsonv2',
    zoom: '14',
    'accept-language': 'es',
    lat: String(latitud),
    lon: String(longitud),
  })
  const respuesta = await fetch(`${URL_GEOCODIFICACION_INVERSA}?${parametros}`)
  if (!respuesta.ok) {
    throw new Error('No se pudo identificar la provincia y la parroquia')
  }
  const { address = {} } = (await respuesta.json()) as RespuestaGeocodificacion
  return {
    provincia: address.state ?? null,
    parroquia:
      address.city_district ?? address.municipality ?? address.town ?? address.village ?? address.city ?? null,
  }
}
