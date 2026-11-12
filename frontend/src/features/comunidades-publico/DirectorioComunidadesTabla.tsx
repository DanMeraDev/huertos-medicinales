import { useEffect, useState } from 'react'
import { Link } from 'react-router'
import { Boton } from '../../components/Boton'
import { Insignia } from '../../components/Insignia'
import { KintiGuia } from '../../components/KintiGuia'
import { Tabla, type ColumnaTabla } from '../../components/Tabla'
import { obtenerDirectorioComunidades, type DirectorioComunidadResponse } from './comunidadesPublicoApi'
import { MapaComunidadModal } from './MapaComunidadModal'
import './DirectorioComunidadesTabla.css'

export function DirectorioComunidadesTabla() {
  const [comunidades, setComunidades] = useState<DirectorioComunidadResponse[]>([])
  const [cargando, setCargando] = useState(true)
  const [comunidadSeleccionada, setComunidadSeleccionada] = useState<DirectorioComunidadResponse | null>(null)

  useEffect(() => {
    obtenerDirectorioComunidades()
      .then((datos) => {
        setComunidades(datos)
      })
      .finally(() => {
        setCargando(false)
      })
  }, [])

  const columnas: ColumnaTabla<DirectorioComunidadResponse>[] = [
    {
      titulo: 'Nombre de la Comunidad',
      celda: (fila) => <strong>{fila.nombre}</strong>,
    },
    {
      titulo: 'Responsable',
      celda: (fila) => fila.nombreResponsable,
    },
    {
      titulo: 'Teléfono',
      celda: (fila) => fila.telefonoContacto,
    },
    {
      titulo: 'Correo Electrónico',
      celda: (fila) => fila.correoContacto,
    },
    {
      titulo: 'Miembros Activos',
      celda: (fila) => <Insignia texto={String(fila.totalMiembros)} tono="exito" />,
    },
    {
      titulo: 'Ubicación',
      celda: (fila) => (
        <Boton variante="secundario" onClick={() => setComunidadSeleccionada(fila)}>
          Ver en el mapa
        </Boton>
      ),
    },
  ]

  return (
    <div className="directorio-contenedor">
      <div className="directorio-cabecera">
        <KintiGuia variante="dia" tamano="md" />
        <p className="directorio-cabecera-mensaje">
          ¡Todas estas comunidades son nuestros guardianes de la tierra! Quienes cultivan plantas medicinales protegen la
          vida, la salud comunitaria y la sabiduría de nuestros mayores
        </p>
      </div>

      <div className="directorio-cta">
        <div className="directorio-cta-contenido">
          <KintiGuia variante="celebracion" tamano="sm" />
          <p className="directorio-cta-mensaje">
            ¿Deseas fundar un huerto en tu barrio o sumarte a este movimiento sagrado?
          </p>
        </div>
        <Link to="/solicitar-comunidad" className="boton boton-primario directorio-cta-boton">
          Solicitar crear mi comunidad
        </Link>
      </div>

      {cargando ? (
        <div className="directorio-cargando">Cargando directorio de comunidades…</div>
      ) : comunidades.length === 0 ? (
        <div className="directorio-vacio">
          <KintiGuia variante="vacio" tamano="lg" />
          <p className="directorio-vacio-mensaje">
            Aún no se han registrado comunidades en la red. ¡Sé el primero en sembrar la tuya!
          </p>
          <Link to="/solicitar-comunidad" className="boton boton-primario">
            Solicitar crear mi comunidad
          </Link>
        </div>
      ) : (
        <div className="directorio-tabla-envoltorio">
          <Tabla
            columnas={columnas}
            filas={comunidades}
            claveFila={(fila) => fila.id}
          />
        </div>
      )}

      <MapaComunidadModal
        comunidad={comunidadSeleccionada}
        alCerrar={() => setComunidadSeleccionada(null)}
      />
    </div>
  )
}
