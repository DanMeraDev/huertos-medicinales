import { useState } from 'react'
import { useNavigate, useParams } from 'react-router'
import { Notificacion } from '../components/Notificacion'
import { FormularioSubsanarEvidencias } from '../features/actividades-ancestrales/FormularioSubsanarEvidencias'
import { ConsejoKinti } from '../components/ConsejoKinti'

export function SubsanarEvidenciaPage() {
  const { id } = useParams()
  const navegar = useNavigate()
  const [reenviada, setReenviada] = useState(false)

  function manejarReenvio() {
    setReenviada(true)
    setTimeout(() => {
      navegar('/actividades-ancestrales/observadas')
    }, 2000)
  }

  return (
    <div
      className="pagina-subsanar-evidencia"
      style={{
        maxWidth: 'var(--ancho-maximo, 1200px)',
        margin: '0 auto',
        width: '100%',
        padding: 'var(--espacio-4)',
      }}
    >
      {reenviada && (
        <Notificacion
          tipo="exito"
          mensaje="Evidencias corregidas reenviadas al Técnico MSP para una nueva revisión"
        />
      )}
      <h1
        style={{
          fontSize: '1.75rem',
          fontWeight: 700,
          color: 'var(--color-texto)',
          marginBottom: 'var(--espacio-4)',
        }}
      >
        Subsanar Evidencias
      </h1>
      <ConsejoKinti
        variante="alerta"
        mensaje="Lee las observaciones del Técnico MSP y vuelve a subir las evidencias corregidas."
      />
      <FormularioSubsanarEvidencias idActividad={Number(id)} alReenviar={manejarReenvio} />
    </div>
  )
}
