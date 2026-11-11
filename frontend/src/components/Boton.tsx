import type { ComponentProps } from 'react'
import './Boton.css'

export type VarianteBoton = 'primario' | 'secundario' | 'peligro' | 'primary' | 'secondary' | 'danger'

export interface BotonProps extends ComponentProps<'button'> {
  variante?: VarianteBoton
}

const MAPA_VARIANTES: Record<VarianteBoton, string> = {
  primario: 'primario',
  primary: 'primario',
  secundario: 'secundario',
  secondary: 'secundario',
  peligro: 'peligro',
  danger: 'peligro',
}

export function Boton({ variante = 'primario', type = 'button', ...props }: BotonProps) {
  const varianteNormalizada = MAPA_VARIANTES[variante] ?? 'primario'
  return <button type={type} className={`boton boton-${varianteNormalizada}`} {...props} />
}
