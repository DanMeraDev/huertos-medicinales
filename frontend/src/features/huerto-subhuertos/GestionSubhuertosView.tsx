import { useMemo, useState } from 'react'
import { Boton } from '../../components/Boton'
import { CampoTexto } from '../../components/CampoTexto'
import { Insignia } from '../../components/Insignia'
import { Modal } from '../../components/Modal'
import { Notificacion } from '../../components/Notificacion'
import {
  type CrearSubhuertoPayload,
  type Subhuerto,
  type SubtipoSubhuerto,
} from './subhuertosApi'
import './GestionSubhuertosView.css'

interface InfoSubtipo {
  valor: SubtipoSubhuerto
  etiqueta: string
  icono: string
}

const SUBTIPOS: InfoSubtipo[] = [
  { valor: 'SURCOS_WACHU', etiqueta: 'Surcos (Wachu)', icono: '🌾' },
  { valor: 'TERRAZAS_ANTI', etiqueta: 'Terrazas (Anti)', icono: '🏔️' },
  { valor: 'CAMELLONES_INKA_WACHU', etiqueta: 'Camellones (Inka Wachu)', icono: '🌱' },
  { valor: 'VERTICAL_PARED', etiqueta: 'Vertical en Pared', icono: '🧱' },
  { valor: 'COLGANTE_TECHO', etiqueta: 'Colgante en Techo', icono: '🧺' },
  { valor: 'MACETAS_PISO', etiqueta: 'Macetas en Piso', icono: '🪴' },
  { valor: 'LLANTAS_RECICLADAS', etiqueta: 'Llantas Recicladas', icono: '♻️' },
]

export interface GestionSubhuertosViewProps {
  areaTotalHuertoM2: number | null
  subhuertosIniciales: Subhuerto[]
  alCrear: (payload: CrearSubhuertoPayload) => Promise<Subhuerto>
}

