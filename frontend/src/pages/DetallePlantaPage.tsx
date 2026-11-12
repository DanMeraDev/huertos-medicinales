import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router'
import { ApiError } from '../api/client'
import '../components/Boton.css'
import { Notificacion } from '../components/Notificacion'
import { esTecnicoMsp } from '../features/catalogo-admin/sesionTecnicoMsp'
import { FichaTecnicaView } from '../features/catalogo-plantas/FichaTecnicaView'
import { leerIdPlanta } from '../features/catalogo-plantas/leerIdPlanta'
import { MensajeDeNavegacion } from '../features/catalogo-plantas/MensajeDeNavegacion'
import { obtenerDetallePlanta } from '../features/catalogo-plantas/plantasApi'
import type { PlantaDetalle } from '../features/catalogo-plantas/types'
import './DetallePlantaPage.css'

const FICHA_NO_DISPONIBLE = 'La ficha solicitada no está disponible'
const ERROR_GENERICO = 'No se pudo cargar la ficha. Intente nuevamente más tarde'

type Resultado = { id: number; planta: PlantaDetalle } | { id: number; error: string }

function ErrorFicha({ mensaje }: { mensaje: string }) {
  return (
    <div className="detalle-planta-error">
      <Notificacion tipo="error" mensaje={mensaje} />
      <Link to="/catalogo-plantas" className="boton boton-secundario detalle-planta-volver">
        Volver al catálogo
      </Link>
    </div>
  )
}

function AccionesTecnico({ id }: { id: number }) {
  if (!esTecnicoMsp()) return null
  return (
    <div className="detalle-planta-acciones">
      <Link to={`/admin/plantas/${id}/editar`} className="boton boton-primario detalle-planta-accion">
        Editar ficha
      </Link>
    </div>
  )
}

function ContenidoDetalle({ id, resultado }: { id: number | null; resultado: Resultado | null }) {
  if (id === null) return <ErrorFicha mensaje={FICHA_NO_DISPONIBLE} />
  const actual = resultado?.id === id ? resultado : null
  if (actual === null) return <p className="detalle-planta-cargando">Cargando ficha…</p>
  if ('error' in actual) return <ErrorFicha mensaje={actual.error} />
  return (
    <div className="detalle-planta">
      <AccionesTecnico id={id} />
      <FichaTecnicaView planta={actual.planta} />
    </div>
  )
}

export function DetallePlantaPage() {
  const id = leerIdPlanta(useParams().id)
  const [resultado, setResultado] = useState<Resultado | null>(null)

  useEffect(() => {
    if (id === null) return
    let vigente = true
    obtenerDetallePlanta(id)
      .then((planta) => {
        if (vigente) setResultado({ id, planta })
      })
      .catch((error: unknown) => {
        if (!vigente) return
        const noEncontrada = error instanceof ApiError && error.status === 404
        setResultado({ id, error: noEncontrada ? FICHA_NO_DISPONIBLE : ERROR_GENERICO })
      })
    return () => {
      vigente = false
    }
  }, [id])

  return (
    <>
      <MensajeDeNavegacion />
      <ContenidoDetalle id={id} resultado={resultado} />
    </>
  )
}
