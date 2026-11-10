import type { ComponentProps } from 'react'
import './Boton.css'

export interface BotonProps extends ComponentProps<'button'> {
  variante?: 'primario' | 'secundario' | 'peligro'
}

export function Boton({ variante = 'primario', type = 'button', ...props }: BotonProps) {
  return <button type={type} className={`boton boton-${variante}`} {...props} />
}
