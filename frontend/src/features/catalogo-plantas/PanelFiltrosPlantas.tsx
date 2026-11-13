import { useEffect, useEffectEvent, useId, useState } from 'react'
import { BottomSheet } from '../../components/BottomSheet'
import { Boton } from '../../components/Boton'
import { CampoTexto } from '../../components/CampoTexto'
import { Insignia } from '../../components/Insignia'
import {
  contarFiltrosActivos,
  ETIQUETAS_CATEGORIA,
  ETIQUETAS_CICLO,
  INSIGNIAS_TERMICAS,
  type FiltrosPlanta,
} from './types'
import { useEsEscritorio } from './useEsEscritorio'
import './PanelFiltrosPlantas.css'

const ALTITUD_MINIMA = 0
const ALTITUD_MAXIMA = 6000
const PASO_ALTITUD = 100
const RETARDO_ALTITUD_MS = 400
const AVISO_ALTITUD = 'La altitud debe estar entre 0 y 6000 msnm'

export interface PanelFiltrosPlantasProps {
  filtros: FiltrosPlanta
  alCambiar: (filtros: FiltrosPlanta) => void
}

interface Opcion<T extends string> {
  valor: T | undefined
  etiqueta: string
}

function opcionesDe<T extends string>(etiquetaTodas: string, etiquetas: Record<T, string>): Opcion<T>[] {
  const valores = Object.keys(etiquetas) as T[]
  return [{ valor: undefined, etiqueta: etiquetaTodas }, ...valores.map((valor) => ({ valor, etiqueta: etiquetas[valor] }))]
}

const OPCIONES_CUALIDAD = opcionesDe('Todas', {
  CALIENTE: INSIGNIAS_TERMICAS.CALIENTE.texto,
  FRIA: INSIGNIAS_TERMICAS.FRIA.texto,
})
const OPCIONES_CICLO = opcionesDe('Todos', ETIQUETAS_CICLO)
const OPCIONES_REGION = opcionesDe('Todas', { Costa: 'Costa', Sierra: 'Sierra', Amazonía: 'Amazonía', Galápagos: 'Galápagos' })
const OPCIONES_CATEGORIA = opcionesDe('Todas', ETIQUETAS_CATEGORIA)

function leerAltitud(texto: string) {
  if (texto.trim() === '') return { altitud: undefined, valida: true }
  const altitud = Number(texto)
  return {
    altitud,
    valida: Number.isInteger(altitud) && altitud >= ALTITUD_MINIMA && altitud <= ALTITUD_MAXIMA,
  }
}

interface GrupoOpcionesProps<T extends string> {
  titulo: string
  opciones: Opcion<T>[]
  valor: T | undefined
  alElegir: (valor: T | undefined) => void
  comoChips?: boolean
}

function GrupoOpciones<T extends string>({ titulo, opciones, valor, alElegir, comoChips }: GrupoOpcionesProps<T>) {
  const idTitulo = useId()

  return (
    <section className="filtros-seccion">
      <h3 id={idTitulo} className="filtros-seccion-titulo">
        {titulo}
      </h3>
      <div role="group" aria-labelledby={idTitulo} className={comoChips ? 'filtros-chips' : 'filtros-segmentado'}>
        {opciones.map((opcion) => (
          <Boton
            key={opcion.etiqueta}
            className="filtros-opcion"
            aria-pressed={opcion.valor === valor}
            onClick={() => alElegir(opcion.valor)}
          >
            {opcion.etiqueta}
          </Boton>
        ))}
      </div>
    </section>
  )
}

interface FormularioFiltrosProps {
  filtros: FiltrosPlanta
  alCambiar: (filtros: FiltrosPlanta) => void
  alEscribirAltitud: (altitud: number | undefined, valida: boolean) => void
}

