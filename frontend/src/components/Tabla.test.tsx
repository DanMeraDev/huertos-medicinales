import { render, screen } from '@testing-library/react'
import { Tabla, type ColumnaTabla } from './Tabla'

interface Persona {
  id: number
  nombre: string
  nivel: string
}

const columnas: ColumnaTabla<Persona>[] = [
  { titulo: 'Nombre', celda: (persona) => persona.nombre },
  { titulo: 'Nivel', celda: (persona) => persona.nivel },
]

const filas: Persona[] = [
  { id: 1, nombre: 'Ana', nivel: 'APRENDIZ' },
  { id: 2, nombre: 'Luis', nivel: 'EXPERTO' },
]

describe('Tabla', () => {
  it('muestra los títulos de las columnas y los datos de cada fila', () => {
    render(<Tabla columnas={columnas} filas={filas} claveFila={(persona) => persona.id} />)

    expect(screen.getByRole('columnheader', { name: 'Nombre' })).toBeInTheDocument()
    expect(screen.getByRole('columnheader', { name: 'Nivel' })).toBeInTheDocument()
    expect(screen.getByRole('cell', { name: 'Ana' })).toBeInTheDocument()
    expect(screen.getByRole('cell', { name: 'EXPERTO' })).toBeInTheDocument()
  })

  it('muestra un aviso cuando no hay filas', () => {
    render(<Tabla columnas={columnas} filas={[]} claveFila={(persona) => persona.id} />)

    expect(screen.getByText('No hay registros.')).toBeInTheDocument()
  })

  it('muestra la carga en lugar de las filas mientras carga', () => {
    render(<Tabla columnas={columnas} filas={filas} claveFila={(persona) => persona.id} cargando />)

    expect(screen.getByText('Cargando…')).toBeInTheDocument()
    expect(screen.queryByRole('cell', { name: 'Ana' })).not.toBeInTheDocument()
  })
})
