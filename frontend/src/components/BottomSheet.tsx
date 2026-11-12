import type { ReactNode } from 'react'

export interface BottomSheetProps {
  abierto: boolean
  titulo: string
  alCerrar: () => void
  children: ReactNode
  pie?: ReactNode
}

export function BottomSheet(_props: BottomSheetProps) {
  return null
}
