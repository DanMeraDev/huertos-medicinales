import { Boton } from '../../components/Boton'
import { Insignia } from '../../components/Insignia'
import { KintiGuia } from '../../components/KintiGuia'
import { Tabla } from '../../components/Tabla'
import type { ActividadRevision } from './mspRevisionApi'
import './mspRevision.css'

export interface BandejaRevisionActividadesProps {
  actividades: ActividadRevision[]
  cargando: boolean
  alAuditar: (actividad: ActividadRevision) => void
}

export function BandejaRevisionActividades({
  actividades,
  cargando,
  alAuditar,
}: BandejaRevisionActividadesProps) {
  const columnas = [
    {
      titulo: 'Comunidad',
      celda: (fila: ActividadRevision) => fila.nombreComunidad,
    },
    {
      titulo: 'Saber Ancestral',
      celda: (fila: ActividadRevision) => fila.tituloSaber,
    },
    {
      titulo: 'Fecha',
      celda: (fila: ActividadRevision) => fila.fechaRealizacion,
    },
    {
      titulo: 'Intento',
      celda: (fila: ActividadRevision) => <Insignia texto={`Intento ${fila.numeroIntento}`} tono="neutro" />,
    },
    {
      titulo: 'Revisión',
      celda: (fila: ActividadRevision) => (
        <Boton variante="secundario" onClick={() => alAuditar(fila)}>
          Auditar Evidencia
        </Boton>
      ),
    },
  ]

  const hayActividades = actividades.length > 0

  return (
    <section className="msp-bandeja" aria-label="Bandeja de actividades pendientes de verificación">
      {!cargando && (
        <div className="msp-bandeja-kinti">
          <KintiGuia variante={hayActividades ? 'celebracion' : 'vacio'} tamano="md" />
          <p>
            {hayActividades
              ? 'Las comunidades están aprendiendo el saber ancestral de nuestros antepasados, celébralo revisando sus actividades'
              : 'No se han realizado actividades del saber ancestral'}
          </p>
        </div>
      )}
      <Tabla
        columnas={columnas}
        filas={actividades}
        claveFila={(fila) => fila.id}
        cargando={cargando}
      />
    </section>
  )
}
