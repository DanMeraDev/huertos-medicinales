import { Boton } from '../../components/Boton'
import { Insignia } from '../../components/Insignia'
import { KintiGuia } from '../../components/KintiGuia'
import { Modal } from '../../components/Modal'
import type { ResultadoAcreditacionMiembroResponse } from './registroLaboresApi'

export interface ModalCelebracionPuntosProps {
  abierto: boolean
  resultado: ResultadoAcreditacionMiembroResponse | null
  alCerrar: () => void
}

export function ModalCelebracionPuntos({
  abierto,
  resultado,
  alCerrar,
}: ModalCelebracionPuntosProps) {
  if (!resultado) return null

  return (
    <Modal
      abierto={abierto}
      titulo="¡Reconocimiento Comunitario!"
      alCancelar={alCerrar}
      textoCancelar="Cerrar"
      acciones={
        <Boton variante="primario" onClick={alCerrar}>
          Continuar
        </Boton>
      }
      className="modal-celebracion"
    >
      <div className="celebracion-cuerpo">
        <div className="celebracion-kinti">
          <KintiGuia variante="celebracion" tamano="lg" />
        </div>

        <h3 className="celebracion-miembro">{resultado.nombreMiembro}</h3>

        <div className="celebracion-puntos-caja">
          <span className="celebracion-puntos-ganados">
            +{resultado.puntosAcreditados} pts
          </span>
          <span className="celebracion-puntos-acumulados">
            Total acumulado: {resultado.puntosTotalesActuales} pts
          </span>
        </div>

        {resultado.subioDeNivel && (
          <div className="celebracion-ascenso">
            <div className="celebracion-ascenso-texto">
              ¡Ascendió al rango {resultado.rangoActual}!
            </div>
            <Insignia texto={resultado.rangoActual} tono="exito" />
          </div>
        )}

        <p className="celebracion-mensaje">{resultado.mensajeCelebracion}</p>

        {resultado.nuevosTalentosDesbloqueados.length > 0 && (
          <div className="celebracion-talentos">
            <h4 className="celebracion-talentos-titulo">
              ¡Nuevos talentos comunitarios desbloqueados!
            </h4>
            <div className="celebracion-talentos-lista">
              {resultado.nuevosTalentosDesbloqueados.map((talento) => (
                <div key={talento.id} className="celebracion-talento-item">
                  <span className="celebracion-talento-icono">🏅</span>
                  <div>
                    <strong>{talento.nombre}</strong>
                    <p>{talento.descripcion}</p>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </Modal>
  )
}
