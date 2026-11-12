export interface PaginadorProps {
  pagina: number
  totalPaginas: number
  tamano: number
  alCambiarPagina: (pagina: number) => void
  alCambiarTamano: (tamano: number) => void
}

export function Paginador(_props: PaginadorProps) {
  return null
}