function FormularioFiltros({ filtros, alCambiar, alEscribirAltitud }: FormularioFiltrosProps) {
  const [textoAltitud, setTextoAltitud] = useState(filtros.altitud?.toString() ?? '')
  const [altitudRecibida, setAltitudRecibida] = useState(filtros.altitud)

  if (altitudRecibida !== filtros.altitud) {
    setAltitudRecibida(filtros.altitud)
    setTextoAltitud(filtros.altitud?.toString() ?? '')
  }

  const { altitud, valida } = leerAltitud(textoAltitud)
  const valorDeslizador = Math.min(Math.max(altitud ?? ALTITUD_MINIMA, ALTITUD_MINIMA), ALTITUD_MAXIMA)

  function cambiarAltitud(texto: string) {
    setTextoAltitud(texto)
    const lectura = leerAltitud(texto)
    alEscribirAltitud(lectura.valida ? lectura.altitud : undefined, lectura.valida)
  }

  return (
    <div className="filtros-formulario">
      <GrupoOpciones
        titulo="Cualidad térmica"
        opciones={OPCIONES_CUALIDAD}
        valor={filtros.cualidad}
        alElegir={(cualidad) => alCambiar({ ...filtros, cualidad })}
      />
      <section className="filtros-seccion">
        <h3 className="filtros-seccion-titulo">Rango de altitud</h3>
        <input
          type="range"
          className="filtros-deslizador"
          aria-label="Deslizador de altitud"
          aria-valuetext={altitud === undefined ? 'Sin filtro de altitud' : `${valorDeslizador} msnm`}
          min={ALTITUD_MINIMA}
          max={ALTITUD_MAXIMA}
          step={PASO_ALTITUD}
          value={valorDeslizador}
          onChange={(evento) => cambiarAltitud(evento.target.value)}
        />
        <CampoTexto
          type="number"
          inputMode="numeric"
          etiqueta="Altitud (msnm)"
          placeholder="Sin filtro"
          min={ALTITUD_MINIMA}
          max={ALTITUD_MAXIMA}
          valor={textoAltitud}
          alCambiar={cambiarAltitud}
          error={valida ? undefined : AVISO_ALTITUD}
        />
      </section>
      <GrupoOpciones
        titulo="Ciclo de cultivo"
        opciones={OPCIONES_CICLO}
        valor={filtros.ciclo}
        alElegir={(ciclo) => alCambiar({ ...filtros, ciclo })}
      />
      <GrupoOpciones
        titulo="Región"
        opciones={OPCIONES_REGION}
        valor={filtros.region}
        alElegir={(region) => alCambiar({ ...filtros, region })}
        comoChips
      />
      <GrupoOpciones
        titulo="Categoría de uso"
        opciones={OPCIONES_CATEGORIA}
        valor={filtros.categoria}
        alElegir={(categoria) => alCambiar({ ...filtros, categoria })}
        comoChips
      />
    </div>
  )
}

function BarraLateralFiltros({ filtros, alCambiar }: PanelFiltrosPlantasProps) {
  const idTitulo = useId()
  const [reinicio, setReinicio] = useState(0)
  const [altitudPendiente, setAltitudPendiente] = useState<{ altitud: number | undefined } | null>(null)
  const aplicarAltitud = useEffectEvent((altitud: number | undefined) => alCambiar({ ...filtros, altitud }))

  useEffect(() => {
    if (altitudPendiente === null) return
    const temporizador = window.setTimeout(() => aplicarAltitud(altitudPendiente.altitud), RETARDO_ALTITUD_MS)
    return () => window.clearTimeout(temporizador)
  }, [altitudPendiente])

  function limpiar() {
    setAltitudPendiente(null)
    setReinicio((anterior) => anterior + 1)
    alCambiar({})
  }

  return (
    <aside className="catalogo-sidebar" aria-labelledby={idTitulo}>
      <div className="filtros-cabecera">
        <h2 id={idTitulo} className="filtros-titulo">
          Filtros
        </h2>
        <button type="button" className="filtros-limpiar" onClick={limpiar}>
          Limpiar todo
        </button>
      </div>
      <FormularioFiltros
        key={reinicio}
        filtros={filtros}
        alCambiar={alCambiar}
        alEscribirAltitud={(altitud, valida) => setAltitudPendiente(valida ? { altitud } : null)}
      />
    </aside>
  )
}

function PanelInferiorFiltros({ filtros, alCambiar }: PanelFiltrosPlantasProps) {
  const [abierto, setAbierto] = useState(false)
  const [borrador, setBorrador] = useState<FiltrosPlanta>({})
  const [altitudValida, setAltitudValida] = useState(true)
  const [reinicio, setReinicio] = useState(0)
  const activos = contarFiltrosActivos(filtros)

  function abrir() {
    setBorrador(filtros)
    setAltitudValida(true)
    setAbierto(true)
  }

  function aplicar() {
    alCambiar(borrador)
    setAbierto(false)
  }

  function limpiar() {
    setBorrador({})
    setAltitudValida(true)
    setReinicio((anterior) => anterior + 1)
  }

  function escribirAltitud(altitud: number | undefined, valida: boolean) {
    setAltitudValida(valida)
    if (valida) setBorrador((anterior) => ({ ...anterior, altitud }))
  }

  return (
    <>
      <div className="filtros-movil">
        <Boton
          className="filtros-boton"
          aria-haspopup="dialog"
          aria-label={activos > 0 ? `Filtros (${activos} ${activos === 1 ? 'activo' : 'activos'})` : undefined}
          onClick={abrir}
        >
          Filtros
          {activos > 0 && <Insignia texto={String(activos)} />}
        </Boton>
      </div>
      <BottomSheet
        abierto={abierto}
        titulo="Filtros"
        alCerrar={() => setAbierto(false)}
        pie={
          <>
            <Boton variante="secundario" onClick={limpiar}>
              Limpiar todo
            </Boton>
            <Boton onClick={aplicar} disabled={!altitudValida}>
              Aplicar filtros
            </Boton>
          </>
        }
      >
        <FormularioFiltros
          key={reinicio}
          filtros={borrador}
          alCambiar={setBorrador}
          alEscribirAltitud={escribirAltitud}
        />
      </BottomSheet>
    </>
  )
}

export function PanelFiltrosPlantas(props: PanelFiltrosPlantasProps) {
  const esEscritorio = useEsEscritorio()
  return esEscritorio ? <BarraLateralFiltros {...props} /> : <PanelInferiorFiltros {...props} />
}
