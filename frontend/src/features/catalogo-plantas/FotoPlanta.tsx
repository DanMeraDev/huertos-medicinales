import { useState } from 'react'
import { urlFoto } from '../../api/fotos'

export interface FotoPlantaProps {
  ruta: string | null
  nombreComun: string
}

export function FotoPlanta({ ruta, nombreComun }: FotoPlantaProps) {
  const [fallo, setFallo] = useState(false)
  const url = urlFoto(ruta)

  if (!url || fallo) {
    return (
      <div className="planta-foto planta-foto-marcador" role="img" aria-label={`Sin fotografía de ${nombreComun}`}>
        <span aria-hidden="true">{nombreComun.charAt(0)}</span>
      </div>
    )
  }
  return (
    <img
      className="planta-foto"
      src={url}
      alt={`Fotografía de ${nombreComun}`}
      loading="lazy"
      onError={() => setFallo(true)}
    />
  )
}
