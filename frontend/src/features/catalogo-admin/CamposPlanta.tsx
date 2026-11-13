import { useId } from 'react'
import { Boton } from '../../components/Boton'
import { CampoTexto } from '../../components/CampoTexto'
import { Selector } from '../../components/Selector'
import { CampoAreaTexto } from './CampoAreaTexto'
import {
  OPCIONES_CATEGORIA,
  OPCIONES_CICLO,
  OPCIONES_CUALIDAD,
  OPCIONES_ORIGEN,
  OPCIONES_REGION,
  type Opcion,
} from './opcionesPlanta'
import type { OrigenPlanta } from './types'
import type { DatosPlanta, ErroresPlanta } from './validacionPlanta'

export interface CamposPlantaProps {
  datos: DatosPlanta
  errores: ErroresPlanta
  alCambiar: <K extends keyof DatosPlanta>(campo: K, valor: DatosPlanta[K]) => void
}

interface GrupoOpcionesProps<T extends string> {
  titulo: string
  opciones: Opcion<T>[]
  seleccionados: T[]
  alPulsar: (valor: T) => void
  comoChips?: boolean
  error?: string
}

function GrupoOpciones<T extends string>({
  titulo,
  opciones,
  seleccionados,
  alPulsar,
  comoChips = false,
  error,
}: GrupoOpcionesProps<T>) {
  const idTitulo = useId()

  return (
    <div className="campo">
      <span id={idTitulo} className="campo-etiqueta">
        {titulo}
      </span>
      <div role="group" aria-labelledby={idTitulo} className={comoChips ? 'crear-planta-chips' : 'crear-planta-segmentado'}>
        {opciones.map((opcion) => (
          <Boton
            key={opcion.valor}
            className="crear-planta-opcion"
            aria-pressed={seleccionados.includes(opcion.valor)}
            onClick={() => alPulsar(opcion.valor)}
          >
            {opcion.etiqueta}
          </Boton>
        ))}
      </div>
      {error && <p className="campo-error">{error}</p>}
    </div>
  )
}

function alternar<T>(lista: T[], valor: T) {
  return lista.includes(valor) ? lista.filter((elemento) => elemento !== valor) : [...lista, valor]
}

function unicoSeleccionado<T extends string>(valor: T | '') {
  return valor ? [valor] : []
}

export function CamposIdentificacion({ datos, errores, alCambiar }: CamposPlantaProps) {
  return (
    <div className="crear-planta-pares">
      <CampoTexto
        etiqueta="Nombre común"
        valor={datos.nombreComun}
        alCambiar={(valor) => alCambiar('nombreComun', valor)}
        error={errores.nombreComun}
      />
      <CampoTexto
        etiqueta="Nombre científico"
        valor={datos.nombreCientifico}
        alCambiar={(valor) => alCambiar('nombreCientifico', valor)}
        error={errores.nombreCientifico}
      />
      <CampoTexto
        etiqueta="Nombre kichwa"
        valor={datos.nombreKichwa}
        alCambiar={(valor) => alCambiar('nombreKichwa', valor)}
        error={errores.nombreKichwa}
      />
      <CampoTexto
        etiqueta="Familia botánica"
        valor={datos.familiaBotanica}
        alCambiar={(valor) => alCambiar('familiaBotanica', valor)}
        error={errores.familiaBotanica}
      />
    </div>
  )
}

function CamposAltitud({ datos, errores, alCambiar }: CamposPlantaProps) {
  const fueraDeRango = Boolean(errores.rangoAltitud)

  return (
    <div className="campo">
      <div className="crear-planta-altitudes">
        <CampoTexto
          etiqueta="Altitud mínima (msnm)"
          type="number"
          inputMode="numeric"
          min={0}
          step={1}
          valor={datos.altitudMinMsnm}
          alCambiar={(valor) => alCambiar('altitudMinMsnm', valor)}
          error={errores.altitudMinMsnm}
          aria-invalid={fueraDeRango || Boolean(errores.altitudMinMsnm)}
        />
        <CampoTexto
          etiqueta="Altitud máxima (msnm)"
          type="number"
          inputMode="numeric"
          min={0}
          step={1}
          valor={datos.altitudMaxMsnm}
          alCambiar={(valor) => alCambiar('altitudMaxMsnm', valor)}
          error={errores.altitudMaxMsnm}
          aria-invalid={fueraDeRango || Boolean(errores.altitudMaxMsnm)}
        />
      </div>
      {errores.rangoAltitud && <p className="campo-error">{errores.rangoAltitud}</p>}
    </div>
  )
}

export function CamposCultivo(props: CamposPlantaProps) {
  const { datos, errores, alCambiar } = props

  return (
    <div className="crear-planta-campos">
      <GrupoOpciones
        titulo="Cualidad térmica"
        opciones={OPCIONES_CUALIDAD}
        seleccionados={unicoSeleccionado(datos.cualidadTermica)}
        alPulsar={(valor) => alCambiar('cualidadTermica', valor)}
        error={errores.cualidadTermica}
      />
      <GrupoOpciones
        titulo="Ciclo de cultivo"
        opciones={OPCIONES_CICLO}
        seleccionados={unicoSeleccionado(datos.cicloCultivo)}
        alPulsar={(valor) => alCambiar('cicloCultivo', datos.cicloCultivo === valor ? '' : valor)}
      />
      <Selector
        etiqueta="Origen"
        opciones={OPCIONES_ORIGEN}
        valor={datos.origen}
        alCambiar={(valor) => alCambiar('origen', valor as OrigenPlanta)}
      />
      <CamposAltitud {...props} />
      <GrupoOpciones
        titulo="Regiones de distribución"
        comoChips
        opciones={OPCIONES_REGION}
        seleccionados={datos.regionesDistribucion}
        alPulsar={(valor) => alCambiar('regionesDistribucion', alternar(datos.regionesDistribucion, valor))}
        error={errores.regionesDistribucion}
      />
    </div>
  )
}

export function CamposUsos({ datos, errores, alCambiar }: CamposPlantaProps) {
  return (
    <div className="crear-planta-campos">
      <GrupoOpciones
        titulo="Categorías de uso"
        comoChips
        opciones={OPCIONES_CATEGORIA}
        seleccionados={datos.categorias}
        alPulsar={(valor) => alCambiar('categorias', alternar(datos.categorias, valor))}
        error={errores.categorias}
      />
      <CampoAreaTexto
        etiqueta="Usos medicinales"
        placeholder="Afecciones que trata y cómo ayuda la planta"
        valor={datos.usosMedicinales}
        alCambiar={(valor) => alCambiar('usosMedicinales', valor)}
        error={errores.usosMedicinales}
      />
    </div>
  )
}

export function CampoPreparacion({ datos, errores, alCambiar }: CamposPlantaProps) {
  return (
    <CampoAreaTexto
      etiqueta="Preparación y dosificación"
      placeholder="Infusión, cocción, cantidades y frecuencia recomendadas"
      valor={datos.preparacionDosificacion}
      alCambiar={(valor) => alCambiar('preparacionDosificacion', valor)}
      error={errores.preparacionDosificacion}
    />
  )
}

export function CampoContraindicaciones({ datos, errores, alCambiar }: CamposPlantaProps) {
  return (
    <div className="crear-planta-preventivo">
      <CampoAreaTexto
        etiqueta="Contraindicaciones"
        placeholder="Casos en los que no se debe usar la planta"
        valor={datos.contraindicaciones}
        alCambiar={(valor) => alCambiar('contraindicaciones', valor)}
        error={errores.contraindicaciones}
      />
    </div>
  )
}
