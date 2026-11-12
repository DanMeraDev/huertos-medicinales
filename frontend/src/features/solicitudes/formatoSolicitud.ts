export function formatearFecha(fechaIso: string) {
  const [anio, mes, dia] = fechaIso.slice(0, 10).split('-')
  return `${dia}/${mes}/${anio}`
}

export function legible(valor: string) {
  const texto = valor.replaceAll('_', ' ').toLowerCase()
  return texto.charAt(0).toUpperCase() + texto.slice(1)
}

export function calcularEdad(fechaNacimiento: string) {
  const nacimiento = new Date(`${fechaNacimiento}T00:00:00`)
  const hoy = new Date()
  const cumplioEsteAnio =
    hoy.getMonth() > nacimiento.getMonth() ||
    (hoy.getMonth() === nacimiento.getMonth() && hoy.getDate() >= nacimiento.getDate())
  return hoy.getFullYear() - nacimiento.getFullYear() - (cumplioEsteAnio ? 0 : 1)
}
