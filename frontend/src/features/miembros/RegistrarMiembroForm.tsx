import { useState, type FormEvent } from 'react'
import { Boton } from '../../components/Boton'
import { CampoTexto } from '../../components/CampoTexto'
import { KintiGuia } from '../../components/KintiGuia'
import { Notificacion } from '../../components/Notificacion'
import { Selector, type OpcionSelector } from '../../components/Selector'
import {
  registrarMiembroEnComunidad,
  type Genero,
  type NivelMiembro,
} from './miembrosApi'
import { ModalConsentimientoMiembroLopdp } from './ModalConsentimientoMiembroLopdp'
import './RegistrarMiembroForm.css'

export interface RegistrarMiembroFormProps {
  alGuardarExitoso?: () => void
}

const OPCIONES_GENERO: OpcionSelector[] = [
  { valor: 'FEMENINO', etiqueta: 'Femenino' },
  { valor: 'MASCULINO', etiqueta: 'Masculino' },
  { valor: 'OTRO', etiqueta: 'Otro' },
]

const OPCIONES_NIVEL: OpcionSelector[] = [
  { valor: 'APRENDIZ', etiqueta: 'Aprendiz' },
  { valor: 'OFICIAL', etiqueta: 'Oficial' },
  { valor: 'MAESTRO', etiqueta: 'Maestro' },
]

function validarCedulaEcuatoriana(cedula: string): boolean {
  if (!/^[0-9]{10}$/.test(cedula)) {
    return false
  }
  const provincia = parseInt(cedula.substring(0, 2), 10)
  if ((provincia < 1 || provincia > 24) && provincia !== 30) {
    return false
  }
  const tercerDigito = parseInt(cedula.charAt(2), 10)
  if (tercerDigito >= 6) {
    return false
  }
  const coeficientes = [2, 1, 2, 1, 2, 1, 2, 1, 2]
  let suma = 0
  for (let i = 0; i < 9; i++) {
    let valor = parseInt(cedula.charAt(i), 10) * coeficientes[i]
    if (valor >= 10) {
      valor -= 9
    }
    suma += valor
  }
  const digitoVerificador = suma % 10 === 0 ? 0 : 10 - (suma % 10)
  return digitoVerificador === parseInt(cedula.charAt(9), 10)
}

function calcularEdad(fechaNacimiento: string): number {
  if (!fechaNacimiento) return 0
  const hoy = new Date()
  const fecha = new Date(`${fechaNacimiento}T00:00:00`)
  let edad = hoy.getFullYear() - fecha.getFullYear()
  const mes = hoy.getMonth() - fecha.getMonth()
  if (mes < 0 || (mes === 0 && hoy.getDate() < fecha.getDate())) {
    edad--
  }
  return edad
}

