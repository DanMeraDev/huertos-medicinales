import { useEffect, useId, useState } from 'react'
import { ApiError } from '../../api/client'
import { Boton } from '../../components/Boton'
import { Insignia } from '../../components/Insignia'
import { KintiGuia } from '../../components/KintiGuia'
import { Notificacion } from '../../components/Notificacion'
import {
  obtenerFichaMiembro,
  type FichaInternaMiembroResponse,
  type NivelMiembro,
} from './miembrosApi'
import './FichaMiembroModal.css'

export interface FichaMiembroModalProps {
  abierto: boolean
  miembroId?: number | null
  ficha?: FichaInternaMiembroResponse | null
  alCerrar: () => void
}

function formatearFecha(fechaIso?: string | null): string {
  if (!fechaIso) return '-'
  const [anio, mes, dia] = fechaIso.slice(0, 10).split('-')
  return `${dia}/${mes}/${anio}`
}

function obtenerMensajeKinti(nivel: NivelMiembro): {
  variante: 'aprendiz' | 'oficial' | 'maestro'
  mensaje: string
} {
  switch (nivel) {
    case 'MAESTRO':
      return {
        variante: 'maestro',
        mensaje:
          '¡Un verdadero guardián de los saberes ancestrales! Su conocimiento y entrega son medicina viva para toda la comunidad',
      }
    case 'OFICIAL':
      return {
        variante: 'oficial',
        mensaje:
          '¡Ha demostrado constancia en las labores del huerto! Su compromiso florece y ya guía a los más jóvenes con su ejemplo y dedicación',
      }
    case 'APRENDIZ':
    default:
      return {
        variante: 'aprendiz',
        mensaje:
          '¡Recién inicia su siembra en el camino de las plantas medicinales! Acompañemos sus primeros pasos con paciencia y sabiduría ancestral',
      }
  }
}

export function FichaMiembroModal({
  abierto,
  miembroId,
  ficha,
  alCerrar,
}: FichaMiembroModalProps) {
  const idTitulo = useId()
  const [datosFicha, setDatosFicha] = useState<FichaInternaMiembroResponse | null>(ficha ?? null)
  const [cargando, setCargando] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (ficha) {
      setDatosFicha(ficha)
      setError(null)
      return
    }

    if (abierto && miembroId) {
      setCargando(true)
      setError(null)
      obtenerFichaMiembro(miembroId)
        .then((respuesta) => setDatosFicha(respuesta))
        .catch((err: unknown) => {
          const mensaje =
            err instanceof ApiError && err.status === 404
              ? (err.detalle ?? 'No se encontró la ficha del voluntario en tu comunidad')
              : 'Error al consultar la ficha del miembro'
          setError(mensaje)
        })
        .finally(() => setCargando(false))
    }
  }, [abierto, miembroId, ficha])

  if (!abierto) return null

  const kinti = datosFicha ? obtenerMensajeKinti(datosFicha.nivel) : null

  return (
    <div className="modal-fondo">
      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby={idTitulo}
        className="ficha-miembro-dialogo"
      >
        <h2 id={idTitulo} className="sr-only">
          Ficha del Miembro
        </h2>

        {cargando && <p className="modal-baja-aviso">Consultando ficha del miembro…</p>}
        {error && <Notificacion tipo="error" mensaje={error} />}

        {datosFicha && (
          <>
            <div className="ficha-miembro-identidad">
              <div
                className="ficha-miembro-avatar-contenedor"
                data-testid="avatar-andino"
                aria-hidden="true"
              >
                <svg
                  className="ficha-miembro-avatar"
                  viewBox="0 0 64 64"
                  fill="none"
                  xmlns="http://www.w3.org/2000/svg"
                >
                  <circle cx="32" cy="32" r="30" stroke="currentColor" strokeWidth="2" />
                  <circle cx="32" cy="24" r="10" stroke="currentColor" strokeWidth="2" />
                  <path
                    d="M14 50C14 41 22 38 32 38C42 38 50 41 50 50"
                    stroke="currentColor"
                    strokeWidth="2"
                    strokeLinecap="round"
                  />
                  <path
                    d="M32 10L35 14L32 18L29 14Z"
                    fill="currentColor"
                  />
                </svg>
              </div>

              <div className="ficha-miembro-titulos">
                <h3 className="ficha-miembro-nombre">{datosFicha.nombreCompleto}</h3>
                <div>
                  <Insignia
                    texto={datosFicha.nivel}
                    tono={
                      datosFicha.nivel === 'MAESTRO'
                        ? 'exito'
                        : datosFicha.nivel === 'OFICIAL'
                          ? 'advertencia'
                          : 'neutro'
                    }
                  />
                </div>
              </div>
            </div>

            {kinti && (
              <div className="ficha-miembro-kinti">
                <KintiGuia variante={kinti.variante} tamano="md" />
                <p className="ficha-miembro-kinti-mensaje">{kinti.mensaje}</p>
              </div>
            )}

            <dl className="ficha-miembro-datos">
              <div className="ficha-miembro-dato">
                <dt>Cédula de Identidad</dt>
                <dd>{datosFicha.cedula}</dd>
              </div>

              <div className="ficha-miembro-dato">
                <dt>Género</dt>
                <dd>{datosFicha.genero}</dd>
              </div>

              <div className="ficha-miembro-dato">
                <dt>Teléfono celular</dt>
                <dd>{datosFicha.telefono}</dd>
              </div>

              <div className="ficha-miembro-dato">
                <dt>Fecha de Nacimiento</dt>
                <dd>{`${formatearFecha(datosFicha.fechaNacimiento)} (${datosFicha.edad} años)`}</dd>
              </div>

              <div className="ficha-miembro-dato">
                <dt>Fecha de ingreso</dt>
                <dd>{formatearFecha(datosFicha.fechaIngreso)}</dd>
              </div>

              <div className="ficha-miembro-dato">
                <dt>Estado en la comunidad</dt>
                <dd>
                  {datosFicha.activoEnComunidad ? 'Activo' : 'Suspendido / Inactivo'}
                </dd>
              </div>

              {!datosFicha.activoEnComunidad && datosFicha.fechaBaja && (
                <div className="ficha-miembro-dato">
                  <dt>Fecha de baja</dt>
                  <dd>{formatearFecha(datosFicha.fechaBaja)}</dd>
                </div>
              )}

              {!datosFicha.activoEnComunidad && datosFicha.motivoBaja && (
                <div className="ficha-miembro-dato ficha-miembro-dato-completo">
                  <dt>Motivo de baja</dt>
                  <dd>{datosFicha.motivoBaja}</dd>
                </div>
              )}

              <div className="ficha-miembro-dato">
                <dt>Puntos acumulados</dt>
                <dd>{`${datosFicha.puntosTotales} puntos`}</dd>
              </div>
            </dl>
          </>
        )}

        <div className="ficha-miembro-acciones">
          <Boton variante="secundario" onClick={alCerrar}>
            Cerrar
          </Boton>
        </div>
      </div>
    </div>
  )
}
