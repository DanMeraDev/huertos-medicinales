import { Boton } from './Boton'
import { Selector } from './Selector'
import './Paginador.css'

const TAMANOS = [12, 24, 48].map((tamano) => ({ valor: String(tamano), etiqueta: String(tamano) }))

export interface PaginadorProps {
  pagina: number
  totalPaginas: number
  tamano: number
  alCambiarPagina: (pagina: number) => void
  alCambiarTamano: (tamano: number) => void
}

export function Paginador({ pagina, totalPaginas, tamano, alCambiarPagina, alCambiarTamano }: PaginadorProps) {
  const paginas = Array.from({ length: totalPaginas }, (_, indice) => indice)

  return (
    <nav className="paginador" aria-label="Paginación">
      <div className="paginador-paginas">
        <Boton variante="secundario" disabled={pagina === 0} onClick={() => alCambiarPagina(pagina - 1)}>
          Anterior
        </Boton>
        {paginas.map((indice) => (
          <Boton
            key={indice}
            variante={indice === pagina ? 'primario' : 'secundario'}
            aria-label={`Página ${indice + 1}`}
            aria-current={indice === pagina ? 'page' : undefined}
            onClick={() => alCambiarPagina(indice)}
          >
            {indice + 1}
          </Boton>
        ))}
        <Boton
          variante="secundario"
          disabled={pagina >= totalPaginas - 1}
          onClick={() => alCambiarPagina(pagina + 1)}
        >
          Siguiente
        </Boton>
      </div>
      <Selector
        etiqueta="Por página"
        opciones={TAMANOS}
        valor={String(tamano)}
        alCambiar={(valor) => alCambiarTamano(Number(valor))}
      />
    </nav>
  )
}
