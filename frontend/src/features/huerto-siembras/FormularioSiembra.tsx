import { useEffect, useState, useId } from 'react'
import { Boton } from '../../components/Boton'
import { CampoTexto } from '../../components/CampoTexto'
import { KintiGuia } from '../../components/KintiGuia'
import { Notificacion } from '../../components/Notificacion'
import {
  obtenerDetallePlanta,
  obtenerHuerto,
  obtenerInventarioBodega,
  obtenerSubhuertos,
  registrarSiembra,
  type FaseLunar,
  type HuertoDetalle,
  type MaterialBodegaItem,
  type PlantaDetalleItem,
  type RegistrarSiembraPayload,
  type SiembraDetalle,
  type SubhuertoItem,
} from './siembrasApi'
import './FormularioSiembra.css'

export interface FormularioSiembraProps {
  huerto?: HuertoDetalle
  subhuertos?: SubhuertoItem[]
  materiales?: MaterialBodegaItem[]
  plantasDetalle?: Record<number, PlantaDetalleItem>
  alRegistrar?: (datos: RegistrarSiembraPayload) => Promise<SiembraDetalle>
  alExito?: (siembra: SiembraDetalle) => void
}

const FASES_LUNARES: { clave: FaseLunar; etiqueta: string; descripcion: string }[] = [
  { clave: 'YANA_KILLA_OSCURA', etiqueta: 'Yana', descripcion: 'Luna Oscura / Nueva' },
  { clave: 'WINAK_KILLA_CRECIENTE', etiqueta: 'Wiñak', descripcion: 'Luna Creciente' },
  { clave: 'PURA_KILLA_LLENA', etiqueta: 'Pura', descripcion: 'Luna Llena' },
  { clave: 'WANUK_KILLA_MENGUANTE', etiqueta: 'Wañuk', descripcion: 'Luna Menguante' },
]

