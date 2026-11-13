import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import { KintiGuia, type KintiVariante } from './KintiGuia'

describe('KintiGuia', () => {
  it('renderiza la variante base de sabiduría por defecto', () => {
    render(<KintiGuia />)

    const img = screen.getByRole('img')
    expect(img).toBeInTheDocument()
    expect(img).toHaveAttribute('src', '/kinti/kinti-sabiduria.svg')
    expect(img).toHaveAttribute('alt', 'Kinti Guía de sabiduría ancestral')
    expect(img).toHaveClass('kinti-guia')
  })

  it('renderiza la variante lunar con su respectivo SVG', () => {
    render(<KintiGuia variante="lunar" />)

    const img = screen.getByRole('img')
    expect(img).toHaveAttribute('src', '/kinti/kinti-lunar.svg')
    expect(img).toHaveAttribute('alt', 'Kinti Guía en fase lunar')
  })

  it('renderiza la variante de celebración con kinti-celebracion.svg', () => {
    render(<KintiGuia variante="celebracion" />)

    const img = screen.getByRole('img')
    expect(img).toHaveAttribute('src', '/kinti/kinti-celebracion.svg')
    expect(img).toHaveAttribute('alt', 'Kinti Guía celebrando logro')
  })

  it('renderiza la variante dorado con kinti-dorado.svg', () => {
    render(<KintiGuia variante="dorado" />)

    const img = screen.getByRole('img')
    expect(img).toHaveAttribute('src', '/kinti/kinti-dorado.svg')
    expect(img).toHaveAttribute('alt', 'Kinti Guía Maestro dorado')
  })

  it('renderiza la variante vacío con su respectivo SVG', () => {
    render(<KintiGuia variante="vacio" />)

    const img = screen.getByRole('img')
    expect(img).toHaveAttribute('src', '/kinti/kinti-vacio.svg')
    expect(img).toHaveAttribute('alt', 'Kinti Guía en estado vacío')
  })

  it('renderiza la variante alerta con su respectivo SVG', () => {
    render(<KintiGuia variante="alerta" />)

    const img = screen.getByRole('img')
    expect(img).toHaveAttribute('src', '/kinti/kinti-alerta.svg')
    expect(img).toHaveAttribute('alt', 'Kinti Guía en estado de alerta')
  })

  it('soporta el alias "dia" mapeándolo al SVG de sabiduría', () => {
    render(<KintiGuia variante="dia" />)

    const img = screen.getByRole('img')
    expect(img).toHaveAttribute('src', '/kinti/kinti-sabiduria.svg')
  })

  it('aplica el fallback seguro al SVG base ante una variante no reconocida', () => {
    // Simulamos un valor no tipado o recibido dinámicamente
    render(<KintiGuia variante={'desconocida' as unknown as KintiVariante} />)

    const img = screen.getByRole('img')
    expect(img).toBeInTheDocument()
    expect(img).toHaveAttribute('src', '/kinti/kinti-sabiduria.svg')
  })

  it('permite sobrescribir el texto alternativo con la prop alt', () => {
    render(<KintiGuia variante="lunar" alt="Mascota Kinti durmiendo" />)

    const img = screen.getByRole('img', { name: 'Mascota Kinti durmiendo' })
    expect(img).toBeInTheDocument()
  })

  it('aplica presets de tamaño correctamente', () => {
    const { rerender } = render(<KintiGuia tamano="sm" />)
    let img = screen.getByRole('img')
    expect(img).toHaveAttribute('width', '48')
    expect(img).toHaveAttribute('height', '48')
    expect(img).toHaveClass('kinti-guia--sm')

    rerender(<KintiGuia tamano="lg" />)
    img = screen.getByRole('img')
    expect(img).toHaveAttribute('width', '160')
    expect(img).toHaveAttribute('height', '160')
    expect(img).toHaveClass('kinti-guia--lg')
  })

  it('prioriza width y height explícitos sobre el preset de tamaño', () => {
    render(<KintiGuia tamano="md" width={200} height={100} />)

    const img = screen.getByRole('img')
    expect(img).toHaveAttribute('width', '200')
    expect(img).toHaveAttribute('height', '100')
  })

  it('concatena clases CSS personalizadas manteniendo la clase base', () => {
    render(<KintiGuia className="mi-clase-extra animacion-flotante" />)

    const img = screen.getByRole('img')
    expect(img).toHaveClass('kinti-guia', 'mi-clase-extra', 'animacion-flotante')
  })
})

