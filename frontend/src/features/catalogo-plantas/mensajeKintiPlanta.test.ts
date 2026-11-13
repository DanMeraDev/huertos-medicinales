import { mensajeKintiPlanta } from './mensajeKintiPlanta'
import { achicoriaSinDatosDetalle } from './plantasMockHandlers'
import type { PlantaDetalle } from './types'

function plantaCon(datos: Partial<PlantaDetalle>): PlantaDetalle {
  return { ...achicoriaSinDatosDetalle, ...datos }
}

describe('mensajeKintiPlanta', () => {
  it('con altitud, región y ciclo arma la frase completa', () => {
    const planta = plantaCon({ altitudMinMsnm: 1500, altitudMaxMsnm: 3200, regionesDistribucion: 'Sierra', cicloCultivo: 'CORTO' })

    expect(mensajeKintiPlanta(planta)).toBe('Crece entre 1.500 y 3.200 msnm en la Sierra. Es de ciclo corto.')
  })

  it('enumera varias regiones', () => {
    const planta = plantaCon({ regionesDistribucion: 'Costa, Sierra, Amazonía', cicloCultivo: 'LARGO' })

    expect(mensajeKintiPlanta(planta)).toBe('Crece en la Costa, la Sierra y la Amazonía. Es de ciclo largo.')
  })

  it('nombra Galápagos sin artículo', () => {
    const planta = plantaCon({ regionesDistribucion: 'Sierra, Galápagos' })

    expect(mensajeKintiPlanta(planta)).toBe('Crece en la Sierra y Galápagos.')
  })

  it('sin regiones ni ciclo menciona solo la altitud', () => {
    const planta = plantaCon({ altitudMinMsnm: 0, altitudMaxMsnm: 3500 })

    expect(mensajeKintiPlanta(planta)).toBe('Crece entre 0 y 3.500 msnm.')
  })

  it('con solo la altitud mínima dice desde qué altura crece', () => {
    const planta = plantaCon({ altitudMinMsnm: 2000, regionesDistribucion: 'Sierra' })

    expect(mensajeKintiPlanta(planta)).toBe('Crece desde 2.000 msnm en la Sierra.')
  })

  it('con solo la altitud máxima dice hasta qué altura crece', () => {
    const planta = plantaCon({ altitudMaxMsnm: 2800, cicloCultivo: 'CORTO' })

    expect(mensajeKintiPlanta(planta)).toBe('Crece hasta 2.800 msnm. Es de ciclo corto.')
  })

  it('sin altitud menciona la región y el ciclo', () => {
    const planta = plantaCon({ regionesDistribucion: 'Amazonía', cicloCultivo: 'LARGO' })

    expect(mensajeKintiPlanta(planta)).toBe('Crece en la Amazonía. Es de ciclo largo.')
  })

  it('con solo el ciclo menciona el ciclo', () => {
    const planta = plantaCon({ cicloCultivo: 'CORTO' })

    expect(mensajeKintiPlanta(planta)).toBe('Es de ciclo corto.')
  })

  it('sin altitud, regiones ni ciclo no arma ningún mensaje', () => {
    expect(mensajeKintiPlanta(achicoriaSinDatosDetalle)).toBe('')
  })
})
