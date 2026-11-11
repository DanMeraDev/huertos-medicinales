import type { ComponentProps } from 'react'

export type KintiVariante =
  | 'lunar'
  | 'celebracion'
  | 'vacio'
  | 'sabiduria'
  | 'alerta'
  | 'dia'

export type KintiTamanoPreset = 'sm' | 'md' | 'lg' | 'xl'

export interface KintiGuiaProps extends Omit<ComponentProps<'img'>, 'src'> {
  variante?: KintiVariante
  tamano?: KintiTamanoPreset | number | string
  width?: number | string
  height?: number | string
  className?: string
  alt?: string
}

interface ConfiguracionVariante {
  archivo: string
  altPorDefecto: string
}

const PRESETS_TAMANO: Record<KintiTamanoPreset, number> = {
  sm: 48,
  md: 96,
  lg: 160,
  xl: 240,
}

/**
 * Evalúa la variante solicitada y retorna la configuración del archivo SVG y su texto alternativo.
 * Garantiza un fallback seguro ante variantes desconocidas o nulas.
 */
function obtenerConfiguracionVariante(variante?: KintiVariante | string): ConfiguracionVariante {
  switch (variante) {
    case 'lunar':
      return {
        archivo: 'kinti-lunar.svg',
        altPorDefecto: 'Kinti Guía en fase lunar',
      }
    case 'celebracion':
      return {
        archivo: 'kinti-celebracion.svg',
        altPorDefecto: 'Kinti Guía celebrando logro',
      }
    case 'vacio':
      return {
        archivo: 'kinti-vacio.svg',
        altPorDefecto: 'Kinti Guía en estado vacío',
      }
    case 'alerta':
      return {
        archivo: 'kinti-alerta.svg',
        altPorDefecto: 'Kinti Guía en estado de alerta',
      }
    case 'sabiduria':
    case 'dia':
      return {
        archivo: 'kinti-sabiduria.svg',
        altPorDefecto: 'Kinti Guía de sabiduría ancestral',
      }
    default:
      // Fallback seguro: siempre retorna el SVG base de sabiduría para nunca romper la UI
      return {
        archivo: 'kinti-sabiduria.svg',
        altPorDefecto: 'Kinti Guía',
      }
  }
}

function resolverDimension(
  tamano?: KintiTamanoPreset | number | string,
  dimensionExplicita?: number | string,
): number | string | undefined {
  if (dimensionExplicita !== undefined) {
    return dimensionExplicita
  }
  if (tamano !== undefined) {
    if (typeof tamano === 'string' && tamano in PRESETS_TAMANO) {
      return PRESETS_TAMANO[tamano as KintiTamanoPreset]
    }
    return tamano
  }
  return undefined
}

export function KintiGuia({
  variante = 'sabiduria',
  tamano,
  width,
  height,
  className,
  alt,
  ...props
}: KintiGuiaProps) {
  const { archivo, altPorDefecto } = obtenerConfiguracionVariante(variante)
  const base = (import.meta.env.BASE_URL ?? '/').replace(/\/$/, '')
  const src = `${base}/kinti/${archivo}`

  const anchoFinal = resolverDimension(tamano, width)
  const altoFinal = resolverDimension(tamano, height)
  const clasesFinales = className ? `kinti-guia ${className}` : 'kinti-guia'

  return (
    <img
      src={src}
      alt={alt ?? altPorDefecto}
      width={anchoFinal}
      height={altoFinal}
      className={clasesFinales}
      {...props}
    />
  )
}

