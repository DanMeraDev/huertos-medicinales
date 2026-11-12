import { useEffect, useRef, useState, type PointerEvent } from 'react'
import { ApiError } from '../../api/client'
import { Boton } from '../../components/Boton'
import { CampoTexto } from '../../components/CampoTexto'
import { Notificacion } from '../../components/Notificacion'
import { Selector } from '../../components/Selector'
import {
  actualizarDatosOperativos,
  obtenerHuertoComunidad,
  type ActualizarHuertoRequest,
  type DisenoPrincipal,
  type FuenteAgua,
  type HuertoDetalleResponse,
  type TipoAbono,
  type TipoCerca,
  type TipoSuelo,
} from './huertoApi'
import './ConfiguracionHuertoView.css'

type PestanaMovil = 'terreno' | 'suelo' | 'cerramiento'

interface OpcionVisual<T extends string> {
  valor: T
  etiqueta: string
  descripcion: string
}

const OPCIONES_ORIENTACION = [
  { valor: 'ESTE_OESTE', etiqueta: 'Este - Oeste (máxima insolación)' },
  { valor: 'NORTE_SUR', etiqueta: 'Norte - Sur (iluminación uniforme)' },
]

const OPCIONES_DISENO: OpcionVisual<DisenoPrincipal>[] = [
  { valor: 'EN_TIERRA', etiqueta: 'En Tierra', descripcion: 'Camas y surcos directos' },
  { valor: 'VERTICAL', etiqueta: 'Vertical', descripcion: 'Estructuras escalonadas' },
  { valor: 'COLGANTE', etiqueta: 'Colgante', descripcion: 'Macetas aéreas optimizadas' },
  { valor: 'EN_PISO', etiqueta: 'En Piso', descripcion: 'Contenedores sobre superficie' },
]

const OPCIONES_SUELO: OpcionVisual<TipoSuelo>[] = [
  { valor: 'ARCILLOSO', etiqueta: 'Arcilloso', descripcion: 'Alta retención de humedad' },
  { valor: 'LIMOSO', etiqueta: 'Limoso', descripcion: 'Fértil y de textura fina' },
  { valor: 'ARENOSO', etiqueta: 'Arenoso', descripcion: 'Drenaje rápido y aireado' },
  { valor: 'MARGOSO', etiqueta: 'Margoso', descripcion: 'Equilibrio franco ideal' },
  { valor: 'GREDOSO', etiqueta: 'Gredoso', descripcion: 'Rico en minerales calcáreos' },
  { valor: 'PANTANOSO', etiqueta: 'Pantanoso', descripcion: 'Alta materia orgánica húmeda' },
]

const OPCIONES_AGUA: OpcionVisual<FuenteAgua>[] = [
  { valor: 'RIO', etiqueta: 'Río', descripcion: 'Cauce natural cercano' },
  { valor: 'CANAL', etiqueta: 'Canal', descripcion: 'Acequia comunitaria' },
  { valor: 'GOTEO', etiqueta: 'Goteo', descripcion: 'Riego localizado eficiente' },
  { valor: 'VERTIENTE_LLUVIA', etiqueta: 'Vertiente / Lluvia', descripcion: 'Cosecha pluvial o vertiente' },
  { valor: 'GRIFO_REPOSADO', etiqueta: 'Grifo Reposado', descripcion: 'Agua declorada en tanque' },
]

const OPCIONES_ABONO: OpcionVisual<TipoAbono>[] = [
  { valor: 'BARBECHO', etiqueta: 'Barbecho', descripcion: 'Descanso y regeneración natural' },
  { valor: 'RESTOS_VEGETALES', etiqueta: 'Restos Vegetales', descripcion: 'Acolchado orgánico vegetal' },
  { valor: 'HOJAS_NITROGENO', etiqueta: 'Hojas Nitrógeno', descripcion: 'Hojarasca verde enriquecida' },
  { valor: 'COMPOST', etiqueta: 'Compost', descripcion: 'Humus maduro aeróbico' },
  { valor: 'PURIN_LIQUIDO', etiqueta: 'Purín Líquido', descripcion: 'Biofertilizante fermentado' },
  { valor: 'ESTIERCOL_SECADO', etiqueta: 'Estiércol Secado', descripcion: 'Abono animal curado' },
]

