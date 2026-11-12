import { urlFoto } from './fotos'

describe('urlFoto', () => {
  it('arma la ruta pública de la foto a partir de la ruta relativa', () => {
    expect(urlFoto('plantas/manzanilla.jpg')).toBe('/api/publico/fotos/plantas/manzanilla.jpg')
  })

  it('devuelve null cuando no hay foto', () => {
    expect(urlFoto(null)).toBeNull()
  })
})
