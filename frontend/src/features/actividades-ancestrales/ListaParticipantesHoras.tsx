import { useState } from 'react'
import { Boton } from '../../components/Boton'
import { CampoTexto } from '../../components/CampoTexto'
import { Selector } from '../../components/Selector'
import { Tabla } from '../../components/Tabla'
import type { MiembroComunidadOpcion } from './actividadesAncestralesApi'
import './FormularioActividadAncestral.css'

export interface ParticipanteNominado {
  miembroId: number
  nombreCompleto: string
  horasTrabajadas: number
}

interface ListaParticipantesHorasProps {
  miembros: MiembroComunidadOpcion[]
  participantes: ParticipanteNominado[]
  alAgregar: (participante: ParticipanteNominado) => void
  alQuitar: (miembroId: number) => void
  error?: string
}

export function ListaParticipantesHoras({
  miembros,
  participantes,
  alAgregar,
  alQuitar,
  error,
}: ListaParticipantesHorasProps) {
  const [miembroSeleccionado, setMiembroSeleccionado] = useState('')
  const [horas, setHoras] = useState('')
  const [erroresFila, setErroresFila] = useState<Record<string, string>>({})

  function agregarParticipante() {
    const nuevosErrores: Record<string, string> = {}

    if (!miembroSeleccionado) {
      nuevosErrores.miembro = 'Selecciona el miembro voluntario participante'
    }

    if (!/^\d+$/.test(horas) || Number(horas) < 1 || Number(horas) > 24) {
      nuevosErrores.horas = 'Las horas trabajadas deben estar entre 1 y 24'
    }

    setErroresFila(nuevosErrores)
    if (Object.keys(nuevosErrores).length > 0) {
      return
    }

    const miembro = miembros.find((opcion) => opcion.id === Number(miembroSeleccionado))
    if (!miembro) {
      return
    }

    if (participantes.some((participante) => participante.miembroId === miembro.id)) {
      setErroresFila({ miembro: 'El participante ya está registrado en la actividad' })
      return
    }

    alAgregar({
      miembroId: miembro.id,
      nombreCompleto: miembro.nombreCompleto,
      horasTrabajadas: Number(horas),
    })
    setMiembroSeleccionado('')
    setHoras('')
  }

  const columnas = [
    {
      titulo: 'Miembro',
      celda: (fila: ParticipanteNominado) => fila.nombreCompleto,
    },
    {
      titulo: 'Horas trabajadas',
      celda: (fila: ParticipanteNominado) => fila.horasTrabajadas,
    },
    {
      titulo: 'Acción',
      celda: (fila: ParticipanteNominado) => (
        <Boton
          variante="peligro"
          className="lista-participantes-quitar"
          onClick={() => alQuitar(fila.miembroId)}
        >
          Quitar
        </Boton>
      ),
    },
  ]

  return (
    <div className="lista-participantes">
      <span className="campo-etiqueta">Nómina de Participantes</span>
      <div className="lista-participantes-agregar">
        <Selector
          etiqueta="Miembro Voluntario"
          opciones={miembros.map((miembro) => ({
            valor: String(miembro.id),
            etiqueta: miembro.nombreCompleto,
          }))}
          valor={miembroSeleccionado}
          alCambiar={setMiembroSeleccionado}
          error={erroresFila.miembro}
        />
        <CampoTexto
          etiqueta="Horas Trabajadas"
          type="number"
          min={1}
          max={24}
          inputMode="numeric"
          valor={horas}
          alCambiar={setHoras}
          error={erroresFila.horas}
        />
        <Boton variante="secundario" onClick={agregarParticipante}>
          Agregar participante
        </Boton>
      </div>
      {error && <p className="campo-error">{error}</p>}
      <div className="lista-participantes-tabla">
        <Tabla
          columnas={columnas}
          filas={participantes}
          claveFila={(fila) => fila.miembroId}
        />
      </div>
    </div>
  )
}
