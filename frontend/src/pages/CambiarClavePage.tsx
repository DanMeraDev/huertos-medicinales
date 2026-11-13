import { CambiarClaveForm } from '../features/auth/CambiarClaveForm'
import { ConsejoKinti } from '../components/ConsejoKinti'
import './LoginPage.css'

export function CambiarClavePage() {
  return (
    <section className="login-pagina">
      <ConsejoKinti
        variante="alerta"
        mensaje="Elige una contraseña que solo tú conozcas. Al cambiarla, vuelve a iniciar sesión."
      />
      <CambiarClaveForm />
    </section>
  )
}
