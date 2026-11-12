import type { ComponentProps } from 'react'
import './Tarjeta.css'

export type TarjetaProps = ComponentProps<'article'>

export function Tarjeta({ className, ...props }: TarjetaProps) {
  return <article className={className ? `tarjeta ${className}` : 'tarjeta'} {...props} />
}
