import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router'
import { describe, expect, it } from 'vitest'
import { InicioPage } from './InicioPage'

describe('InicioPage', () => {
  it('muestra el Hero de Hampi Muyu con los objetivos del sistema', () => {
    render(
      <MemoryRouter>
        <InicioPage />
      </MemoryRouter>,
    )

    expect(screen.getByRole('heading', { level: 1, name: 'Hampi Muyu' })).toBeInTheDocument()
    expect(screen.getByText(/atienda enfermedades leves/i)).toBeInTheDocument()
    expect(screen.getByText(/entregamos gratis plantas medicinales/i)).toBeInTheDocument()
  })

  it('presenta los tres niveles de crecimiento con Kinti', () => {
    render(
      <MemoryRouter>
        <InicioPage />
      </MemoryRouter>,
    )

    expect(screen.getByRole('heading', { name: 'APRENDIZ' })).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: 'OFICIAL' })).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: 'MAESTRO' })).toBeInTheDocument()
    expect(screen.getByAltText('Kinti en nivel APRENDIZ')).toBeInTheDocument()
    expect(screen.getByAltText('Kinti en nivel OFICIAL')).toBeInTheDocument()
    expect(screen.getByAltText('Kinti en nivel MAESTRO')).toBeInTheDocument()
  })

  it('incluye la sección de Kinti Lunar y la llamada final a solicitar comunidad', () => {
    render(
      <MemoryRouter>
        <InicioPage />
      </MemoryRouter>,
    )

    expect(screen.getByAltText('Kinti Lunar guiando el huerto')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Consultar calendario lunar' })).toHaveAttribute(
      'href',
      '/calendario-lunar',
    )

    expect(screen.getByAltText('Kinti llamando a crear una nueva comunidad')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Solicitar creación de comunidad' })).toHaveAttribute(
      'href',
      '/solicitar-comunidad',
    )
  })
})
