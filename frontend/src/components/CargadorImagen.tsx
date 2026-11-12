export interface CargadorImagenProps {
  etiqueta: string
  archivo: File | null
  alCambiar: (archivo: File | null) => void
  urlActual?: string | null
}

export function CargadorImagen(_props: CargadorImagenProps) {
  return null
}
