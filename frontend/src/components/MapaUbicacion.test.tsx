import { fireEvent, render, screen } from '@testing-library/react'
import { vi } from 'vitest'
import { MapaUbicacion } from './MapaUbicacion'

describe('MapaUbicacion', () => {
  it('entrega las coordenadas del punto que se pulsa en el mapa', () => {
    const alSeleccionar = vi.fn()
    render(<MapaUbicacion latitud="" longitud="" alSeleccionar={alSeleccionar} />)

    fireEvent.click(screen.getByRole('application', { name: 'Mapa para elegir la ubicación' }), {
      clientX: 0,
      clientY: 0,
    })

    expect(alSeleccionar).toHaveBeenCalledTimes(1)
    const [latitud, longitud] = alSeleccionar.mock.calls[0]
    expect(latitud).toBeCloseTo(-1.5, 1)
    expect(longitud).toBeCloseTo(-78.5, 1)
  })

  it('marca en el mapa la ubicación recibida', () => {
    const { container } = render(
      <MapaUbicacion latitud="-0.0987123" longitud="-78.4321456" alSeleccionar={vi.fn()} />,
    )

    expect(container.querySelector('.mapa-ubicacion-marcador')).toBeInTheDocument()
  })
})
