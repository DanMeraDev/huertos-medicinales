export interface Ubicacion {
  codigoPais: string | null
  provincia: string | null
  parroquia: string | null
}

interface RespuestaGeocodificacion {
  address?: {
    country_code?: string
    state?: string
    city_district?: string
    municipality?: string
    town?: string
    village?: string
    city?: string
  }
}

const URL_GEOCODIFICACION_INVERSA = 'https://nominatim.openstreetmap.org/reverse'

export const AVISO_FUERA_DE_ECUADOR = 'Ese punto está fuera de Ecuador. Elige un lugar dentro del país.'

export async function buscarUbicacion(latitud: number, longitud: number): Promise<Ubicacion> {
  const parametros = new URLSearchParams({
    format: 'jsonv2',
    zoom: '14',
    'accept-language': 'es',
    lat: String(latitud),
    lon: String(longitud),
  })
  const respuesta = await fetch(`${URL_GEOCODIFICACION_INVERSA}?${parametros}`)
  if (!respuesta.ok) {
    throw new Error('No se pudo verificar el punto elegido')
  }
  const { address = {} } = (await respuesta.json()) as RespuestaGeocodificacion
  return {
    codigoPais: address.country_code ?? null,
    provincia: address.state ?? null,
    parroquia:
      address.city_district ?? address.municipality ?? address.town ?? address.village ?? address.city ?? null,
  }
}
