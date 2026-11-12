import { useState, type FormEvent } from 'react'
import { ApiError } from '../../api/client'
import { Boton } from '../../components/Boton'
import { CampoTexto } from '../../components/CampoTexto'
import { KintiGuia } from '../../components/KintiGuia'
import { Notificacion, type NotificacionProps } from '../../components/Notificacion'
import { Selector } from '../../components/Selector'
import { crearUsuario, type CrearUsuarioRequest, type Genero, type Rol } from './usuariosApi'
import { esCedulaValida, esContrasenaSegura, esMayorDeEdad } from '../../components/validaciones'
import './CrearUsuarioForm.css'

type Campos = Omit<CrearUsuarioRequest, 'genero' | 'rol'> & { genero: Genero | ''; rol: Rol | '' }

const CAMPOS_VACIOS: Campos = {
  nombreCompleto: '',
  identificacion: '',
  fechaNacimiento: '',
  genero: '',
  correo: '',
  telefono: '',
  password: '',
  rol: '',
}

const OPCIONES_GENERO = [
  { valor: 'FEMENINO', etiqueta: 'Femenino' },
  { valor: 'MASCULINO', etiqueta: 'Masculino' },
  { valor: 'OTRO', etiqueta: 'Otro' },
]

const OPCIONES_ROL = [
  { valor: 'TECNICO_SIS', etiqueta: 'Técnico SIS' },
  { valor: 'TECNICO_MSP', etiqueta: 'Técnico MSP' },
  { valor: 'RESPONSABLE_COMUNIDAD', etiqueta: 'Responsable de comunidad' },
]

function validar(campos: Campos) {
  const errores: Partial<Record<keyof Campos, string>> = {}
  if (campos.identificacion && !esCedulaValida(campos.identificacion)) {
    errores.identificacion = 'La cédula no es válida'
  }
  if (campos.fechaNacimiento && !esMayorDeEdad(campos.fechaNacimiento)) {
    errores.fechaNacimiento = 'La persona debe ser mayor de 18 años'
  }
  if (campos.password && !esContrasenaSegura(campos.password)) {
    errores.password = 'Mínimo 12 caracteres, con al menos una mayúscula, una minúscula y un número'
  }
  return errores
}

export function CrearUsuarioForm() {
  const [campos, setCampos] = useState<Campos>(CAMPOS_VACIOS)
  const [enviando, setEnviando] = useState(false)
  const [errorServidor, setErrorServidor] = useState<ApiError | null>(null)
  const [aviso, setAviso] = useState<NotificacionProps | null>(null)
  const erroresCliente = validar(campos)

  function cambiar(campo: keyof Campos) {
    return (valor: string) => setCampos((anteriores) => ({ ...anteriores, [campo]: valor }))
  }

  function errorDe(campo: keyof Campos) {
    return erroresCliente[campo] ?? errorServidor?.errores?.[campo]
  }

  async function enviar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    const { genero, rol } = campos
    if (Object.keys(erroresCliente).length > 0 || !genero || !rol) {
      return
    }
    setEnviando(true)
    setErrorServidor(null)
    setAviso(null)
    try {
      const creado = await crearUsuario({ ...campos, genero, rol })
      setCampos(CAMPOS_VACIOS)
      setAviso({
        tipo: 'exito',
        mensaje: `Cuenta creada para ${creado.nombreCompleto}. Deberá cambiar la contraseña en su primer acceso`,
      })
    } catch (falla) {
      if (!(falla instanceof ApiError)) {
        throw falla
      }
      setErrorServidor(falla)
      setAviso({ tipo: 'error', mensaje: falla.message })
    } finally {
      setEnviando(false)
    }
  }

  return (
    <form className="crear-usuario" onSubmit={enviar}>
      <header className="crear-usuario-cabecera">
        <KintiGuia variante="dia" className="crear-usuario-kinti" />
        <p className="crear-usuario-frase">
          El trabajo en comunidad florece cuando sumamos voluntades con confianza y compañerismo. Registra a los
          guardianes de nuestros saberes
        </p>
      </header>
      {aviso && <Notificacion tipo={aviso.tipo} mensaje={aviso.mensaje} />}
      <div className="crear-usuario-campos">
        <div className="crear-usuario-ancho">
          <CampoTexto
            etiqueta="Nombre completo"
            required
            maxLength={120}
            pattern="[A-Za-zÁÉÍÓÚáéíóúÑñÜü ]+"
            valor={campos.nombreCompleto}
            alCambiar={cambiar('nombreCompleto')}
            error={errorDe('nombreCompleto')}
          />
        </div>
        <CampoTexto
          etiqueta="Cédula de identidad"
          inputMode="numeric"
          required
          maxLength={10}
          valor={campos.identificacion}
          alCambiar={cambiar('identificacion')}
          error={errorDe('identificacion')}
        />
        <CampoTexto
          etiqueta="Fecha de nacimiento"
          type="date"
          required
          valor={campos.fechaNacimiento}
          alCambiar={cambiar('fechaNacimiento')}
          error={errorDe('fechaNacimiento')}
        />
        <Selector
          etiqueta="Género"
          required
          opciones={OPCIONES_GENERO}
          valor={campos.genero}
          alCambiar={cambiar('genero')}
          error={errorDe('genero')}
        />
        <CampoTexto
          etiqueta="Correo electrónico"
          type="email"
          required
          maxLength={180}
          valor={campos.correo}
          alCambiar={cambiar('correo')}
          error={errorDe('correo')}
        />
        <CampoTexto
          etiqueta="Teléfono celular"
          type="tel"
          required
          pattern="09[0-9]{8}"
          maxLength={10}
          valor={campos.telefono}
          alCambiar={cambiar('telefono')}
          error={errorDe('telefono')}
        />
        <Selector
          etiqueta="Rol del sistema"
          required
          opciones={OPCIONES_ROL}
          valor={campos.rol}
          alCambiar={cambiar('rol')}
          error={errorDe('rol')}
        />
        <div className="crear-usuario-ancho">
          <CampoTexto
            etiqueta="Contraseña temporal"
            type="password"
            autoComplete="new-password"
            required
            valor={campos.password}
            alCambiar={cambiar('password')}
            error={errorDe('password')}
          />
        </div>
        <div className="crear-usuario-acciones">
          <Boton type="submit" disabled={enviando}>
            {enviando ? 'Creando…' : 'Crear usuario'}
          </Boton>
        </div>
      </div>
    </form>
  )
}
