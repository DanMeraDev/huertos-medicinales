import type { Key, ReactNode } from 'react'
import './Tabla.css'

export interface ColumnaTabla<T> {
  titulo: string
  celda: (fila: T) => ReactNode
}

export interface TablaProps<T> {
  columnas: ColumnaTabla<T>[]
  filas: T[]
  claveFila: (fila: T) => Key
  cargando?: boolean
}

export function Tabla<T>({ columnas, filas, claveFila, cargando = false }: TablaProps<T>) {
  const mensaje = cargando ? 'Cargando…' : filas.length === 0 ? 'No hay registros.' : null

  return (
    <table className="tabla">
      <thead>
      <tr>
        {columnas.map((columna) => (
          <th key={columna.titulo} scope="col">
            {columna.titulo}
          </th>
        ))}
      </tr>
      </thead>
      <tbody>
      {mensaje ? (
        <tr>
          <td colSpan={columnas.length} className="tabla-mensaje">
            {mensaje}
          </td>
        </tr>
      ) : (
        filas.map((fila) => (
          <tr key={claveFila(fila)}>
            {columnas.map((columna) => (
              <td key={columna.titulo}>{columna.celda(fila)}</td>
            ))}
          </tr>
        ))
      )}
      </tbody>
    </table>
  )
}
