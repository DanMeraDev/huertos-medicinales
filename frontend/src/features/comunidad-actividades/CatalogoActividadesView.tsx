import { useMemo, useState } from 'react'
import { Boton } from '../../components/Boton'
import { CampoTexto } from '../../components/CampoTexto'
import { Insignia, type InsigniaProps } from '../../components/Insignia'
import { Modal } from '../../components/Modal'
import { Notificacion } from '../../components/Notificacion'
import { Tabla, type ColumnaTabla } from '../../components/Tabla'
import {
  type ActividadCatalogo,
  type CategoriaActividad,
  type CrearActividadCatalogoPayload,
} from './actividadesCatalogoApi'
import './CatalogoActividadesView.css'

interface InfoCategoria {
  valor: CategoriaActividad
  etiqueta: string
  icono: string
  tono: InsigniaProps['tono']
}

const CATEGORIAS: InfoCategoria[] = [
  { valor: 'LIMPIEZA_DESHIERBE', etiqueta: 'Limpieza y Deshierbe', icono: '🌿', tono: 'exito' },
  { valor: 'RIEGO', etiqueta: 'Riego', icono: '💧', tono: 'neutro' },
  { valor: 'ABONO_COMPOST', etiqueta: 'Abono y Compost', icono: '🌱', tono: 'advertencia' },
  { valor: 'REPLANTE_APORQUE', etiqueta: 'Replante y Aporque', icono: '🪴', tono: 'exito' },
  { valor: 'CUIDADO_GENERAL', etiqueta: 'Cuidado General', icono: '🛠️', tono: 'neutro' },
]

export interface CatalogoActividadesViewProps {
  actividadesIniciales: ActividadCatalogo[]
  cargando?: boolean
  alCrear: (payload: CrearActividadCatalogoPayload) => Promise<ActividadCatalogo>
  alNavegarRegistroLabor?: () => void
}

