import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it } from 'vitest'
import { TablaRankingComunidades } from './TablaRankingComunidades'
import type { RankingComunidad } from './types'

const COMUNIDADES_PRUEBA: RankingComunidad[] = [
  {
    posicion: 1,
    comunidadId: 1,
    nombreComunidad: 'Huerto Comunitario Allpa Kawsay',
    tipoOrganizacion: 'BARRIO',
    provincia: 'Pichincha',
    parroquia: 'Calderón',
    puntosComunidad: 350,
  },
  {
    posicion: 2,
    comunidadId: 2,
    nombreComunidad: 'Chakra Ancestral Sumak Muyu',
    tipoOrganizacion: 'COMITE_LOCAL',
    provincia: 'Imbabura',
    parroquia: 'Otavalo',
    puntosComunidad: 280,
  },
  {
    posicion: 3,
    comunidadId: 3,
    nombreComunidad: 'Huerto Medicinal Yaku Allpa',
    tipoOrganizacion: 'FAMILIA',
    provincia: 'Pichincha',
    parroquia: 'Cumbayá',
    puntosComunidad: 190,
  },
]

describe('TablaRankingComunidades', () => {
  it('renderiza la tabla con los encabezados y los datos de las comunidades', () => {
    render(<TablaRankingComunidades comunidades={COMUNIDADES_PRUEBA} />)

    expect(screen.getByRole('columnheader', { name: 'Posición' })).toBeInTheDocument()
    expect(screen.getByRole('columnheader', { name: 'Nombre de la Comunidad' })).toBeInTheDocument()
    expect(screen.getByRole('columnheader', { name: 'Provincia / Parroquia' })).toBeInTheDocument()
    expect(screen.getByRole('columnheader', { name: 'Tipo de Organización' })).toBeInTheDocument()
    expect(screen.getByRole('columnheader', { name: 'Puntos Acumulados' })).toBeInTheDocument()

    expect(screen.getByText('Huerto Comunitario Allpa Kawsay')).toBeInTheDocument()
    expect(screen.getByText('Chakra Ancestral Sumak Muyu')).toBeInTheDocument()
    expect(screen.getByText('Pichincha / Calderón')).toBeInTheDocument()
    expect(screen.getByText('Imbabura / Otavalo')).toBeInTheDocument()
    expect(screen.getByText('Barrio')).toBeInTheDocument()
    expect(screen.getByText('350')).toBeInTheDocument()
  })

  it('filtra dinámicamente por provincia seleccionada', async () => {
    const usuario = userEvent.setup()
    render(<TablaRankingComunidades comunidades={COMUNIDADES_PRUEBA} />)

    const selectorProvincia = screen.getByLabelText(/provincia/i)
    await usuario.selectOptions(selectorProvincia, 'Imbabura')

    expect(screen.getByText('Chakra Ancestral Sumak Muyu')).toBeInTheDocument()
    expect(screen.queryByText('Huerto Comunitario Allpa Kawsay')).not.toBeInTheDocument()
    expect(screen.queryByText('Huerto Medicinal Yaku Allpa')).not.toBeInTheDocument()
  })

  it('filtra dinámicamente por búsqueda de nombre de comunidad', async () => {
    const usuario = userEvent.setup()
    render(<TablaRankingComunidades comunidades={COMUNIDADES_PRUEBA} />)

    const campoBuscar = screen.getByLabelText(/buscar/i)
    await usuario.type(campoBuscar, 'Yaku')

    expect(screen.getByText('Huerto Medicinal Yaku Allpa')).toBeInTheDocument()
    expect(screen.queryByText('Huerto Comunitario Allpa Kawsay')).not.toBeInTheDocument()
    expect(screen.queryByText('Chakra Ancestral Sumak Muyu')).not.toBeInTheDocument()
  })

  it('muestra mensaje motivacional cuando ninguna comunidad tiene puntos acumulados', () => {
    const comunidadesSinPuntos: RankingComunidad[] = [
      {
        posicion: 1,
        comunidadId: 10,
        nombreComunidad: 'Huerto Nuevo Amanecer',
        tipoOrganizacion: 'BARRIO',
        provincia: 'Guayas',
        parroquia: 'Tarqui',
        puntosComunidad: 0,
      },
      {
        posicion: 2,
        comunidadId: 11,
        nombreComunidad: 'Chakra Verde',
        tipoOrganizacion: 'FAMILIA',
        provincia: 'Azuay',
        parroquia: 'Ricaurte',
        puntosComunidad: 0,
      },
    ]

    render(<TablaRankingComunidades comunidades={comunidadesSinPuntos} />)

    expect(
      screen.getByText('¡Sé parte de la primera comunidad en sumar puntos ancestrales!'),
    ).toBeInTheDocument()
    expect(screen.getByText('Huerto Nuevo Amanecer')).toBeInTheDocument()
  })

  it('muestra estado de carga cuando cargando es true', () => {
    render(<TablaRankingComunidades comunidades={[]} cargando />)

    expect(screen.getByText('Cargando…')).toBeInTheDocument()
  })
})
