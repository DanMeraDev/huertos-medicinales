import { Boton } from '../../components/Boton'
import { Modal } from '../../components/Modal'
import { calcularEdad, formatearFecha, legible } from './formatoSolicitud'
import type { SolicitudDetalleCompletoResponse } from './solicitudesApi'

export interface DetalleSolicitudModalProps {
  solicitud: SolicitudDetalleCompletoResponse | null
  alCerrar: () => void
  alAprobar: () => void
  alRechazar: () => void
}

function Dato({ etiqueta, valor }: { etiqueta: string; valor: string | number }) {
  return (
    <div className="solicitud-dato">
      <dt>{etiqueta}</dt>
      <dd>{valor}</dd>
    </div>
  )
}

export function DetalleSolicitudModal({ solicitud, alCerrar, alAprobar, alRechazar }: DetalleSolicitudModalProps) {
  if (!solicitud) return null

  return (
    <Modal
      abierto
      titulo={`Solicitud de ${solicitud.nombreComunidad}`}
      alCancelar={alCerrar}
      acciones={
        <>
          <Boton variante="danger" onClick={alRechazar}>
            No aprobar
          </Boton>
          <Boton onClick={alAprobar}>Aprobar</Boton>
        </>
      }
    >
      <div className="solicitud-ficha">
        <section className="solicitud-bloque">
          <h3>Comunidad propuesta</h3>
          <dl>
            <Dato etiqueta="Nombre" valor={solicitud.nombreComunidad} />
            <Dato etiqueta="Tipo de organización" valor={legible(solicitud.tipoOrganizacion)} />
            <Dato etiqueta="Provincia" valor={solicitud.provincia} />
            <Dato etiqueta="Parroquia" valor={solicitud.parroquia} />
            <Dato etiqueta="Dirección" valor={solicitud.direccion} />
            <Dato etiqueta="Coordenadas" valor={`${solicitud.latitud}, ${solicitud.longitud}`} />
            <Dato etiqueta="Altitud" valor={`${solicitud.altitudMsnm} m s. n. m.`} />
            <Dato etiqueta="Diseño principal" valor={legible(solicitud.disenoPrincipal)} />
            <Dato
              etiqueta="Subtipos solicitados"
              valor={solicitud.subtiposSolicitados.split(',').map(legible).join(', ')}
            />
          </dl>
        </section>
        <section className="solicitud-bloque">
          <h3>Solicitante</h3>
          <dl>
            <Dato etiqueta="Cédula" valor={solicitud.cedulaSolicitante} />
            <Dato etiqueta="Nombre" valor={solicitud.nombreSolicitante} />
            <Dato etiqueta="Fecha de nacimiento" valor={formatearFecha(solicitud.fechaNacimientoSolicitante)} />
            <Dato etiqueta="Edad" valor={`${calcularEdad(solicitud.fechaNacimientoSolicitante)} años`} />
            <Dato etiqueta="Correo" valor={solicitud.correoSolicitante} />
            <Dato etiqueta="Celular" valor={solicitud.celularSolicitante} />
            <Dato etiqueta="Género" valor={legible(solicitud.generoSolicitante)} />
          </dl>
        </section>
      </div>
    </Modal>
  )
}
