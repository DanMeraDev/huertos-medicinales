import { useState } from 'react'
import { TablaInventarioBodega, type ComunidadSupervisada } from '../features/bodega/TablaInventarioBodega'

const COMUNIDADES_DEFAULT: ComunidadSupervisada[] = [
  { id: 1, nombre: 'Comunidad San Pedro' },
  { id: 2, nombre: 'Comunidad Santa Clara' },
]

export function InventarioBodegaPage() {
  const rol = typeof window !== 'undefined' ? localStorage.getItem('rol') : null
  const comunidadGuardada = typeof window !== 'undefined' ? localStorage.getItem('comunidadId') : null

  const esTecnicoMsp = rol === 'TECNICO_MSP'
  const comunidadInicial = comunidadGuardada ? Number(comunidadGuardada) : 1

  const [comunidadId, setComunidadId] = useState(comunidadInicial)

  return (
    <div className="pagina-inventario-bodega">
      <TablaInventarioBodega
        comunidadId={comunidadId}
        esTecnicoMsp={esTecnicoMsp}
        comunidadesSupervisadas={COMUNIDADES_DEFAULT}
        alCambiarComunidad={setComunidadId}
      />
    </div>
  )
}

