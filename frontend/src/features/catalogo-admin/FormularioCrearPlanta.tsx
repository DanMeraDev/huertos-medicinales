import { useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router'
import { Boton } from '../../components/Boton'
import { CargadorImagen } from '../../components/CargadorImagen'
import {
  CampoContraindicaciones,
  CampoPreparacion,
  CamposCultivo,
  CamposIdentificacion,
  CamposUsos,
  type CamposPlantaProps,
} from './CamposPlanta'
import {
  AVISO_SESION,
  CAMPOS_POR_SECCION,
  enviarConFotografia,
  erroresDelServidor,
  esErrorDeEstado,
  seccionConError,
  type Aviso,
} from './envioPlanta'
import { AvisoFormulario, TarjetaFormulario } from './PartesFormularioPlanta'
import { registrarPlanta } from './plantasAdminApi'
import type { CualidadTermica } from './types'
import { useEsEscritorio } from './useEsEscritorio'
import {
  construirSolicitud,
  DATOS_INICIALES,
  erroresDelPaso,
  validarPlanta,
  type DatosPlanta,
  type ErroresPlanta,
} from './validacionPlanta'
import './FormularioCrearPlanta.css'

const RUTA_CATALOGO = '/catalogo-plantas'
const MENSAJE_EXITO = 'Planta registrada exitosamente'
const MENSAJE_DUPLICADO = 'El nombre científico ya existe en el catálogo'
const MENSAJE_ERROR_REGISTRO = 'No se pudo registrar la planta. Intente nuevamente'

const TITULOS_PASOS = ['Identificación y fotografía', 'Parámetros agronómicos', 'Usos y dosificación']

function avisoDeError(error: unknown): Aviso {
  return esErrorDeEstado(error, 401, 403) ? AVISO_SESION : { mensaje: MENSAJE_ERROR_REGISTRO, enlace: null }
}

export function FormularioCrearPlanta() {
  const esEscritorio = useEsEscritorio()
  const navegar = useNavigate()
  const [datos, setDatos] = useState<DatosPlanta>(DATOS_INICIALES)
  const [fotografia, setFotografia] = useState<File | null>(null)
  const [errores, setErrores] = useState<ErroresPlanta>({})
  const [aviso, setAviso] = useState<Aviso | null>(null)
  const [paso, setPaso] = useState(0)
  const [enviando, setEnviando] = useState(false)
  const esUltimoPaso = paso === TITULOS_PASOS.length - 1

  function actualizar<K extends keyof DatosPlanta>(campo: K, valor: DatosPlanta[K]) {
    setDatos((actuales) => ({ ...actuales, [campo]: valor }))
  }

  function mostrarErrores(nuevos: ErroresPlanta) {
    setErrores(nuevos)
    const pasoConError = seccionConError(nuevos)
    if (pasoConError >= 0) setPaso(pasoConError)
  }

  function siguiente() {
    const delPaso = erroresDelPaso(validarPlanta(datos), CAMPOS_POR_SECCION[paso])
    setErrores(delPaso)
    if (Object.keys(delPaso).length === 0) setPaso(paso + 1)
  }

  async function registrar(cualidadTermica: CualidadTermica, fotografiaUrl: string | null) {
    try {
      await registrarPlanta(construirSolicitud(datos, cualidadTermica, fotografiaUrl))
      navegar(RUTA_CATALOGO, { state: { mensajeExito: MENSAJE_EXITO } })
    } catch (error) {
      const delServidor = erroresDelServidor(error, MENSAJE_DUPLICADO)
      if (delServidor) mostrarErrores(delServidor)
      else setAviso(avisoDeError(error))
    }
  }

  async function guardar() {
    const nuevos = validarPlanta(datos)
    const cualidadTermica = datos.cualidadTermica
    if (Object.keys(nuevos).length > 0 || !cualidadTermica) {
      mostrarErrores(nuevos)
      return
    }
    setErrores({})
    setAviso(null)
    setEnviando(true)
    try {
      await enviarConFotografia(fotografia, null, (url) => registrar(cualidadTermica, url), setAviso)
    } finally {
      setEnviando(false)
    }
  }

  function alEnviar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    if (!esEscritorio && !esUltimoPaso) siguiente()
    else void guardar()
  }

  const campos: CamposPlantaProps = { datos, errores, alCambiar: actualizar }
  const cargadorFoto = (
    <CargadorImagen etiqueta="Fotografía de la planta" archivo={fotografia} alCambiar={setFotografia} />
  )

  if (esEscritorio) {
    return (
      <form className="crear-planta" onSubmit={alEnviar} noValidate>
        <div className="crear-planta-columnas">
          <div className="crear-planta-columna">
            <TarjetaFormulario titulo="Taxonomía y nombres">
              <CamposIdentificacion {...campos} />
            </TarjetaFormulario>
            <TarjetaFormulario titulo="Fotografía">{cargadorFoto}</TarjetaFormulario>
            <TarjetaFormulario titulo="Condiciones de cultivo">
              <CamposCultivo {...campos} />
            </TarjetaFormulario>
          </div>
          <div className="crear-planta-columna">
            <TarjetaFormulario titulo="Usos terapéuticos MSP">
              <CamposUsos {...campos} />
            </TarjetaFormulario>
            <TarjetaFormulario titulo="Modos de preparación y dosificación">
              <CampoPreparacion {...campos} />
            </TarjetaFormulario>
            <TarjetaFormulario titulo="Contraindicaciones">
              <CampoContraindicaciones {...campos} />
            </TarjetaFormulario>
          </div>
        </div>
        <AvisoFormulario aviso={aviso} />
        <div className="crear-planta-acciones">
          <Boton variante="secundario" onClick={() => navegar(RUTA_CATALOGO)}>
            Cancelar
          </Boton>
          <Boton type="submit" className="crear-planta-guardar" disabled={enviando}>
            Guardar Planta en Catálogo
          </Boton>
        </div>
      </form>
    )
  }

  function contenidoDelPaso() {
    if (paso === 0) {
      return (
        <>
          <CamposIdentificacion {...campos} />
          {cargadorFoto}
        </>
      )
    }
    if (paso === 1) return <CamposCultivo {...campos} />
    return (
      <>
        <CamposUsos {...campos} />
        <CampoPreparacion {...campos} />
        <CampoContraindicaciones {...campos} />
      </>
    )
  }

  return (
    <form className="crear-planta" onSubmit={alEnviar} noValidate>
      <div
        className="crear-planta-progreso"
        role="progressbar"
        aria-label={`Paso ${paso + 1} de ${TITULOS_PASOS.length}`}
        aria-valuemin={1}
        aria-valuemax={TITULOS_PASOS.length}
        aria-valuenow={paso + 1}
      >
        <div className="crear-planta-progreso-avance" style={{ width: `${((paso + 1) / TITULOS_PASOS.length) * 100}%` }} />
      </div>
      <h2 className="crear-planta-paso-titulo">{TITULOS_PASOS[paso]}</h2>
      <div className="crear-planta-campos">{contenidoDelPaso()}</div>
      <AvisoFormulario aviso={aviso} />
      <div className="crear-planta-barra-inferior">
        {paso > 0 && (
          <Boton variante="secundario" onClick={() => setPaso(paso - 1)}>
            Atrás
          </Boton>
        )}
        {esUltimoPaso ? (
          <Boton type="submit" disabled={enviando}>
            Guardar Planta
          </Boton>
        ) : (
          <Boton onClick={siguiente}>Siguiente</Boton>
        )}
      </div>
    </form>
  )
}
