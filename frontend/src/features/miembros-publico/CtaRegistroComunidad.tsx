import { useNavigate } from 'react-router'
import { Boton } from '../../components/Boton'
import { KintiGuia } from '../../components/KintiGuia'
import './CtaRegistroComunidad.css'

export function CtaRegistroComunidad() {
  const navigate = useNavigate()

  return (
    <aside className="cta-comunidad" aria-label="Participación comunitaria">
      <div className="cta-comunidad-contenido">
        <KintiGuia variante="celebracion" tamano="sm" />
        <p className="cta-comunidad-texto">
          ¿Deseas ser parte de este listado de guardianes o quieres fundar tu propio huerto
          comunitario? ¡Inscríbete y siembra con nosotros!
        </p>
      </div>
      <Boton
        variante="secondary"
        className="cta-comunidad-boton"
        onClick={() => navigate('/solicitar-comunidad')}
      >
        Crear mi comunidad
      </Boton>
    </aside>
  )
}
