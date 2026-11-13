import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import { useEffect, useRef } from 'react'
import './MapaUbicacion.css'

interface MapaUbicacionProps {
  latitud: string
  longitud: string
  limites?: [[number, number], [number, number]]
  alSeleccionar: (latitud: number, longitud: number) => void
}

const CENTRO_ECUADOR: L.LatLngTuple = [-1.5, -78.5]
const ZOOM_PAIS = 6
const ZOOM_MINIMO = 5
const LIMITES_ECUADOR: L.LatLngBoundsLiteral = [
  [-5.2, -92.4],
  [2, -75],
]
const ZOOM_UBICACION = 16

function aCoordenada(valor: string) {
  return valor.trim() !== '' && Number.isFinite(Number(valor)) ? Number(valor) : null
}

const ICONO_MARCADOR = L.divIcon({ className: 'mapa-ubicacion-marcador', iconSize: [22, 22] })

export function MapaUbicacion({ latitud, longitud, limites, alSeleccionar }: MapaUbicacionProps) {
  const contenedorRef = useRef<HTMLDivElement>(null)
  const mapaRef = useRef<L.Map | null>(null)
  const marcadorRef = useRef<L.Marker | null>(null)
  const alSeleccionarRef = useRef(alSeleccionar)

  useEffect(() => {
    alSeleccionarRef.current = alSeleccionar
  }, [alSeleccionar])

  useEffect(() => {
    if (!contenedorRef.current) {
      return
    }
    const mapa = L.map(contenedorRef.current, {
      center: CENTRO_ECUADOR,
      zoom: ZOOM_PAIS,
      minZoom: ZOOM_MINIMO,
      maxBounds: LIMITES_ECUADOR,
      maxBoundsViscosity: 1,
    })
    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '&copy; OpenStreetMap',
    }).addTo(mapa)
    mapa.on('click', ({ latlng }) => alSeleccionarRef.current(latlng.lat, latlng.lng))
    mapaRef.current = mapa
    return () => {
      mapa.remove()
      mapaRef.current = null
      marcadorRef.current = null
    }
  }, [])

  useEffect(() => {
    if (limites) {
      mapaRef.current?.fitBounds(limites)
    }
  }, [limites])

  const latitudMarcada = aCoordenada(latitud)
  const longitudMarcada = aCoordenada(longitud)

  useEffect(() => {
    const mapa = mapaRef.current
    if (!mapa || latitudMarcada === null || longitudMarcada === null) {
      return
    }
    const punto: L.LatLngTuple = [latitudMarcada, longitudMarcada]
    if (marcadorRef.current) {
      marcadorRef.current.setLatLng(punto)
    } else {
      marcadorRef.current = L.marker(punto, { icon: ICONO_MARCADOR, keyboard: false }).addTo(mapa)
    }
    mapa.setView(punto, Math.max(mapa.getZoom(), ZOOM_UBICACION))
  }, [latitudMarcada, longitudMarcada])

  return (
    <div
      ref={contenedorRef}
      className="mapa-ubicacion"
      role="application"
      aria-label="Mapa para elegir la ubicación"
    />
  )
}