export function RegistrarMiembroForm({ alGuardarExitoso }: RegistrarMiembroFormProps) {
  const [cedula, setCedula] = useState('')
  const [nombres, setNombres] = useState('')
  const [apellidos, setApellidos] = useState('')
  const [fechaNacimiento, setFechaNacimiento] = useState('')
  const [genero, setGenero] = useState<Genero>('' as Genero)
  const [telefono, setTelefono] = useState('')
  const [nivel, setNivel] = useState<NivelMiembro>('APRENDIZ')

  const [errores, setErrores] = useState<Record<string, string>>({})
  const [notificacion, setNotificacion] = useState<{ tipo: 'exito' | 'error'; mensaje: string } | null>(null)
  const [modalAbierto, setModalAbierto] = useState(false)
  const [cargando, setCargando] = useState(false)

  function validar(): boolean {
    const nuevosErrores: Record<string, string> = {}

    if (!cedula.trim()) {
      nuevosErrores.cedula = 'La cédula es obligatoria'
    } else if (!validarCedulaEcuatoriana(cedula.trim())) {
      nuevosErrores.cedula = 'La cédula ingresada no es válida'
    }

    if (!nombres.trim()) {
      nuevosErrores.nombres = 'Los nombres son obligatorios'
    } else if (!/^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$/.test(nombres.trim())) {
      nuevosErrores.nombres = 'Los nombres solo admiten letras y espacios'
    } else if (nombres.trim().length > 100) {
      nuevosErrores.nombres = 'Los nombres no pueden superar 100 caracteres'
    }

    if (!apellidos.trim()) {
      nuevosErrores.apellidos = 'Los apellidos son obligatorios'
    } else if (!/^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$/.test(apellidos.trim())) {
      nuevosErrores.apellidos = 'Los apellidos solo admiten letras y espacios'
    } else if (apellidos.trim().length > 100) {
      nuevosErrores.apellidos = 'Los apellidos no pueden superar 100 caracteres'
    }

    if (!fechaNacimiento) {
      nuevosErrores.fechaNacimiento = 'La fecha de nacimiento es obligatoria'
    } else {
      const edad = calcularEdad(fechaNacimiento)
      if (edad < 10) {
        nuevosErrores.fechaNacimiento = 'La persona voluntaria debe tener al menos 10 años de edad cumplidos'
      }
    }

    if (!genero) {
      nuevosErrores.genero = 'El género es obligatorio'
    }

    if (!telefono.trim()) {
      nuevosErrores.telefono = 'El teléfono celular es obligatorio'
    } else if (!/^09[0-9]{8}$/.test(telefono.trim())) {
      nuevosErrores.telefono = 'El teléfono debe iniciar con 09 y tener 10 dígitos'
    }

    setErrores(nuevosErrores)
    return Object.keys(nuevosErrores).length === 0
  }

  function manejarEnvio(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    setNotificacion(null)
    if (validar()) {
      setModalAbierto(true)
    }
  }

  async function confirmarRegistro() {
    setCargando(true)
    try {
      await registrarMiembroEnComunidad({
        cedula: cedula.trim(),
        nombres: nombres.trim(),
        apellidos: apellidos.trim(),
        fechaNacimiento,
        genero,
        telefono: telefono.trim(),
        nivel,
        consentimientoDatos: true,
      })
      setModalAbierto(false)
      setNotificacion({
        tipo: 'exito',
        mensaje: '¡Voluntario/a registrado/a exitosamente en la comunidad!',
      })
      setCedula('')
      setNombres('')
      setApellidos('')
      setFechaNacimiento('')
      setGenero('' as Genero)
      setTelefono('')
      setNivel('APRENDIZ')
      setErrores({})
      alGuardarExitoso?.()
    } catch (error) {
      setModalAbierto(false)
      const mensaje = error instanceof Error ? error.message : 'Error al registrar el voluntario'
      setNotificacion({
        tipo: 'error',
        mensaje,
      })
    } finally {
      setCargando(false)
    }
  }

  return (
    <div className="formulario-miembro-contenedor">
      <div className="formulario-miembro-motivacional">
        <div className="formulario-miembro-kinti">
          <KintiGuia variante="dia" tamano="md" />
        </div>
        <p className="formulario-miembro-mensaje">
          ¡Mejores son dos que uno, que la comunidad siga creciendo! Cada nueva mano que se suma a la siembra fortalece el saber y la salud de nuestro pueblo
        </p>
      </div>

      {notificacion && (
        <Notificacion tipo={notificacion.tipo} mensaje={notificacion.mensaje} />
      )}

      <form className="formulario-miembro-grilla" onSubmit={manejarEnvio} noValidate>
        <CampoTexto
          etiqueta="Cédula de Identidad"
          valor={cedula}
          alCambiar={setCedula}
          error={errores.cedula}
          maxLength={10}
          inputMode="numeric"
          placeholder="Ej: 1710034065"
          required
        />

        <CampoTexto
          etiqueta="Nombres"
          valor={nombres}
          alCambiar={setNombres}
          error={errores.nombres}
          maxLength={100}
          placeholder="Ej: María Tránsito"
          required
        />

        <CampoTexto
          etiqueta="Apellidos"
          valor={apellidos}
          alCambiar={setApellidos}
          error={errores.apellidos}
          maxLength={100}
          placeholder="Ej: Amaguaña Alba"
          required
        />

        <CampoTexto
          etiqueta="Fecha de Nacimiento"
          type="date"
          valor={fechaNacimiento}
          alCambiar={setFechaNacimiento}
          error={errores.fechaNacimiento}
          required
        />

        <Selector
          etiqueta="Género"
          opciones={OPCIONES_GENERO}
          valor={genero}
          alCambiar={(valor) => setGenero(valor as Genero)}
          error={errores.genero}
          required
        />

        <CampoTexto
          etiqueta="Teléfono Celular"
          type="tel"
          valor={telefono}
          alCambiar={setTelefono}
          error={errores.telefono}
          maxLength={10}
          placeholder="Ej: 0981234567"
          required
        />

        <Selector
          etiqueta="Nivel Inicial"
          opciones={OPCIONES_NIVEL}
          valor={nivel}
          alCambiar={(valor) => setNivel(valor as NivelMiembro)}
          error={errores.nivel}
          required
        />

        <div className="formulario-miembro-acciones">
          <Boton
            type="submit"
            variante="primario"
            className="formulario-miembro-boton"
            disabled={cargando}
          >
            Registrar Voluntario
          </Boton>
        </div>
      </form>

      <ModalConsentimientoMiembroLopdp
        abierto={modalAbierto}
        alConfirmar={confirmarRegistro}
        alCancelar={() => setModalAbierto(false)}
        cargando={cargando}
      />
    </div>
  )
}