export function CatalogoActividadesView({
  actividadesIniciales,
  cargando = false,
  alCrear,
  alNavegarRegistroLabor,
}: CatalogoActividadesViewProps) {
  const [actividades, setActividades] = useState<ActividadCatalogo[]>(actividadesIniciales)
  const [filtroCategoria, setFiltroCategoria] = useState<CategoriaActividad | 'TODAS'>('TODAS')
  const [ordenAscendente, setOrdenAscendente] = useState(true)
  const [dialogoAbierto, setDialogoAbierto] = useState(false)
  const [nombre, setNombre] = useState('')
  const [categoria, setCategoria] = useState<CategoriaActividad>('LIMPIEZA_DESHIERBE')
  const [errorCampo, setErrorCampo] = useState<string | undefined>(undefined)
  const [guardando, setGuardando] = useState(false)
  const [mensajeExito, setMensajeExito] = useState<string | null>(null)

  const infoPorCategoria = (cat: CategoriaActividad): InfoCategoria =>
    CATEGORIAS.find((c) => c.valor === cat) ?? CATEGORIAS[0]

  const actividadesFiltradas = useMemo(() => {
    const base =
      filtroCategoria === 'TODAS'
        ? [...actividades]
        : actividades.filter((a) => a.categoria === filtroCategoria)

    return base.sort((a, b) =>
      ordenAscendente ? a.nombre.localeCompare(b.nombre) : b.nombre.localeCompare(a.nombre)
    )
  }, [actividades, filtroCategoria, ordenAscendente])

  const abrirDialogo = () => {
    setNombre('')
    setCategoria('LIMPIEZA_DESHIERBE')
    setErrorCampo(undefined)
    setDialogoAbierto(true)
  }

  const cerrarDialogo = () => {
    if (guardando) return
    setDialogoAbierto(false)
    setErrorCampo(undefined)
  }

  const manejarGuardar = async () => {
    const limpio = nombre.trim()
    if (!limpio) {
      setErrorCampo('Ingresa el nombre de la labor cotidiana')
      return
    }
    if (limpio.length > 120) {
      setErrorCampo('El nombre no puede superar los 120 caracteres')
      return
    }

    setGuardando(true)
    setErrorCampo(undefined)
    try {
      const creada = await alCrear({ nombre: limpio, categoria })
      setActividades((prev) => [...prev, creada])
      setDialogoAbierto(false)
      setMensajeExito(`Actividad "${creada.nombre}" agregada al catálogo comunitario.`)
    } catch (err) {
      const msg =
        err instanceof Error
          ? err.message
          : 'Ya existe una actividad con este nombre en tu comunidad'
      setErrorCampo(msg)
    } finally {
      setGuardando(false)
    }
  }

  const columnas: ColumnaTabla<ActividadCatalogo>[] = [
    {
      titulo: 'Labor',
      celda: (fila) => {
        const info = infoPorCategoria(fila.categoria)
        return (
          <span>
            <span aria-hidden="true">{info.icono} </span>
            <strong>{fila.nombre}</strong>
          </span>
        )
      },
    },
    {
      titulo: 'Categoría',
      celda: (fila) => {
        const info = infoPorCategoria(fila.categoria)
        return <Insignia texto={info.etiqueta} tono={info.tono} />
      },
    },
    {
      titulo: 'Puntaje Base',
      celda: (fila) => <Insignia texto={`${fila.puntosPorHora} pt / hora`} tono="exito" />,
    },
    {
      titulo: 'Estado',
      celda: (fila) => (
        <Insignia
          texto={fila.activa ? 'Activa' : 'Inactiva'}
          tono={fila.activa ? 'exito' : 'neutro'}
        />
      ),
    },
  ]

  return (
    <section className="catalogo-actividades">
      <div className="catalogo-actividades__cabecera">
        <div>
          <h1 className="catalogo-actividades__titulo">Catálogo de Labores Comunitarias</h1>
          <p className="catalogo-actividades__subtitulo">
            Tareas cotidianas de cuidado del huerto (1 punto por hora, sin aval MSP).
          </p>
        </div>
        <div className="catalogo-actividades__accion-pc">
          <Boton
            variante="secundario"
            onClick={alNavegarRegistroLabor ?? (() => { window.location.assign('/comunidad/actividades-miembros/nueva') })}
          >
            Registrar Labor de Voluntario
          </Boton>
          <Boton variante="primario" onClick={abrirDialogo}>
            + Nueva Actividad Cotidiana
          </Boton>
        </div>
      </div>

      {mensajeExito && (
        <div style={{ marginBottom: '1rem' }}>
          <Notificacion tipo="exito" mensaje={mensajeExito} />
        </div>
      )}

      <div className="catalogo-actividades__filtros" role="group" aria-label="Filtrar por categoría">
        <button
          type="button"
          className={`catalogo-actividades__chip ${filtroCategoria === 'TODAS' ? 'catalogo-actividades__chip--activo' : ''}`}
          onClick={() => setFiltroCategoria('TODAS')}
        >
          Todas ({actividades.length})
        </button>
        {CATEGORIAS.map((cat) => (
          <button
            key={cat.valor}
            type="button"
            className={`catalogo-actividades__chip ${filtroCategoria === cat.valor ? 'catalogo-actividades__chip--activo' : ''}`}
            onClick={() => setFiltroCategoria(cat.valor)}
          >
            {cat.icono} {cat.etiqueta}
          </button>
        ))}
        <button
          type="button"
          className="catalogo-actividades__chip"
          onClick={() => setOrdenAscendente((prev) => !prev)}
        >
          Orden: {ordenAscendente ? 'A–Z' : 'Z–A'}
        </button>
      </div>

      {/* Vista Móvil: Tarjetas apiladas */}
      <div className="catalogo-actividades__lista-movil">
        {actividadesFiltradas.length === 0 ? (
          <p>{cargando ? 'Cargando…' : 'Aún no hay labores cotidianas registradas en tu comunidad.'}</p>
        ) : (
          actividadesFiltradas.map((item) => {
            const info = infoPorCategoria(item.categoria)
            return (
              <article key={item.id} className="catalogo-actividades__tarjeta">
                <div className="catalogo-actividades__tarjeta-top">
                  <strong>
                    {info.icono} {item.nombre}
                  </strong>
                  <Insignia texto={`${item.puntosPorHora} pt / hora`} tono="exito" />
                </div>
                <div>
                  <Insignia texto={info.etiqueta} tono={info.tono} />{' '}
                  <Insignia texto={item.activa ? 'Activa' : 'Inactiva'} tono="neutro" />
                </div>
              </article>
            )
          })
        )}
      </div>

      {/* Vista Escritorio: Tabla estructurada */}
      <div className="catalogo-actividades__tabla-pc">
        <Tabla
          columnas={columnas}
          filas={actividadesFiltradas}
          claveFila={(fila) => fila.id}
          cargando={cargando}
        />
      </div>

      {/* Botón flotante móvil (FAB) */}
      <button
        type="button"
        className="catalogo-actividades__fab"
        aria-label="Nueva actividad cotidiana"
        onClick={abrirDialogo}
      >
        +
      </button>

      {/* Modal centrado (PC) / Bottom Sheet (Móvil) */}
      <Modal
        abierto={dialogoAbierto}
        titulo="Nueva Actividad Cotidiana"
        alCancelar={cerrarDialogo}
        acciones={
          <Boton variante="primario" onClick={manejarGuardar} disabled={guardando}>
            {guardando ? 'Guardando…' : 'Guardar Actividad'}
          </Boton>
        }
      >
        <div className="catalogo-actividades__form">
          <CampoTexto
            etiqueta="Nombre de la actividad"
            valor={nombre}
            alCambiar={setNombre}
            placeholder="Ej. Riego matutino de almácigos"
            error={errorCampo}
          />

          <div>
            <p style={{ margin: '0 0 0.5rem', fontWeight: 600 }}>Categoría de labor</p>
            <div className="catalogo-actividades__categorias-grid">
              {CATEGORIAS.map((cat) => (
                <button
                  key={cat.valor}
                  type="button"
                  className={`catalogo-actividades__cat-btn ${
                    categoria === cat.valor ? 'catalogo-actividades__cat-btn--seleccionado' : ''
                  }`}
                  onClick={() => setCategoria(cat.valor)}
                >
                  <span>
                    {cat.icono} {cat.etiqueta}
                  </span>
                  {categoria === cat.valor && <span>✓</span>}
                </button>
              ))}
            </div>
          </div>

          <div className="catalogo-actividades__regla-puntos">
            Puntaje fijo comunitario: <strong>1 pt / hora</strong> (habilita insignias propias en la
            comunidad sin requerir validación del MSP).
          </div>
        </div>
      </Modal>
    </section>
  )
}