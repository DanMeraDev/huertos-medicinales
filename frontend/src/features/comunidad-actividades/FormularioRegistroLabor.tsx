import { useEffect, useState, type FormEvent } from 'react'
import { Boton } from '../../components/Boton'
import { CampoTexto } from '../../components/CampoTexto'
import { Insignia } from '../../components/Insignia'
import { KintiGuia } from '../../components/KintiGuia'
import { Selector } from '../../components/Selector'
import { ModalCelebracionPuntos } from './ModalCelebracionPuntos'
import type {
  ActividadComunitaria,
  InsigniaMiembro,
  MiembroVoluntario,
  RegistrarLaborMiembroRequest,
  ResultadoAcreditacionMiembroResponse,
} from './registroLaboresApi'
import './FormularioRegistroLabor.css'

export interface FormularioRegistroLaborProps {
  miembros: MiembroVoluntario[]
  actividades: ActividadComunitaria[]
  alObtenerInsignias: (miembroId: number) => Promise<InsigniaMiembro[]>
  alRegistrar: (datos: RegistrarLaborMiembroRequest) => Promise<ResultadoAcreditacionMiembroResponse>
  cargando?: boolean
  alExito?: () => void
}

export function FormularioRegistroLabor({
  miembros,
  actividades,
  alObtenerInsignias,
  alRegistrar,
  cargando = false,
  alExito,
}: FormularioRegistroLaborProps) {
  const [fechaHoy] = useState(() => new Date().toISOString().split('T')[0])

  const [miembroId, setMiembroId] = useState('')
  const [actividadId, setActividadId] = useState('')
  const [fechaLabor, setFechaLabor] = useState(fechaHoy)
  const [horasTrabajadas, setHorasTrabajadas] = useState('1')
  const [observaciones, setObservaciones] = useState('')

  const [insignias, setInsignias] = useState<InsigniaMiembro[]>([])
  const [guardando, setGuardando] = useState(false)
  const [errorApi, setErrorApi] = useState<string | null>(null)
  const [resultadoCelebracion, setResultadoCelebracion] =
    useState<ResultadoAcreditacionMiembroResponse | null>(null)
  const [modalAbierto, setModalAbierto] = useState(false)

  const voluntarioSeleccionado = miembros.find((m) => m.id === Number(miembroId))
  const laborSeleccionada = actividades.find((a) => a.id === Number(actividadId))

  useEffect(() => {
    let activo = true
    if (voluntarioSeleccionado) {
      alObtenerInsignias(voluntarioSeleccionado.id)
        .then((datos) => {
          if (activo) setInsignias(datos)
        })
        .catch(() => {
          if (activo) setInsignias([])
        })
    }
    return () => {
      activo = false
    }
  }, [voluntarioSeleccionado, alObtenerInsignias])

  const horasNum = parseFloat(horasTrabajadas) || 0
  const excedeMaximoHoras = horasNum > 24
  const esFechaFutura = fechaLabor > fechaHoy
  const puntosPorHora = laborSeleccionada?.puntosPorHora ?? 1
  const puntosCalculados = Math.round(horasNum * puntosPorHora)
  const puntosActuales = voluntarioSeleccionado?.puntosTotales ?? 0
  const puntosProyectados = puntosActuales + puntosCalculados

  const metaSiguienteRango =
    voluntarioSeleccionado?.nivel === 'APRENDIZ'
      ? { nombre: 'OFICIAL', meta: 50, faltan: Math.max(0, 50 - puntosActuales) }
      : voluntarioSeleccionado?.nivel === 'OFICIAL'
        ? { nombre: 'MAESTRO', meta: 150, faltan: Math.max(0, 150 - puntosActuales) }
        : null

  const porcentajeProgreso = metaSiguienteRango
    ? Math.min(100, Math.round((puntosActuales / metaSiguienteRango.meta) * 100))
    : 100

  const opcionesMiembros = miembros
    .filter((m) => m.activoEnComunidad)
    .map((m) => ({
      valor: String(m.id),
      etiqueta: `${m.nombreCompleto} (${m.cedula}) - ${m.nivel}`,
    }))

  const opcionesActividades = actividades
    .filter((a) => a.activa)
    .map((a) => ({
      valor: String(a.id),
      etiqueta: `${a.nombre} (${a.puntosPorHora} pt/h)`,
    }))

  const formularioInvalido =
    !miembroId ||
    !actividadId ||
    excedeMaximoHoras ||
    esFechaFutura ||
    horasNum < 0.25 ||
    guardando ||
    cargando

  const manejarEnvio = async (evento: FormEvent) => {
    evento.preventDefault()
    if (formularioInvalido) return

    setGuardando(true)
    setErrorApi(null)

    try {
      const respuesta = await alRegistrar({
        miembroId: Number(miembroId),
        actividadId: Number(actividadId),
        horasTrabajadas: horasNum,
        fechaLabor,
        observaciones: observaciones.trim() || undefined,
      })

      setResultadoCelebracion(respuesta)
      setModalAbierto(true)
    } catch (err) {
      if (err instanceof Error) {
        setErrorApi(err.message)
      } else {
        setErrorApi('Ocurrió un error al registrar la jornada')
      }
    } finally {
      setGuardando(false)
    }
  }

  const manejarCerrarModal = () => {
    setModalAbierto(false)
    if (alExito) {
      alExito()
    }
  }

  return (
    <div className="registro-labor-contenedor">
      <header className="registro-labor-cabecera">
        <KintiGuia variante="sabiduria" tamano="sm" />
        <div>
          <h1 className="registro-labor-titulo">Registrar Jornada de Voluntario</h1>
          <p className="registro-labor-subtitulo">
            Acredita puntos ancestrales exclusivamente al miembro comunitario por su labor
          </p>
        </div>
      </header>

      <form onSubmit={manejarEnvio} className="registro-labor-layout">
        <div className="columna-formulario">
          <section className="tarjeta-paso">
            <h2 className="tarjeta-paso-titulo">
              <span className="paso-numero">1</span> Voluntario
            </h2>
            <Selector
              etiqueta="Voluntario"
              opciones={opcionesMiembros}
              valor={miembroId}
              alCambiar={setMiembroId}
            />
          </section>

          <section className="tarjeta-paso">
            <h2 className="tarjeta-paso-titulo">
              <span className="paso-numero">2</span> Labor desempeñada
            </h2>
            <Selector
              etiqueta="Labor Comunitaria"
              opciones={opcionesActividades}
              valor={actividadId}
              alCambiar={setActividadId}
            />
            {laborSeleccionada && (
              <div className="resumen-labor-seleccionada">
                <span>{laborSeleccionada.nombre}</span>
                <Insignia
                  texto={`${laborSeleccionada.puntosPorHora} pt / hora`}
                  tono="exito"
                />
              </div>
            )}
          </section>

          <section className="tarjeta-paso">
            <h2 className="tarjeta-paso-titulo">
              <span className="paso-numero">3</span> Fecha y Horas Trabajadas
            </h2>
            <div className="horas-contenedor">
              <CampoTexto
                type="date"
                etiqueta="Fecha de la labor"
                valor={fechaLabor}
                alCambiar={setFechaLabor}
              />
              {esFechaFutura && (
                <div className="alerta-validacion">
                  <span>⚠️</span>
                  <span>La fecha de la labor no puede ser futura</span>
                </div>
              )}

              <CampoTexto
                type="number"
                etiqueta="Horas trabajadas"
                valor={horasTrabajadas}
                alCambiar={setHorasTrabajadas}
                step="0.25"
                min="0.25"
                max="24"
              />

              <div className="botones-horas-rapidas">
                {['1', '2', '4', '8'].map((h) => (
                  <button
                    key={h}
                    type="button"
                    className={`boton-hora-rapida ${horasTrabajadas === h ? 'activo' : ''}`}
                    onClick={() => setHorasTrabajadas(h)}
                  >
                    {h}h
                  </button>
                ))}
              </div>

              {excedeMaximoHoras && (
                <div className="alerta-validacion">
                  <span>⚠️</span>
                  <span>La jornada no puede exceder las 24 horas continuas (norma MSP)</span>
                </div>
              )}
            </div>
          </section>

          <section className="tarjeta-paso">
            <h2 className="tarjeta-paso-titulo">
              <span className="paso-numero">4</span> Observaciones (opcional)
            </h2>
            <div className="campo-observaciones">
              <label htmlFor="observaciones" className="campo-etiqueta">
                Observaciones
              </label>
              <textarea
                id="observaciones"
                className="campo-textarea"
                rows={3}
                placeholder="Detalles de la labor comunitaria..."
                value={observaciones}
                onChange={(e) => setObservaciones(e.target.value)}
                maxLength={500}
              />
            </div>
          </section>

          {errorApi && (
            <div className="alerta-validacion">
              <span>⚠️</span>
              <span>{errorApi}</span>
            </div>
          )}

          <div className="sticky-bottom-bar">
            <Boton
              type="submit"
              variante="primario"
              disabled={formularioInvalido}
            >
              {guardando ? 'Acreditando Puntos...' : 'Acreditar Puntos al Miembro'}
            </Boton>
          </div>
        </div>

        <aside className="columna-ficha">
          {!voluntarioSeleccionado ? (
            <div className="ficha-vacia">
              <KintiGuia variante="sabiduria" tamano="md" />
              <p>Selecciona un voluntario para ver su ficha y progreso ancestral en vivo</p>
            </div>
          ) : (
            <div className="ficha-motivacional">
              <div className="ficha-encabezado">
                <div className="ficha-avatar">
                  {voluntarioSeleccionado.nombres.charAt(0)}
                  {voluntarioSeleccionado.apellidos.charAt(0)}
                </div>
                <div className="ficha-datos">
                  <h3 className="ficha-nombre">{voluntarioSeleccionado.nombreCompleto}</h3>
                  <p className="ficha-cedula">C.I. {voluntarioSeleccionado.cedula}</p>
                  <div className="ficha-rango">
                    <Insignia
                      texto={voluntarioSeleccionado.nivel}
                      tono={
                        voluntarioSeleccionado.nivel === 'MAESTRO'
                          ? 'exito'
                          : voluntarioSeleccionado.nivel === 'OFICIAL'
                            ? 'advertencia'
                            : 'neutro'
                      }
                    />
                  </div>
                </div>
              </div>

              <div className="calculadora-animada">
                <div className="calculadora-titulo">Proyección Dinámica de Puntaje</div>
                <div className="calculadora-proyeccion">
                  Puntos actuales: {puntosActuales} pts → Tras esta labor: {puntosProyectados} pts
                  <span className="calculadora-delta"> (+{puntosCalculados} pts)</span>
                </div>
              </div>

              {metaSiguienteRango && (
                <div className="progresion-rango">
                  <div className="progresion-meta">
                    <span>Proximidad a {metaSiguienteRango.nombre}</span>
                    <span>{metaSiguienteRango.faltan} pts restantes</span>
                  </div>
                  <div className="barra-progreso-fondo">
                    <div
                      className="barra-progreso-relleno"
                      style={{ width: `${porcentajeProgreso}%` }}
                    />
                  </div>
                </div>
              )}

              <div className="vitrina-talentos">
                <h4 className="vitrina-titulo">Talentos e Insignias Comunitarias</h4>
                {insignias.length === 0 ? (
                  <p className="vitrina-vacia">Aún no posee talentos registrados</p>
                ) : (
                  <div className="vitrina-lista">
                    {insignias.map((insignia) => (
                      <div key={insignia.id} className="vitrina-item">
                        <span className="vitrina-icono">🌿</span>
                        <div>
                          <strong>{insignia.nombre}</strong>
                          <p>{insignia.descripcion}</p>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          )}
        </aside>
      </form>

      <ModalCelebracionPuntos
        abierto={modalAbierto}
        resultado={resultadoCelebracion}
        alCerrar={manejarCerrarModal}
      />
    </div>
  )
}