const OPCIONES_CERCA: OpcionVisual<TipoCerca>[] = [
  { valor: 'CERCA_VIVA', etiqueta: 'Cerca Viva', descripcion: 'Arbustos y plantas protectoras' },
  { valor: 'MATERIAL_NATURAL', etiqueta: 'Material Natural', descripcion: 'Madera, caña guadúa o piedra' },
  { valor: 'MATERIAL_RECICLADO', etiqueta: 'Material Reciclado', descripcion: 'Reutilización comunitaria' },
  { valor: 'MATERIAL_METALICO', etiqueta: 'Material Metálico', descripcion: 'Malla o cerramiento metálico' },
  { valor: 'CONVENCIONAL', etiqueta: 'Convencional', descripcion: 'Postes y alambre tradicional' },
]

const LATITUD_BASE = -0.180653
const LONGITUD_BASE = -78.467834

function calcularPosicionPin(latitudTexto: string, longitudTexto: string) {
  const lat = Number(latitudTexto)
  const lon = Number(longitudTexto)
  if (Number.isNaN(lat) || Number.isNaN(lon)) {
    return { x: 50, y: 50 }
  }
  const x = Math.min(92, Math.max(8, 50 + (lon - LONGITUD_BASE) * 900))
  const y = Math.min(88, Math.max(18, 50 - (lat - LATITUD_BASE) * 900))
  return { x, y }
}

