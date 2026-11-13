import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router'
import { Boton } from '../components/Boton'
import { Insignia } from '../components/Insignia'
import { Notificacion } from '../components/Notificacion'
import { Tabla, type ColumnaTabla } from '../components/Tabla'
import {
  listarObservadas,
  type ActividadParaCorregirResponse,
} from '../features/actividades-ancestrales/actividadesAncestralesApi'

export function BandejaObservadasPage() {
  const [actividades, setActividades] = useState<ActividadParaCorregirResponse[]>([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const navegar = useNavigate()

  useEffect(() => {
    let vigente = true
    listarObservadas()
      .then((observadas) => {
        if (vigente) {
          setActividades(observadas)
          setError(null)
        }
      })
      .catch(() => {
        if (vigente) {
          setError('No se pudieron cargar las actividades observadas')
        }
      })
      .finally(() => {
        if (vigente) {
          setCargando(false)
        }
      })
    return () => {
      vigente = false
    }
  }, [])

  const columnas: ColumnaTabla<ActividadParaCorregirResponse>[] = [
    {
      titulo: 'Saber Ancestral',
      celda: (fila) => fila.tituloSaber,
    },
    {
      titulo: 'Intento',
      celda: (fila) => <Insignia texto={`Intento ${fila.numeroIntento}`} tono="neutro" />,
    },
    {
      titulo: 'Observación',
      celda: (fila) => fila.observacionMsp ?? 'Sin observación',
    },
    {
      titulo: 'Acción',
      celda: (fila) => (
        <Boton
          variante="primario"
          onClick={() => navegar(`/actividades-ancestrales/${fila.id}/subsanar`)}
        >
          Subsanar
        </Boton>
      ),
    },
  ]

  return (
    <div
      className="pagina-bandeja-observadas"
      style={{
        maxWidth: 'var(--ancho-maximo, 1200px)',
        margin: '0 auto',
        width: '100%',
        padding: 'var(--espacio-4)',
      }}
    >
      <h1
        style={{
          fontSize: '1.75rem',
          fontWeight: 700,
          color: 'var(--color-texto)',
          marginBottom: 'var(--espacio-4)',
        }}
      >
        Bandeja de actividades observadas
      </h1>

      {error && <Notificacion tipo="error" mensaje={error} />}

      <Tabla
        columnas={columnas}
        filas={actividades}
        claveFila={(fila) => fila.id}
        cargando={cargando}
      />
    </div>
  )
}
