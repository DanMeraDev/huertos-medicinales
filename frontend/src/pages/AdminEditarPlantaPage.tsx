import { useEffect, useState } from 'react'
import { useParams } from 'react-router'
import { ENLACE_CATALOGO, esErrorDeEstado } from '../features/catalogo-admin/envioPlanta'
import { FormularioEditarPlanta } from '../features/catalogo-admin/FormularioEditarPlanta'
import { AvisoFormulario } from '../features/catalogo-admin/PartesFormularioPlanta'
import { obtenerPlantaParaEditar } from '../features/catalogo-admin/plantasAdminApi'
import type { PlantaDetalleResponse } from '../features/catalogo-admin/types'
import { leerIdPlanta } from '../features/catalogo-plantas/leerIdPlanta'
import { ConsejoKinti } from '../components/ConsejoKinti'
import './AdminEditarPlantaPage.css'

const PLANTA_NO_DISPONIBLE = 'La planta no existe o fue dada de baja'
const ERROR_GENERICO = 'No se pudo cargar la ficha. Intente nuevamente más tarde'

type Resultado = { id: number; planta: PlantaDetalleResponse } | { id: number; error: string }

function ErrorCarga({ mensaje }: { mensaje: string }) {
  return <AvisoFormulario aviso={{ mensaje, enlace: ENLACE_CATALOGO }} />
}

function ContenidoEdicion({ id, resultado }: { id: number | null; resultado: Resultado | null }) {
  if (id === null) return <ErrorCarga mensaje={PLANTA_NO_DISPONIBLE} />
  const actual = resultado?.id === id ? resultado : null
  if (actual === null) return <p className="admin-editar-planta-cargando">Cargando ficha…</p>
  if ('error' in actual) return <ErrorCarga mensaje={actual.error} />
  return <FormularioEditarPlanta key={id} planta={actual.planta} />
}

export function AdminEditarPlantaPage() {
  const id = leerIdPlanta(useParams().id)
  const [resultado, setResultado] = useState<Resultado | null>(null)

  useEffect(() => {
    if (id === null) return
    let vigente = true
    obtenerPlantaParaEditar(id)
      .then((planta) => {
        if (vigente) setResultado({ id, planta })
      })
      .catch((error: unknown) => {
        if (vigente) setResultado({ id, error: esErrorDeEstado(error, 404) ? PLANTA_NO_DISPONIBLE : ERROR_GENERICO })
      })
    return () => {
      vigente = false
    }
  }, [id])

  return (
    <div className="admin-editar-planta-pagina">
      <h1>Editar ficha técnica</h1>
      <ConsejoKinti
        variante="sabiduria"
        mensaje="Revisa cada dato antes de guardar: las comunidades consultan esta ficha antes de sembrar."
      />
      <ContenidoEdicion id={id} resultado={resultado} />
    </div>
  )
}
