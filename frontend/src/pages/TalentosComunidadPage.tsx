import { useEffect, useState } from 'react'
import { Notificacion } from '../components/Notificacion'
import { CrearTalentoForm } from '../features/talentos/CrearTalentoForm'
import { GaleriaTalentosComunidad } from '../features/talentos/GaleriaTalentosComunidad'
import {
  obtenerActividadesParaTalentos,
  obtenerTalentosComunidad,
  type ActividadOpcion,
  type TalentoComunidadResponse,
} from '../features/talentos/talentosApi'
import './TalentosComunidadPage.css'

export function TalentosComunidadPage() {
  const [talentos, setTalentos] = useState<TalentoComunidadResponse[]>([])
  const [actividades, setActividades] = useState<ActividadOpcion[]>([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let montado = true
    Promise.all([obtenerTalentosComunidad(), obtenerActividadesParaTalentos()])
      .then(([talentosRes, actividadesRes]) => {
        if (montado) {
          setTalentos(talentosRes)
          setActividades(actividadesRes)
        }
      })
      .catch(() => {
        if (montado) {
          setError('No se pudo cargar la información de talentos y actividades')
        }
      })
      .finally(() => {
        if (montado) {
          setCargando(false)
        }
      })

    return () => {
      montado = false
    }
  }, [])

  function manejarNuevoTalento(nuevoTalento: TalentoComunidadResponse) {
    setTalentos((prev) => [nuevoTalento, ...prev])
  }

  return (
    <main className="talentos-pagina-contenedor">
      <header className="talentos-pagina-cabecera">
        <h2 className="talentos-pagina-titulo">Insignias y Talentos de la Comunidad</h2>
        <p className="talentos-pagina-subtitulo">
          Configura medallas de reconocimiento para motivar la constancia de los voluntarios en las labores comunitarias.
        </p>
      </header>

      {error && <Notificacion tipo="error" mensaje={error} />}

      {cargando ? (
        <p className="modal-baja-aviso">Cargando talentos de la comunidad…</p>
      ) : (
        <>
          <CrearTalentoForm actividades={actividades} alExito={manejarNuevoTalento} />
          <GaleriaTalentosComunidad talentos={talentos} />
        </>
      )}
    </main>
  )
}
