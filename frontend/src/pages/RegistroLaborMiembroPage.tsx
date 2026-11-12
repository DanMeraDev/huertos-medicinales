import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router'
import { Notificacion } from '../components/Notificacion'
import { FormularioRegistroLabor } from '../features/comunidad-actividades/FormularioRegistroLabor'
import {
  listarActividadesComunidad,
  listarMiembrosComunidad,
  obtenerInsigniasMiembro,
  registrarLaborMiembro,
  type ActividadComunitaria,
  type MiembroVoluntario,
} from '../features/comunidad-actividades/registroLaboresApi'

export function RegistroLaborMiembroPage() {
  const navigate = useNavigate()
  const [miembros, setMiembros] = useState<MiembroVoluntario[]>([])
  const [actividades, setActividades] = useState<ActividadComunitaria[]>([])
  const [cargando, setCargando] = useState(true)
  const [errorCarga, setErrorCarga] = useState<string | null>(null)

  useEffect(() => {
    let activo = true

    Promise.all([listarMiembrosComunidad(), listarActividadesComunidad()])
      .then(([datosMiembros, datosActividades]) => {
        if (activo) {
          setMiembros(datosMiembros)
          setActividades(datosActividades)
          setCargando(false)
        }
      })
      .catch((err) => {
        if (activo) {
          setErrorCarga(
            err instanceof Error
              ? err.message
              : 'Error al cargar los datos para registrar la jornada',
          )
          setCargando(false)
        }
      })

    return () => {
      activo = false
    }
  }, [])

  const manejarExito = () => {
    navigate('/comunidad/actividades-catalogo')
  }

  if (errorCarga) {
    return <Notificacion tipo="error" mensaje={errorCarga} />
  }

  return (
    <FormularioRegistroLabor
      miembros={miembros}
      actividades={actividades}
      alObtenerInsignias={obtenerInsigniasMiembro}
      alRegistrar={registrarLaborMiembro}
      cargando={cargando}
      alExito={manejarExito}
    />
  )
}