export function GestionSubhuertosView({
  areaTotalHuertoM2,
  subhuertosIniciales,
  alCrear,
}: GestionSubhuertosViewProps) {
  const [subhuertos, setSubhuertos] = useState<Subhuerto[]>(subhuertosIniciales)
  const [dialogoAbierto, setDialogoAbierto] = useState(false)
  const [nombreZona, setNombreZona] = useState('')
  const [subtipo, setSubtipo] = useState<SubtipoSubhuerto>('SURCOS_WACHU')
  const [areaTexto, setAreaTexto] = useState('')
  const [errorNombre, setErrorNombre] = useState<string | undefined>(undefined)
  const [errorArea, setErrorArea] = useState<string | undefined>(undefined)
  const [errorGeneral, setErrorGeneral] = useState<string | null>(null)
  const [guardando, setGuardando] = useState(false)
  const [mensajeExito, setMensajeExito] = useState<string | null>(null)

  const infoPorSubtipo = (valor: SubtipoSubhuerto): InfoSubtipo =>
    SUBTIPOS.find((s) => s.valor === valor) ?? SUBTIPOS[0]

  const { areaOcupadaM2, areaDisponibleM2, porcentajeOcupado, subtiposUsados } = useMemo(() => {
    const ocupada = subhuertos.reduce((acc, item) => acc + Number(item.areaM2 || 0), 0)
    const total = areaTotalHuertoM2 ?? 0
    const libre = Math.max(0, total - ocupada)
    const pct = total > 0 ? Math.min(100, Math.round((ocupada / total) * 100)) : 0
    const distintos = Array.from(new Set(subhuertos.map((s) => s.subtipo)))
    return {
      areaOcupadaM2: ocupada,
      areaDisponibleM2: libre,
      porcentajeOcupado: pct,
      subtiposUsados: distintos,
    }
  }, [subhuertos, areaTotalHuertoM2])

  const limiteSubtiposAlcanzado = subtiposUsados.length >= 3

  const abrirDialogo = () => {
    setNombreZona('')
    const subtipoPorDefecto =
      limiteSubtiposAlcanzado && subtiposUsados.length > 0 ? subtiposUsados[0] : 'SURCOS_WACHU'
    setSubtipo(subtipoPorDefecto)
    setAreaTexto('')
    setErrorNombre(undefined)
    setErrorArea(undefined)
    setErrorGeneral(null)
    setDialogoAbierto(true)
  }

  const cerrarDialogo = () => {
    if (guardando) return
    setDialogoAbierto(false)
  }

  const manejarCrear = async () => {
    setErrorNombre(undefined)
    setErrorArea(undefined)
    setErrorGeneral(null)

    const nombreLimpio = nombreZona.trim()
    if (!nombreLimpio) {
      setErrorNombre('Ingresa el nombre de la zona')
      return
    }

    const areaNumero = Number(areaTexto)
    if (!areaTexto || Number.isNaN(areaNumero) || areaNumero <= 0) {
      setErrorArea('Ingresa un área válida mayor a 0 m²')
      return
    }

    if (areaTotalHuertoM2 !== null && areaNumero > areaDisponibleM2 + 0.0001) {
      setErrorArea('El área ingresada supera el espacio disponible del huerto')
      return
    }

    setGuardando(true)
    try {
      const creado = await alCrear({
        nombreZona: nombreLimpio,
        subtipo,
        areaM2: areaNumero,
      })
      setSubhuertos((prev) => [...prev, creado])
      setDialogoAbierto(false)
      setMensajeExito('Subhuerto creado exitosamente')
    } catch (err) {
      const mensaje =
        err instanceof Error ? err.message : 'No se pudo registrar el subhuerto'
      if (mensaje.toLowerCase().includes('espacio disponible')) {
        setErrorArea(mensaje)
      } else {
        setErrorGeneral(mensaje)
      }
    } finally {
      setGuardando(false)
    }
  }

  return (
    <section className="gestion-subhuertos">
      <div className="gestion-subhuertos__cabecera">
        <div>
          <h1 className="gestion-subhuertos__titulo">Zonificación y Subhuertos</h1>
          <p className="gestion-subhuertos__subtitulo">
            Superficie total del huerto:{' '}
            <strong>
              {areaTotalHuertoM2 !== null
                ? `${areaTotalHuertoM2.toFixed(2)} m²`
                : 'Pendiente de configurar (HR-CU03)'}
            </strong>
          </p>
        </div>
        <div className="gestion-subhuertos__accion-pc">
          <Boton variante="primario" onClick={abrirDialogo}>
            + Nuevo Subhuerto
          </Boton>
        </div>
      </div>

      {mensajeExito && (
        <div style={{ marginBottom: '1rem' }}>
          <Notificacion tipo="exito" mensaje={mensajeExito} />
        </div>
      )}

      {areaTotalHuertoM2 === null && (
        <div style={{ marginBottom: '1rem' }}>
          <Notificacion
            tipo="error"
            mensaje="Primero completa el área del huerto en la configuración operativa (HR-CU03)."
          />
        </div>
      )}

      {/* Dashboard superior de ocupación territorial */}
      <div className="gestion-subhuertos__dashboard">
        <div className="gestion-subhuertos__barra-info">
          <span>
            <strong>
              {areaOcupadaM2.toFixed(2)} m² ocupados de {(areaTotalHuertoM2 ?? 0).toFixed(2)} m²
            </strong>{' '}
            ({porcentajeOcupado}%)
          </span>
          <span>
            Espacio libre disponible: <strong>{areaDisponibleM2.toFixed(2)} m²</strong>
          </span>
        </div>

        <div
          className="gestion-subhuertos__barra-pista"
          role="progressbar"
          aria-valuenow={porcentajeOcupado}
          aria-valuemin={0}
          aria-valuemax={100}
        >
          <div
            className="gestion-subhuertos__barra-relleno"
            style={{ width: `${porcentajeOcupado}%` }}
          />
        </div>

        <div className="gestion-subhuertos__capsulas">
          <Insignia
            texto={`${subtiposUsados.length} / 3 subtipos MSP en uso`}
            tono={limiteSubtiposAlcanzado ? 'advertencia' : 'exito'}
          />
          {[0, 1, 2].map((indice) => {
            const usado = subtiposUsados[indice]
            if (usado) {
              const info = infoPorSubtipo(usado)
              return (
                <span
                  key={indice}
                  className="gestion-subhuertos__capsula gestion-subhuertos__capsula--ocupada"
                >
                  {info.icono} Cupo {indice + 1}: {info.etiqueta}
                </span>
              )
            }
            return (
              <span key={indice} className="gestion-subhuertos__capsula">
                Cupo {indice + 1}: Disponible
              </span>
            )
          })}
        </div>
      </div>

      {/* Cuadrícula de subhuertos (1 col móvil / 3 cols PC) */}
      {subhuertos.length === 0 ? (
        <p>Aún no has creado subhuertos. Pulsa el botón para definir la primera zona de cultivo.</p>
      ) : (
        <div className="gestion-subhuertos__grid">
          {subhuertos.map((item) => {
            const info = infoPorSubtipo(item.subtipo)
            return (
              <article key={item.id} className="gestion-subhuertos__tarjeta">
                <div className="gestion-subhuertos__tarjeta-top">
                  <div>
                    <span className="gestion-subhuertos__icono-botanico" aria-hidden="true">
                      {info.icono}
                    </span>
                    <h2 style={{ margin: '0.25rem 0 0', fontSize: '1.05rem' }}>{item.nombreZona}</h2>
                  </div>
                  <Insignia texto={`${Number(item.areaM2).toFixed(2)} m²`} tono="exito" />
                </div>

                <div>
                  <Insignia texto={info.etiqueta} tono="neutro" />
                </div>

                <div className="gestion-subhuertos__meta">
                  <span>Estado: {item.activo ? 'Activo' : 'Inactivo'}</span>
                  <span>Listo para siembra</span>
                </div>
              </article>
            )
          })}
        </div>
      )}

      {/* Botón flotante móvil (FAB) */}
      <button
        type="button"
        className="gestion-subhuertos__fab"
        aria-label="Nuevo subhuerto"
        onClick={abrirDialogo}
      >
        +
      </button>

      {/* Modal centrado (PC) / Bottom Sheet (Móvil) */}
      <Modal
        abierto={dialogoAbierto}
        titulo="Crear Nuevo Subhuerto"
        alCancelar={cerrarDialogo}
        acciones={
          <Boton variante="primario" onClick={manejarCrear} disabled={guardando}>
            {guardando ? 'Creando…' : 'Crear Subhuerto'}
          </Boton>
        }
      >
        <div className="gestion-subhuertos__form">
          {errorGeneral && <Notificacion tipo="error" mensaje={errorGeneral} />}

          <CampoTexto
            etiqueta="Nombre de la zona"
            valor={nombreZona}
            alCambiar={setNombreZona}
            placeholder="Ej. Surcos Norte de Manzanilla"
            error={errorNombre}
          />

          <div>
            <p style={{ margin: '0 0 0.45rem', fontWeight: 600 }}>Subtipo de cultivo</p>
            {limiteSubtiposAlcanzado && (
              <p className="gestion-subhuertos__aviso-msp" style={{ marginBottom: '0.5rem' }}>
                🔒 Máximo 3 subtipos por huerto según norma MSP
              </p>
            )}
            <div className="gestion-subhuertos__subtipos-grid">
              {SUBTIPOS.map((item) => {
                const yaEnUso = subtiposUsados.includes(item.valor)
                const bloqueado = limiteSubtiposAlcanzado && !yaEnUso
                return (
                  <button
                    key={item.valor}
                    type="button"
                    disabled={bloqueado}
                    className={`gestion-subhuertos__subtipo-btn ${
                      subtipo === item.valor ? 'gestion-subhuertos__subtipo-btn--activo' : ''
                    }`}
                    onClick={() => setSubtipo(item.valor)}
                  >
                    <span>
                      {item.icono} {item.etiqueta}
                    </span>
                    <span>{bloqueado ? '🔒' : subtipo === item.valor ? '✓' : ''}</span>
                  </button>
                )
              })}
            </div>
          </div>

          <CampoTexto
            etiqueta="Área de la zona (m²)"
            type="number"
            valor={areaTexto}
            alCambiar={setAreaTexto}
            placeholder={`Disponible: ${areaDisponibleM2.toFixed(2)} m²`}
            error={errorArea}
          />
        </div>
      </Modal>
    </section>
  )
}