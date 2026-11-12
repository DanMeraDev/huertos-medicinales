import { useEffect, useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router'
import { ApiError } from '../../api/client'
import { Boton } from '../../components/Boton'
import { CampoTexto } from '../../components/CampoTexto'
import { Modal } from '../../components/Modal'
import { Notificacion, type NotificacionProps } from '../../components/Notificacion'
import { cambiarClave, type CambiarClaveRequest } from './authApi'
import './CambiarClaveForm.css'

const CLAVES_VACIAS: CambiarClaveRequest = { passwordActual: '', nuevaPassword: '', confirmarPassword: '' }
const ESPERA_REDIRECCION_MS = 1500

export function CambiarClaveForm() {
  const navegar = useNavigate()
  const [claves, setClaves] = useState<CambiarClaveRequest>(CLAVES_VACIAS)
  const [confirmando, setConfirmando] = useState(false)
  const [enviando, setEnviando] = useState(false)
  const [errorServidor, setErrorServidor] = useState<ApiError | null>(null)
  const [aviso, setAviso] = useState<NotificacionProps | null>(null)
  const noCoinciden = claves.confirmarPassword !== '' && claves.nuevaPassword !== claves.confirmarPassword

  useEffect(() => {
    if (aviso?.tipo !== 'exito') return
    const temporizador = setTimeout(() => navegar('/'), ESPERA_REDIRECCION_MS)
    return () => clearTimeout(temporizador)
  }, [aviso, navegar])

  function cambiar(campo: keyof CambiarClaveRequest) {
    return (valor: string) => setClaves((anteriores) => ({ ...anteriores, [campo]: valor }))
  }

  function pedirConfirmacion(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    if (!noCoinciden) setConfirmando(true)
  }

  async function confirmar() {
    setConfirmando(false)
    setEnviando(true)
    setErrorServidor(null)
    setAviso(null)
    try {
      const respuesta = await cambiarClave(claves)
      setAviso({ tipo: 'exito', mensaje: respuesta.mensaje })
    } catch (falla) {
      if (!(falla instanceof ApiError)) {
        throw falla
      }
      setErrorServidor(falla)
      setAviso({ tipo: 'error', mensaje: falla.message })
      setClaves(CLAVES_VACIAS)
      setEnviando(false)
    }
  }

  return (
    <form className="cambiar-clave" onSubmit={pedirConfirmacion}>
      <h1 className="cambiar-clave-titulo">Cambiar contraseña</h1>
      {aviso && <Notificacion tipo={aviso.tipo} mensaje={aviso.mensaje} />}
      <CampoTexto
        etiqueta="Contraseña Actual"
        type="password"
        autoComplete="current-password"
        required
        valor={claves.passwordActual}
        alCambiar={cambiar('passwordActual')}
        error={errorServidor?.errores?.passwordActual}
      />
      <CampoTexto
        etiqueta="Nueva Contraseña"
        type="password"
        autoComplete="new-password"
        required
        valor={claves.nuevaPassword}
        alCambiar={cambiar('nuevaPassword')}
        error={errorServidor?.errores?.nuevaPassword}
      />
      <CampoTexto
        etiqueta="Confirmar Nueva Contraseña"
        type="password"
        autoComplete="new-password"
        required
        valor={claves.confirmarPassword}
        alCambiar={cambiar('confirmarPassword')}
        error={noCoinciden ? 'La nueva contraseña y su confirmación no coinciden' : errorServidor?.errores?.confirmarPassword}
      />
      <Boton type="submit" className="cambiar-clave-boton" disabled={enviando}>
        {enviando ? 'Actualizando…' : 'Actualizar Contraseña'}
      </Boton>
      <Modal
        abierto={confirmando}
        titulo="Confirmar cambio de contraseña"
        alCancelar={() => setConfirmando(false)}
        acciones={<Boton onClick={confirmar}>Sí, cambiar contraseña</Boton>}
      >
        <p>
          ¿Estás seguro de que deseas actualizar tu contraseña de acceso? A partir de tu próximo inicio de sesión
          deberás utilizar tu nueva clave.
        </p>
      </Modal>
    </form>
  )
}
