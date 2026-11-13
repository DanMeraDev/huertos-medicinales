import type { Key, ReactNode } from 'react'
import { KintiGuia } from './KintiGuia'
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
    <div className="tabla-contenedor">
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
              {!cargando && <KintiGuia variante="vacio" tamano={80} alt="" aria-hidden="true" />}
              <p>{mensaje}</p>
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
    </div>
  )
}
