import { ETIQUETAS_CICLO, type PlantaDetalle } from './types'

const formatoMetros = new Intl.NumberFormat('es-EC')
const formatoLista = new Intl.ListFormat('es', { type: 'conjunction' })
const REGIONES_CON_ARTICULO: Record<string, string> = {
  Costa: 'la Costa',
  Sierra: 'la Sierra',
  Amazonía: 'la Amazonía',
}

function textoAltitud(minima: number | null, maxima: number | null) {
  if (minima !== null && maxima !== null) {
    return `entre ${formatoMetros.format(minima)} y ${formatoMetros.format(maxima)} msnm`
  }
  if (minima !== null) return `desde ${formatoMetros.format(minima)} msnm`
  if (maxima !== null) return `hasta ${formatoMetros.format(maxima)} msnm`
  return null
}

function textoRegiones(regiones: string | null) {
  const nombres = (regiones ?? '')
    .split(',')
    .map((region) => region.trim())
    .filter(Boolean)
    .map((region) => REGIONES_CON_ARTICULO[region] ?? region)
  return nombres.length > 0 ? `en ${formatoLista.format(nombres)}` : null
}

function oracionCrecimiento(planta: PlantaDetalle) {
  const partes = [
    textoAltitud(planta.altitudMinMsnm, planta.altitudMaxMsnm),
    textoRegiones(planta.regionesDistribucion),
  ].filter((parte) => parte !== null)
  return partes.length > 0 ? `Crece ${partes.join(' ')}.` : null
}

function oracionCiclo(planta: PlantaDetalle) {
  return planta.cicloCultivo ? `Es de ciclo ${ETIQUETAS_CICLO[planta.cicloCultivo].toLowerCase()}.` : null
}

export function mensajeKintiPlanta(planta: PlantaDetalle) {
  return [oracionCrecimiento(planta), oracionCiclo(planta)].filter((oracion) => oracion !== null).join(' ')
}
