export function leerIdPlanta(parametro: string | undefined) {
  if (parametro === undefined || !/^[1-9]\d*$/.test(parametro)) return null
  const id = Number(parametro)
  return Number.isSafeInteger(id) ? id : null
}
