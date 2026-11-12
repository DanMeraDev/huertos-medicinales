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

export function Boton({ variante = 'primario', type = 'button', className, ...props }: BotonProps) {
  const varianteNormalizada = MAPA_VARIANTES[variante] ?? 'primario'
  const clases = className ? `boton boton-${varianteNormalizada} ${className}` : `boton boton-${varianteNormalizada}`
  return <button type={type} className={clases} {...props} />
}
