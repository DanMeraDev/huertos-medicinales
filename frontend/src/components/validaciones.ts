const COEFICIENTES_CEDULA = [2, 1, 2, 1, 2, 1, 2, 1, 2]
const PROVINCIAS = 24
const EDAD_MINIMA = 18

/**
 * Cédula ecuatoriana: dos dígitos de provincia (01 a 24), tercer dígito menor a 6
 * y el último dígito verificador según el módulo 10.
 */
export function esCedulaValida(cedula: string) {
  if (!/^[0-9]{10}$/.test(cedula)) {
    return false
  }
  const digitos = [...cedula].map(Number)
  const provincia = Number(cedula.slice(0, 2))
  if (provincia < 1 || provincia > PROVINCIAS || digitos[2] >= 6) {
    return false
  }
  const suma = COEFICIENTES_CEDULA.reduce((total, coeficiente, indice) => {
    const producto = digitos[indice] * coeficiente
    return total + (producto > 9 ? producto - 9 : producto)
  }, 0)
  return (10 - (suma % 10)) % 10 === digitos[9]
}

export function esMayorDeEdad(fechaNacimiento: string, hoy = new Date()) {
  const [anio, mes, dia] = fechaNacimiento.split('-').map(Number)
  const cumpleEsteAnio = hoy.getMonth() + 1 > mes || (hoy.getMonth() + 1 === mes && hoy.getDate() >= dia)
  const edad = hoy.getFullYear() - anio - (cumpleEsteAnio ? 0 : 1)
  return edad >= EDAD_MINIMA
}

export function esContrasenaSegura(password: string) {
  return /^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9]).{12,}$/.test(password)
}