export function FormularioSiembra({
  huerto,
  subhuertos,
  materiales,
  plantasDetalle,
  alRegistrar = registrarSiembra,
  alExito,
}: FormularioSiembraProps) {
  const idAlerta = useId()

  const [huertoCargado, setHuertoCargado] = useState<HuertoDetalle | null>(null)
  const [subhuertosCargados, setSubhuertosCargados] = useState<SubhuertoItem[]>([])
  const [materialesCargados, setMaterialesCargados] = useState<MaterialBodegaItem[]>([])
  const [plantasMapCargadas, setPlantasMapCargadas] = useState<Record<number, PlantaDetalleItem>>({})

  const huertoActual = huerto ?? huertoCargado
  const subhuertosLista = subhuertos ?? subhuertosCargados
  const materialesLista = materiales ?? materialesCargados
  const plantasMap = plantasDetalle ?? plantasMapCargadas

  const [subhuertoSeleccionadoId, setSubhuertoSeleccionadoId] = useState<number | null>(null)
  const [materialSeleccionadoId, setMaterialSeleccionadoId] = useState<number | null>(null)
  const [cantidadSembrada, setCantidadSembrada] = useState<string>('')
  const [fechaSiembra, setFechaSiembra] = useState<string>(() => new Date().toISOString().split('T')[0])
  const [faseLunar, setFaseLunar] = useState<FaseLunar>('WINAK_KILLA_CRECIENTE')

  const [alertaAltitudAbierta, setAlertaAltitudAbierta] = useState(false)
  const [enviando, setEnviando] = useState(false)
  const [errorGlobal, setErrorGlobal] = useState<string | null>(null)

  const necesitaCargar = !huerto || !subhuertos || !materiales
  const [cargandoInicial, setCargandoInicial] = useState(necesitaCargar)

  useEffect(() => {
    if (!necesitaCargar) {
      return
    }

    let activo = true
    async function cargarDatos() {
      try {
        const [h, subs] = await Promise.all([obtenerHuerto(), obtenerSubhuertos()])
        if (!activo) return
        setHuertoCargado(h)
        setSubhuertosCargados(subs)

        if (h.comunidadId) {
          const mats = await obtenerInventarioBodega(h.comunidadId)
          if (!activo) return
          setMaterialesCargados(mats)

          const mapaPlantas: Record<number, PlantaDetalleItem> = {}
          await Promise.all(
            mats.map(async (m) => {
              try {
                const detalle = await obtenerDetallePlanta(m.plantaId)
                mapaPlantas[m.plantaId] = detalle
              } catch {
                // Continua si falla una planta
              }
            })
          )
          if (!activo) return
          setPlantasMapCargadas(mapaPlantas)
        }
      } catch (err) {
        if (activo) {
          setErrorGlobal(err instanceof Error ? err.message : 'Error al cargar datos del huerto')
        }
      } finally {
        if (activo) {
          setCargandoInicial(false)
        }
      }
    }

    cargarDatos()
    return () => {
      activo = false
    }
  }, [necesitaCargar])

  const materialSeleccionado = materialesLista.find((m) => m.id === materialSeleccionadoId) ?? null
  const plantaSeleccionada = materialSeleccionado ? plantasMap[materialSeleccionado.plantaId] : null

  const cantidadNum = parseFloat(cantidadSembrada) || 0
  const stockDisponible = materialSeleccionado ? materialSeleccionado.cantidadDisponible : 0
  const esStockInsuficiente = materialSeleccionado !== null && cantidadNum > stockDisponible
  const saldoRestante = materialSeleccionado ? Math.max(0, stockDisponible - cantidadNum) : 0

  const altitudHuerto = huertoActual?.altitudMsnm ?? 2500
  const fueraDeRangoAltitud =
    plantaSeleccionada !== null &&
    plantaSeleccionada !== undefined &&
    ((plantaSeleccionada.altitudMinMsnm != null && altitudHuerto < plantaSeleccionada.altitudMinMsnm) ||
      (plantaSeleccionada.altitudMaxMsnm != null && altitudHuerto > plantaSeleccionada.altitudMaxMsnm))

  async function ejecutarEnvio(aceptaRiesgo: boolean) {
    if (!subhuertoSeleccionadoId || !materialSeleccionadoId || cantidadNum <= 0) {
      setErrorGlobal('Por favor completa todos los campos obligatorios con valores válidos.')
      return
    }

    try {
      setEnviando(true)
      setErrorGlobal(null)
      const respuesta = await alRegistrar({
        subhuertoId: subhuertoSeleccionadoId,
        bodegaMaterialId: materialSeleccionadoId,
        cantidadSembrada: cantidadNum,
        fechaSiembra,
        faseLunarSiembra: faseLunar,
        aceptaRiesgoAltitud: aceptaRiesgo,
      })
      setAlertaAltitudAbierta(false)
      if (alExito) {
        alExito(respuesta)
      }
    } catch (err) {
      setErrorGlobal(err instanceof Error ? err.message : 'Error al registrar la siembra')
    } finally {
      setEnviando(false)
    }
  }

  function manejarEnvio(e: React.FormEvent) {
    e.preventDefault()
    if (fueraDeRangoAltitud) {
      setAlertaAltitudAbierta(true)
      return
    }
    ejecutarEnvio(false)
  }

  if (cargandoInicial) {
    return <div className="siembra-layout">Cargando datos para el registro de siembra...</div>
  }

  return (
    <div className="siembra-layout">
      {errorGlobal && (
        <Notificacion tipo="error" mensaje={errorGlobal} />
      )}

      <header className="siembra-cabecera-fija">
        <div className="siembra-cabecera-info">
          <h2 className="siembra-cabecera-titulo">{huertoActual?.nombreHuerto ?? 'Huerto Comunitario'}</h2>
          <span className="siembra-cabecera-altitud">
            Altitud: {altitudHuerto} msnm
          </span>
        </div>
        <KintiGuia variante="sabiduria" tamano="sm" />
      </header>

      <div className="siembra-split-view">
        <form className="siembra-columna-formulario" onSubmit={manejarEnvio}>
          <section className="siembra-tarjeta">
            <h3 className="siembra-tarjeta-titulo">
              <span className="siembra-tarjeta-numero">1</span> Subhuerto
            </h3>
            <div className="siembra-opciones-grid">
              {subhuertosLista.map((s) => {
                const seleccionado = s.id === subhuertoSeleccionadoId
                return (
                  <button
                    key={s.id}
                    type="button"
                    className={`siembra-opcion-btn ${seleccionado ? 'seleccionado' : ''}`}
                    onClick={() => setSubhuertoSeleccionadoId(s.id)}
                  >
                    <span className="siembra-opcion-nombre">{s.nombreZona}</span>
                    <span className="siembra-opcion-meta">{s.areaM2} m² • {s.subtipo}</span>
                  </button>
                )
              })}
            </div>
          </section>

          <section className="siembra-tarjeta">
            <h3 className="siembra-tarjeta-titulo">
              <span className="siembra-tarjeta-numero">2</span> Lote de bodega
            </h3>
            <div className="siembra-opciones-grid">
              {materialesLista.map((m) => {
                const seleccionado = m.id === materialSeleccionadoId
                const planta = plantasMap[m.plantaId]
                return (
                  <button
                    key={m.id}
                    type="button"
                    className={`siembra-opcion-btn ${seleccionado ? 'seleccionado' : ''}`}
                    onClick={() => setMaterialSeleccionadoId(m.id)}
                  >
                    <span className="siembra-opcion-nombre">{m.nombreComunPlanta}</span>
                    <span className="siembra-opcion-meta">
                      Stock: {m.cantidadDisponible} {m.unidadMedida}
                      {planta?.cualidadTermica && (
                        <span
                          className={
                            planta.cualidadTermica === 'CALIENTE'
                              ? 'siembra-termica-rojo'
                              : 'siembra-termica-verde'
                          }
                        >
                          {planta.cualidadTermica}
                        </span>
                      )}
                    </span>
                  </button>
                )
              })}
            </div>
          </section>

          <section className="siembra-tarjeta">
            <h3 className="siembra-tarjeta-titulo">
              <span className="siembra-tarjeta-numero">3</span> Cantidad y fecha
            </h3>
            <div className="siembra-campos-fila">
              <CampoTexto
                etiqueta="Cantidad a sembrar"
                type="number"
                step="0.01"
                min="0.01"
                valor={cantidadSembrada}
                alCambiar={(val) => setCantidadSembrada(val)}
                required
              />
              <CampoTexto
                etiqueta="Fecha de siembra"
                type="date"
                valor={fechaSiembra}
                alCambiar={(val) => setFechaSiembra(val)}
                required
              />
            </div>
            {esStockInsuficiente && (
              <div className="siembra-error-alerta">
                Stock insuficiente en bodega (disponible: {stockDisponible})
              </div>
            )}
          </section>

          <section className="siembra-tarjeta">
            <h3 className="siembra-tarjeta-titulo">
              <span className="siembra-tarjeta-numero">4</span> Fase lunar de siembra
            </h3>
            <div className="siembra-fases-grid">
              {FASES_LUNARES.map((f) => {
                const seleccionado = f.clave === faseLunar
                return (
                  <button
                    key={f.clave}
                    type="button"
                    className={`siembra-fase-btn ${seleccionado ? 'seleccionado' : ''}`}
                    onClick={() => setFaseLunar(f.clave)}
                    title={f.descripcion}
                  >
                    {f.etiqueta}
                  </button>
                )
              })}
            </div>
          </section>

          <div className="siembra-acciones-fijas-movil">
            <Boton
              type="submit"
              variante="primario"
              disabled={
                enviando ||
                esStockInsuficiente ||
                !subhuertoSeleccionadoId ||
                !materialSeleccionadoId ||
                cantidadNum <= 0
              }
            >
              Confirmar Siembra
            </Boton>
          </div>
        </form>

        <aside className="siembra-columna-panel" data-testid="panel-agronomico">
          <div className="siembra-panel-agronomico">
            <div className="siembra-panel-header">
              <KintiGuia variante="sabiduria" tamano="sm" />
              <h3>Validación Agronómica</h3>
            </div>

            <div className="siembra-panel-seccion">
              <span className="siembra-panel-seccion-titulo">Desglose de inventario en bodega</span>
              <div className="siembra-desglose-stock">
                <div className="siembra-desglose-linea">
                  <span>Stock disponible: {stockDisponible}</span>
                </div>
                <div className="siembra-desglose-linea">
                  <span>A sembrar: {cantidadNum}</span>
                </div>
                <div className="siembra-desglose-linea restante">
                  <span>Saldo restante: {saldoRestante}</span>
                </div>
              </div>
            </div>

            <div className="siembra-panel-seccion">
              <span className="siembra-panel-seccion-titulo">Compatibilidad de Altitud</span>
              <div className="siembra-barra-altitud-wrapper">
                <div className="siembra-barra-pistas">
                  <span>Huerto: {altitudHuerto} msnm</span>
                  <span>
                    Rango óptimo: {plantaSeleccionada?.altitudMinMsnm ?? 0} -{' '}
                    {plantaSeleccionada?.altitudMaxMsnm ?? 'Sin límite'} msnm
                  </span>
                </div>
                <div className="siembra-barra-altitud">
                  <div
                    className="siembra-barra-indicador"
                    style={{
                      left: `${Math.min(100, Math.max(0, ((altitudHuerto - 500) / 3500) * 100))}%`,
                    }}
                  />
                </div>
                <div>
                  {fueraDeRangoAltitud ? (
                    <span className="siembra-termica-rojo">Fuera de rango óptimo</span>
                  ) : (
                    <span className="siembra-termica-verde">Altitud óptima</span>
                  )}
                </div>
              </div>
            </div>

            <div className="siembra-panel-seccion">
              <span className="siembra-panel-seccion-titulo">Cualidad Térmica Ancestral</span>
              <div>
                {plantaSeleccionada?.cualidadTermica ? (
                  <span
                    className={
                      plantaSeleccionada.cualidadTermica === 'CALIENTE'
                        ? 'siembra-termica-rojo'
                        : 'siembra-termica-verde'
                    }
                  >
                    {plantaSeleccionada.cualidadTermica === 'CALIENTE'
                      ? 'ROJO_CALIENTE (CALIENTE)'
                      : 'VERDE_BLANCO_FRIA (FRIA)'}
                  </span>
                ) : (
                  <span style={{ fontSize: '0.875rem', color: 'var(--color-texto-secundario)' }}>
                    Selecciona un lote de bodega
                  </span>
                )}
              </div>
            </div>
          </div>
        </aside>
      </div>

      {alertaAltitudAbierta && (
        <div
          className="siembra-alerta-backdrop"
          role="dialog"
          aria-modal="true"
          aria-labelledby={idAlerta}
        >
          <div className="siembra-alerta-contenedor">
            <div className="siembra-alerta-kinti">
              <KintiGuia variante="alerta" tamano="md" />
            </div>
            <h3 id={idAlerta} className="siembra-alerta-titulo">
              Alerta de Altitud
            </h3>
            <p className="siembra-alerta-mensaje">
              La altitud de tu huerto ({altitudHuerto} msnm) se encuentra fuera del rango óptimo para{' '}
              {plantaSeleccionada?.nombreComun} ({plantaSeleccionada?.altitudMinMsnm ?? 0} -{' '}
              {plantaSeleccionada?.altitudMaxMsnm ?? 'máx'} msnm).
            </p>
            <p className="siembra-alerta-submensaje">
              Sembrar fuera del piso altitudinal recomendado puede afectar la germinación, desarrollo o
              resistencia del cultivo medicinal.
            </p>
            <div className="siembra-alerta-acciones">
              <Boton
                type="button"
                variante="secundario"
                onClick={() => setAlertaAltitudAbierta(false)}
              >
                Cancelar
              </Boton>
              <Boton
                type="button"
                variante="primario"
                onClick={() => ejecutarEnvio(true)}
              >
                Sembrar de todos modos
              </Boton>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