export function ConfiguracionHuertoView() {
  const [cargando, setCargando] = useState(true)
  const [guardando, setGuardando] = useState(false)
  const [pestanaActiva, setPestanaActiva] = useState<PestanaMovil>('terreno')
  const [huerto, setHuerto] = useState<HuertoDetalleResponse | null>(null)
  const [notificacion, setNotificacion] = useState<{ tipo: 'exito' | 'error'; mensaje: string } | null>(null)

  const [latitud, setLatitud] = useState('-0.180653')
  const [longitud, setLongitud] = useState('-78.467834')
  const [altitudMsnm, setAltitudMsnm] = useState('2800')
  const [orientacion, setOrientacion] = useState('ESTE_OESTE')
  const [areaTotalM2, setAreaTotalM2] = useState('150')
  const [disenoPrincipal, setDisenoPrincipal] = useState<DisenoPrincipal>('EN_TIERRA')
  const [tipoSuelo, setTipoSuelo] = useState<TipoSuelo | null>(null)
  const [fuenteAgua, setFuenteAgua] = useState<FuenteAgua | null>(null)
  const [tipoAbono, setTipoAbono] = useState<TipoAbono | null>(null)
  const [tipoCerca, setTipoCerca] = useState<TipoCerca | null>(null)
  const [detalleCerca, setDetalleCerca] = useState('')

  const [arrastrandoPin, setArrastrandoPin] = useState(false)
  const visorRef = useRef<HTMLDivElement | null>(null)

  useEffect(() => {
    let activo = true
    obtenerHuertoComunidad()
      .then((datos) => {
        if (!activo) return
        setHuerto(datos)
        if (datos.latitud !== null) setLatitud(String(datos.latitud))
        if (datos.longitud !== null) setLongitud(String(datos.longitud))
        if (datos.altitudMsnm !== null) setAltitudMsnm(String(datos.altitudMsnm))
        if (datos.orientacion) setOrientacion(datos.orientacion)
        if (datos.areaTotalM2 !== null) setAreaTotalM2(String(datos.areaTotalM2))
        if (datos.disenoPrincipal) setDisenoPrincipal(datos.disenoPrincipal)
        setTipoSuelo(datos.tipoSuelo)
        setFuenteAgua(datos.fuenteAgua)
        setTipoAbono(datos.tipoAbono)
        setTipoCerca(datos.tipoCerca)
        setDetalleCerca(datos.detalleCerca ?? '')
      })
      .catch((error: unknown) => {
        if (!activo) return
        const mensaje =
          error instanceof ApiError && error.detalle
            ? error.detalle
            : 'No se pudo cargar la configuración del huerto'
        setNotificacion({ tipo: 'error', mensaje })
      })
      .finally(() => {
        if (activo) setCargando(false)
      })

    return () => {
      activo = false
    }
  }, [])

  const areaNumerica = Number(areaTotalM2)
  const areaOcupada = huerto?.areaOcupadaSubhuertosM2 ?? 0
  const areaEnRangoMsp = !Number.isNaN(areaNumerica) && areaNumerica >= 16 && areaNumerica <= 400
  const areaCompatibleSubhuertos = !Number.isNaN(areaNumerica) && areaNumerica >= areaOcupada
  const porcentajeMedidor = Number.isNaN(areaNumerica)
    ? 0
    : Math.min(100, Math.max(0, (areaNumerica / 400) * 100))

  function actualizarCoordenadasDesdeVisor(clienteX: number, clienteY: number) {
    const visor = visorRef.current
    if (!visor) return
    const rect = visor.getBoundingClientRect()
    if (rect.width === 0 || rect.height === 0) return

    const proporcionX = Math.min(1, Math.max(0, (clienteX - rect.left) / rect.width))
    const proporcionY = Math.min(1, Math.max(0, (clienteY - rect.top) / rect.height))
    const nuevaLongitud = LONGITUD_BASE + (proporcionX - 0.5) / 9
    const nuevaLatitud = LATITUD_BASE - (proporcionY - 0.5) / 9

    setLatitud(nuevaLatitud.toFixed(6))
    setLongitud(nuevaLongitud.toFixed(6))
  }

  function manejarPointerDown(evento: PointerEvent<HTMLDivElement>) {
    setArrastrandoPin(true)
    actualizarCoordenadasDesdeVisor(evento.clientX, evento.clientY)
  }

  function manejarPointerMove(evento: PointerEvent<HTMLDivElement>) {
    if (!arrastrandoPin) return
    actualizarCoordenadasDesdeVisor(evento.clientX, evento.clientY)
  }

  function manejarPointerUp() {
    setArrastrandoPin(false)
  }

  function sincronizarGps() {
    if (!navigator.geolocation) {
      setNotificacion({ tipo: 'error', mensaje: 'Tu navegador no soporta geolocalización GPS' })
      return
    }
    navigator.geolocation.getCurrentPosition(
      (posicion) => {
        setLatitud(posicion.coords.latitude.toFixed(6))
        setLongitud(posicion.coords.longitude.toFixed(6))
        if (posicion.coords.altitude !== null) {
          setAltitudMsnm(String(Math.round(posicion.coords.altitude)))
        }
        setNotificacion({ tipo: 'exito', mensaje: 'Coordenadas GPS sincronizadas correctamente' })
      },
      () => {
        setNotificacion({ tipo: 'error', mensaje: 'No se pudo obtener la ubicación GPS del dispositivo' })
      },
    )
  }

  async function manejarGuardar() {
    if (!areaEnRangoMsp) {
      setNotificacion({
        tipo: 'error',
        mensaje: 'El huerto debe medir entre 16.00 m² y 400.00 m² según lineamientos MSP',
      })
      return
    }

    if (!areaCompatibleSubhuertos) {
      setNotificacion({
        tipo: 'error',
        mensaje: `No puedes reducir a ${areaNumerica} m² porque tus subhuertos ocupan ${areaOcupada} m²`,
      })
      return
    }

    const latNumero = Number(latitud)
    const lonNumero = Number(longitud)
    const altNumero = Number(altitudMsnm)

    if (Number.isNaN(latNumero) || Number.isNaN(lonNumero) || Number.isNaN(altNumero) || altNumero < 0) {
      setNotificacion({
        tipo: 'error',
        mensaje: 'Verifica que la latitud, longitud y altitud sean valores numéricos válidos',
      })
      return
    }

    const payload: ActualizarHuertoRequest = {
      latitud: latNumero,
      longitud: lonNumero,
      altitudMsnm: Math.trunc(altNumero),
      orientacion: orientacion || null,
      areaTotalM2: areaNumerica,
      disenoPrincipal,
      tipoSuelo,
      fuenteAgua,
      tipoAbono,
      tipoCerca,
      detalleCerca: detalleCerca.trim() || null,
    }

    setGuardando(true)
    setNotificacion(null)

    try {
      const actualizado = await actualizarDatosOperativos(payload)
      setHuerto(actualizado)
      setNotificacion({ tipo: 'exito', mensaje: 'Configuración del huerto actualizada con éxito' })
    } catch (error: unknown) {
      const mensaje =
        error instanceof ApiError && error.detalle
          ? error.detalle
          : 'No se pudo guardar la configuración del huerto'
      setNotificacion({ tipo: 'error', mensaje })
    } finally {
      setGuardando(false)
    }
  }

  const posicionPin = calcularPosicionPin(latitud, longitud)

  if (cargando) {
    return (
      <section className="huerto-config" aria-busy="true">
        <p>Cargando configuración del huerto…</p>
      </section>
    )
  }

  return (
    <section className="huerto-config">
      <header className="huerto-config-cabecera">
        <div>
          <h1 className="huerto-config-titulo">
            {huerto?.nombreHuerto ? `Configuración de ${huerto.nombreHuerto}` : 'Configuración Operativa del Huerto'}
          </h1>
          <p className="huerto-config-subtitulo">
            Parámetros territoriales, agronómicos y de cerramiento según normativa MSP
          </p>
        </div>
        {huerto && <span className="huerto-config-insignia">Nivel {huerto.tipoHuerto}</span>}
      </header>

      {notificacion && (
        <div className="huerto-config-notificacion">
          <Notificacion tipo={notificacion.tipo} mensaje={notificacion.mensaje} />
        </div>
      )}

      <div className="huerto-tabs" role="tablist" aria-label="Secciones de configuración del huerto">
        <button
          type="button"
          role="tab"
          aria-selected={pestanaActiva === 'terreno'}
          className={pestanaActiva === 'terreno' ? 'huerto-tab huerto-tab-activa' : 'huerto-tab'}
          onClick={() => setPestanaActiva('terreno')}
        >
          1. Terreno y Área
        </button>
        <button
          type="button"
          role="tab"
          aria-selected={pestanaActiva === 'suelo'}
          className={pestanaActiva === 'suelo' ? 'huerto-tab huerto-tab-activa' : 'huerto-tab'}
          onClick={() => setPestanaActiva('suelo')}
        >
          2. Suelo y Agua
        </button>
        <button
          type="button"
          role="tab"
          aria-selected={pestanaActiva === 'cerramiento'}
          className={pestanaActiva === 'cerramiento' ? 'huerto-tab huerto-tab-activa' : 'huerto-tab'}
          onClick={() => setPestanaActiva('cerramiento')}
        >
          3. Cerramiento
        </button>
      </div>

      <div className="huerto-split" data-testid="layout-split-huerto">
        <div className={pestanaActiva === 'terreno' ? 'huerto-columna' : 'huerto-columna huerto-seccion-oculta'}>
          <article className="huerto-tarjeta">
            <h2 className="huerto-tarjeta-titulo">Georreferenciación del Terreno</h2>
            <p className="huerto-tarjeta-descripcion">
              Arrastra el marcador sobre el visor o sincroniza el GPS en sitio
            </p>

            <div
              ref={visorRef}
              className="huerto-visor-mapa"
              role="application"
              aria-label="Mapa interactivo de ubicación del huerto"
              onPointerDown={manejarPointerDown}
              onPointerMove={manejarPointerMove}
              onPointerUp={manejarPointerUp}
              onPointerLeave={manejarPointerUp}
            >
              <span className="huerto-visor-etiqueta">
                {latitud}°, {longitud}°
              </span>
              <div className="huerto-pin" style={{ left: `${posicionPin.x}%`, top: `${posicionPin.y}%` }}>
                <span className="huerto-pin-cabeza" />
                <span className="huerto-pin-punta" />
              </div>
              <div className="huerto-visor-pie" onPointerDown={(e) => e.stopPropagation()}>
                <Boton variante="secundario" onClick={sincronizarGps}>
                  Capturar GPS
                </Boton>
              </div>
            </div>

            <div className="huerto-coordenadas-grid">
              <CampoTexto
                etiqueta="Latitud"
                type="number"
                step="0.000001"
                valor={latitud}
                alCambiar={setLatitud}
              />
              <CampoTexto
                etiqueta="Longitud"
                type="number"
                step="0.000001"
                valor={longitud}
                alCambiar={setLongitud}
              />
              <CampoTexto
                etiqueta="Altitud (msnm)"
                type="number"
                min="0"
                valor={altitudMsnm}
                alCambiar={setAltitudMsnm}
              />
            </div>
          </article>

          <article className="huerto-tarjeta">
            <h2 className="huerto-tarjeta-titulo">Superficie y Diseño Principal</h2>
            <p className="huerto-tarjeta-descripcion">
              Rango normativo MSP permitido: entre 16.00 m² y 400.00 m²
            </p>

            <CampoTexto
              etiqueta="Área Total (m²)"
              type="number"
              step="0.01"
              valor={areaTotalM2}
              alCambiar={setAreaTotalM2}
              error={
                !areaEnRangoMsp
                  ? 'El huerto debe medir entre 16.00 m² y 400.00 m² según lineamientos MSP'
                  : !areaCompatibleSubhuertos
                    ? `No puedes reducir a ${areaNumerica} m² porque tus subhuertos ocupan ${areaOcupada} m²`
                    : undefined
              }
            />

            <div className="huerto-medidor-caja">
              <div className="huerto-medidor-encabezado">
                <span>Cumplimiento Normativo MSP</span>
                <span
                  className={
                    areaEnRangoMsp && areaCompatibleSubhuertos
                      ? 'huerto-medidor-texto-valido'
                      : 'huerto-medidor-texto-invalido'
                  }
                >
                  {areaEnRangoMsp && areaCompatibleSubhuertos ? 'Dentro de norma (16 - 400 m²)' : 'Fuera de norma'}
                </span>
              </div>
              <div className="huerto-medidor-pista">
                <div
                  data-testid="barra-medidor-msp"
                  className={
                    areaEnRangoMsp && areaCompatibleSubhuertos
                      ? 'huerto-medidor-barra huerto-medidor-valido'
                      : 'huerto-medidor-barra huerto-medidor-invalido'
                  }
                  style={{ width: `${porcentajeMedidor}%` }}
                />
              </div>
              <p className="huerto-medidor-nota">
                Área ocupada actualmente por subhuertos: {areaOcupada} m²
              </p>
            </div>

            <div className="huerto-grupo">
              <Selector
                etiqueta="Orientación Solar"
                opciones={OPCIONES_ORIENTACION}
                valor={orientacion}
                alCambiar={setOrientacion}
              />
            </div>

            <div className="huerto-grupo">
              <span className="huerto-grupo-etiqueta">Diseño Principal del Huerto</span>
              <div className="huerto-diseno-grid">
                {OPCIONES_DISENO.map((opcion) => (
                  <button
                    key={opcion.valor}
                    type="button"
                    aria-pressed={disenoPrincipal === opcion.valor}
                    className={
                      disenoPrincipal === opcion.valor ? 'huerto-chip huerto-chip-activo' : 'huerto-chip'
                    }
                    onClick={() => setDisenoPrincipal(opcion.valor)}
                  >
                    <span className="huerto-chip-titulo">{opcion.etiqueta}</span>
                    <span className="huerto-chip-detalle">{opcion.descripcion}</span>
                  </button>
                ))}
              </div>
            </div>
          </article>

          <div className="huerto-acciones-movil">
            <Boton onClick={() => setPestanaActiva('suelo')}>Siguiente</Boton>
            <Boton
              disabled={!areaEnRangoMsp || !areaCompatibleSubhuertos || guardando}
              onClick={manejarGuardar}
            >
              {guardando ? 'Guardando…' : 'Guardar Datos del Huerto'}
            </Boton>
          </div>
        </div>

        <div className="huerto-columna">
          <div className={pestanaActiva === 'suelo' ? undefined : 'huerto-seccion-oculta'}>
            <article className="huerto-tarjeta">
              <h2 className="huerto-tarjeta-titulo">Suelo y Fuentes de Agua</h2>
              <p className="huerto-tarjeta-descripcion">
                Selecciona las condiciones edáficas y el sistema de riego predominante
              </p>

              <div className="huerto-grupo">
                <span className="huerto-grupo-etiqueta">Tipo de Suelo</span>
                <div className="huerto-chips-grid">
                  {OPCIONES_SUELO.map((opcion) => (
                    <button
                      key={opcion.valor}
                      type="button"
                      aria-pressed={tipoSuelo === opcion.valor}
                      className={tipoSuelo === opcion.valor ? 'huerto-chip huerto-chip-activo' : 'huerto-chip'}
                      onClick={() => setTipoSuelo(tipoSuelo === opcion.valor ? null : opcion.valor)}
                    >
                      <span className="huerto-chip-titulo">{opcion.etiqueta}</span>
                      <span className="huerto-chip-detalle">{opcion.descripcion}</span>
                    </button>
                  ))}
                </div>
              </div>

              <div className="huerto-grupo">
                <span className="huerto-grupo-etiqueta">Fuente de Agua</span>
                <div className="huerto-chips-grid">
                  {OPCIONES_AGUA.map((opcion) => (
                    <button
                      key={opcion.valor}
                      type="button"
                      aria-pressed={fuenteAgua === opcion.valor}
                      className={fuenteAgua === opcion.valor ? 'huerto-chip huerto-chip-activo' : 'huerto-chip'}
                      onClick={() => setFuenteAgua(fuenteAgua === opcion.valor ? null : opcion.valor)}
                    >
                      <span className="huerto-chip-titulo">{opcion.etiqueta}</span>
                      <span className="huerto-chip-detalle">{opcion.descripcion}</span>
                    </button>
                  ))}
                </div>
              </div>
            </article>

            <article className="huerto-tarjeta" style={{ marginTop: 'var(--espacio-4)' }}>
              <h2 className="huerto-tarjeta-titulo">Abonos Naturales</h2>
              <p className="huerto-tarjeta-descripcion">
                Prácticas ancestrales y orgánicas de nutrición del suelo
              </p>

              <div className="huerto-chips-grid">
                {OPCIONES_ABONO.map((opcion) => (
                  <button
                    key={opcion.valor}
                    type="button"
                    aria-pressed={tipoAbono === opcion.valor}
                    className={tipoAbono === opcion.valor ? 'huerto-chip huerto-chip-activo' : 'huerto-chip'}
                    onClick={() => setTipoAbono(tipoAbono === opcion.valor ? null : opcion.valor)}
                  >
                    <span className="huerto-chip-titulo">{opcion.etiqueta}</span>
                    <span className="huerto-chip-detalle">{opcion.descripcion}</span>
                  </button>
                ))}
              </div>
            </article>

            <div className="huerto-acciones-movil">
              <Boton variante="secundario" onClick={() => setPestanaActiva('terreno')}>
                Anterior
              </Boton>
              <Boton onClick={() => setPestanaActiva('cerramiento')}>Siguiente</Boton>
            </div>
          </div>

          <div className={pestanaActiva === 'cerramiento' ? undefined : 'huerto-seccion-oculta'}>
            <article className="huerto-tarjeta">
              <h2 className="huerto-tarjeta-titulo">Cerramiento Perimetral</h2>
              <p className="huerto-tarjeta-descripcion">
                Protección física del huerto frente a animales o factores externos
              </p>

              <div className="huerto-chips-grid">
                {OPCIONES_CERCA.map((opcion) => (
                  <button
                    key={opcion.valor}
                    type="button"
                    aria-pressed={tipoCerca === opcion.valor}
                    className={tipoCerca === opcion.valor ? 'huerto-chip huerto-chip-activo' : 'huerto-chip'}
                    onClick={() => setTipoCerca(tipoCerca === opcion.valor ? null : opcion.valor)}
                  >
                    <span className="huerto-chip-titulo">{opcion.etiqueta}</span>
                    <span className="huerto-chip-detalle">{opcion.descripcion}</span>
                  </button>
                ))}
              </div>

              <div className="huerto-grupo">
                <CampoTexto
                  etiqueta="Detalle de Cerca o Especies Protectoras"
                  maxLength={255}
                  placeholder="Ej: Cerco vivo de cabuya, chilca y postes de eucalipto"
                  valor={detalleCerca}
                  alCambiar={setDetalleCerca}
                />
              </div>
            </article>

            <div className="huerto-acciones-movil">
              <Boton variante="secundario" onClick={() => setPestanaActiva('suelo')}>
                Anterior
              </Boton>
              <Boton
                disabled={!areaEnRangoMsp || !areaCompatibleSubhuertos || guardando}
                onClick={manejarGuardar}
              >
                {guardando ? 'Guardando…' : 'Guardar Datos del Huerto'}
              </Boton>
            </div>
          </div>

          <footer className="huerto-footer-desktop">
            <Boton
              className="huerto-boton-guardar-desktop"
              disabled={!areaEnRangoMsp || !areaCompatibleSubhuertos || guardando}
              onClick={manejarGuardar}
            >
              {guardando ? 'Guardando…' : 'Guardar Configuración del Huerto'}
            </Boton>
          </footer>
        </div>
      </div>
    </section>
  )
}