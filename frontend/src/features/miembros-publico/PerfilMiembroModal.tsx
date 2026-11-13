import { Insignia } from '../../components/Insignia'
import { KintiGuia, type KintiVariante } from '../../components/KintiGuia'
import { Modal } from '../../components/Modal'
import type { NivelMiembro, PerfilPublicoMiembroResponse } from './miembrosPublicoApi'
import './PerfilMiembroModal.css'

export interface PerfilMiembroModalProps {
  abierto: boolean
  perfil: PerfilPublicoMiembroResponse | null
  alCerrar: () => void
}

function obtenerTonoNivel(nivel: NivelMiembro): 'exito' | 'advertencia' | 'neutro' {
  if (nivel === 'MAESTRO') return 'exito'
  if (nivel === 'OFICIAL') return 'advertencia'
  return 'neutro'
}

function obtenerVarianteKinti(nivel: NivelMiembro): KintiVariante {
  if (nivel === 'MAESTRO') return 'maestro'
  if (nivel === 'OFICIAL') return 'oficial'
  return 'aprendiz'
}

function formatearFecha(fechaIso: string): string {
  const [anio, mes, dia] = fechaIso.slice(0, 10).split('-')
  return `${dia}/${mes}/${anio}`
}

export function PerfilMiembroModal({ abierto, perfil, alCerrar }: PerfilMiembroModalProps) {
  if (!abierto || !perfil) return null

  return (
    <Modal
      abierto={abierto}
      titulo="Perfil Público del Voluntario"
      alCancelar={alCerrar}
      textoCancelar="Cerrar"
      className="perfil-miembro-modal"
    >
      <div className="perfil-miembro-cuerpo">
        <div className="perfil-miembro-kinti">
          <KintiGuia variante={obtenerVarianteKinti(perfil.nivel)} tamano="md" />
          <p className="perfil-miembro-kinti-saludo">¡Sembrador/a ejemplar de nuestra red!</p>
        </div>

        <div className="perfil-miembro-cabecera">
          <h3 className="perfil-miembro-nombre">{perfil.nombreCompleto}</h3>
          <div className="perfil-miembro-meta">
            <Insignia texto={perfil.nivel} tono={obtenerTonoNivel(perfil.nivel)} />
            <span className="perfil-miembro-puntos">{`${perfil.puntosTotales} puntos acumulados`}</span>
          </div>
        </div>

        <section className="perfil-miembro-seccion" aria-label="Comunidades vinculadas">
          <h4 className="perfil-miembro-subtitulo">Comunidades vinculadas</h4>
          {perfil.comunidades.length > 0 ? (
            <ul className="perfil-miembro-comunidades">
              {perfil.comunidades.map((comunidad) => (
                <li key={comunidad} className="perfil-miembro-chip">
                  {comunidad}
                </li>
              ))}
            </ul>
          ) : (
            <p className="perfil-miembro-vacio">Sin comunidades activas registradas</p>
          )}
        </section>

        <section className="perfil-miembro-seccion" aria-label="Vitrina de insignias y talentos">
          <h4 className="perfil-miembro-subtitulo">Vitrina de insignias y talentos</h4>
          {perfil.insignias.length > 0 ? (
            <ul className="perfil-miembro-insignias">
              {perfil.insignias.map((insignia) => (
                <li
                  key={`${insignia.nombre}-${insignia.fechaObtencion}`}
                  className="perfil-miembro-insignia-card"
                >
                  <div className="perfil-miembro-insignia-top">
                    <span className="perfil-miembro-insignia-nombre">{insignia.nombre}</span>
                    <Insignia
                      texto={insignia.tipo === 'OFICIAL_SABER_MSP' ? 'Oficial MSP' : 'Talento'}
                      tono={insignia.tipo === 'OFICIAL_SABER_MSP' ? 'exito' : 'advertencia'}
                    />
                  </div>
                  <p className="perfil-miembro-insignia-desc">{insignia.descripcion}</p>
                  <span className="perfil-miembro-insignia-fecha">
                    {`Obtenida: ${formatearFecha(insignia.fechaObtencion)}`}
                  </span>
                </li>
              ))}
            </ul>
          ) : (
            <p className="perfil-miembro-vacio">
              Aún no registra insignias en su vitrina comunitaria
            </p>
          )}
        </section>

        <p className="perfil-miembro-lopdp">
          Información comunitaria y logros visibles en conformidad con el consentimiento otorgado
          por el voluntario para fines de reconocimiento, motivación y trazabilidad comunitaria
          (LOPDP Ecuador). Prohibido su uso para fines comerciales o de contacto no autorizado.
        </p>
      </div>
    </Modal>
  )
}
