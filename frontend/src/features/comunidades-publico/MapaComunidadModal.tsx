import { Boton } from '../../components/Boton'
import { Modal } from '../../components/Modal'
import type { DirectorioComunidadResponse } from './comunidadesPublicoApi'
import './MapaComunidadModal.css'

export interface MapaComunidadModalProps {
  comunidad: DirectorioComunidadResponse | null
  alCerrar: () => void
}

export function MapaComunidadModal({ comunidad, alCerrar }: MapaComunidadModalProps) {
  if (!comunidad) return null

  const tieneCoordenadas = comunidad.latitud !== null && comunidad.longitud !== null
  const delta = 0.005
  const bbox = tieneCoordenadas
    ? `${comunidad.longitud! - delta},${comunidad.latitud! - delta},${comunidad.longitud! + delta},${comunidad.latitud! + delta}`
    : ''
  const osmEmbedUrl = tieneCoordenadas
    ? `https://www.openstreetmap.org/export/embed.html?bbox=${bbox}&layer=mapnik&marker=${comunidad.latitud},${comunidad.longitud}`
    : ''

  return (
    <Modal
      abierto={true}
      titulo={`Ubicación Territorial - ${comunidad.nombre}`}
      alCancelar={alCerrar}
      acciones={
        <div className="mapa-modal-acciones">
          <Boton variante="secundario" onClick={alCerrar}>
            Cerrar
          </Boton>
        </div>
      }
    >
      <div className="mapa-modal-ficha">
        <div className="mapa-modal-ficha-item">
          <span className="mapa-modal-ficha-etiqueta">Provincia:</span>
          <span className="mapa-modal-ficha-valor">{comunidad.provincia}</span>
        </div>
        <div className="mapa-modal-ficha-item">
          <span className="mapa-modal-ficha-etiqueta">Parroquia:</span>
          <span className="mapa-modal-ficha-valor">{comunidad.parroquia}</span>
        </div>
        <div className="mapa-modal-ficha-item">
          <span className="mapa-modal-ficha-etiqueta">Dirección exacta:</span>
          <span className="mapa-modal-ficha-valor">{comunidad.direccion}</span>
        </div>
        <div className="mapa-modal-ficha-item">
          <span className="mapa-modal-ficha-etiqueta">Altitud:</span>
          <span className="mapa-modal-ficha-valor">
            {comunidad.altitudMsnm !== null ? `${comunidad.altitudMsnm} msnm` : 'No registrada'}
          </span>
        </div>
      </div>

      <div className="mapa-modal-visor">
        {tieneCoordenadas ? (
          <iframe
            title={`Mapa de ${comunidad.nombre}`}
            src={osmEmbedUrl}
            loading="lazy"
          />
        ) : (
          <div className="mapa-modal-pendiente">
            Ubicación en mapa en proceso de georreferenciación
          </div>
        )}
      </div>
    </Modal>
  )
}
