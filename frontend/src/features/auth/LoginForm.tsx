import { useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router'
import { ApiError } from '../../api/client'
import { guardarSesion } from '../../api/sesion'
import { Boton } from '../../components/Boton'
import { CampoTexto } from '../../components/CampoTexto'
import { KintiGuia } from '../../components/KintiGuia'
import { Notificacion } from '../../components/Notificacion'
import { iniciarSesion, type LoginResponse, type Rol } from './authApi'
import './LoginForm.css'

const PANEL_POR_ROL: Record<Rol, string> = {
  TECNICO_SIS: '/usuarios/nuevo',
  TECNICO_MSP: '/solicitudes',
  RESPONSABLE_COMUNIDAD: '/mi-comunidad',
}

function rutaDespuesDelIngreso(sesion: LoginResponse) {
  return sesion.requiereCambioClave ? '/cambiar-clave' : PANEL_POR_ROL[sesion.rol]
}

export function LoginForm() {
  const navegar = useNavigate()
  const [correo, setCorreo] = useState('')
  const [password, setPassword] = useState('')
  const [enviando, setEnviando] = useState(false)
  const [error, setError] = useState<ApiError | null>(null)

  async function enviar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    setEnviando(true)
    setError(null)
    try {
      const sesion = await iniciarSesion({ correo, password })
      guardarSesion({
        token: sesion.token,
        rol: sesion.rol,
        nombreCompleto: sesion.nombreCompleto,
      })
      navegar(rutaDespuesDelIngreso(sesion))
    } catch (falla) {
      if (!(falla instanceof ApiError)) {
        throw falla
      }
      setError(falla)
      setPassword('')
      setEnviando(false)
    }
  }

  return (
    <form className="login" onSubmit={enviar} noValidate>
      <header className="login-cabecera">
        <KintiGuia variante="celebracion" className="login-kinti" />
        <p className="login-saludo">¡Alli shamushka! Qué alegría verte de nuevo por nuestros huertos medicinales</p>
      </header>
      {error && <Notificacion tipo="error" mensaje={error.message} />}
      <CampoTexto
        etiqueta="Correo electrónico"
        type="email"
        autoComplete="email"
        required
        valor={correo}
        alCambiar={setCorreo}
        error={error?.errores?.correo}
      />
      <CampoTexto
        etiqueta="Contraseña"
        type="password"
        autoComplete="current-password"
        required
        valor={password}
        alCambiar={setPassword}
        error={error?.errores?.password}
      />
      <Boton type="submit" className="login-boton" disabled={enviando}>
        {enviando ? 'Ingresando…' : 'Iniciar sesión'}
      </Boton>
    </form>
  )
}
