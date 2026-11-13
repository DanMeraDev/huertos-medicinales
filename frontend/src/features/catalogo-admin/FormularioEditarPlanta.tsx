import { useState, type FormEvent, type ReactNode } from 'react'
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
  ENLACE_CATALOGO,
  enviarConFotografia,
  erroresDelServidor,
  esErrorDeEstado,
  seccionConError,
  type Aviso,
} from './envioPlanta'
import { AvisoFormulario, TarjetaFormulario } from './PartesFormularioPlanta'
import { actualizarPlanta } from './plantasAdminApi'
import type { CualidadTermica, PlantaDetalleResponse } from './types'
import { useEsEscritorio } from './useEsEscritorio'
import { construirSolicitud, datosDesdePlanta, validarPlanta, type DatosPlanta, type ErroresPlanta } from './validacionPlanta'
import './FormularioCrearPlanta.css'
import './FormularioEditarPlanta.css'

const MENSAJE_EXITO = 'Ficha técnica actualizada exitosamente'
const MENSAJE_DUPLICADO = 'Nombre científico ya registrado en otra planta'
const MENSAJE_NO_EXISTE = 'La planta no existe o fue dada de baja'
const MENSAJE_ERROR_ACTUALIZACION = 'No se pudo actualizar la ficha. Intente nuevamente'

const TITULOS_ACORDEONES = ['Botánica', 'Agronomía', 'Medicina/Dosificación']

function avisoDeError(error: unknown): Aviso {
  if (esErrorDeEstado(error, 409)) return { mensaje: MENSAJE_DUPLICADO, enlace: null }
  if (esErrorDeEstado(error, 404)) return { mensaje: MENSAJE_NO_EXISTE, enlace: ENLACE_CATALOGO }
  if (esErrorDeEstado(error, 401, 403)) return AVISO_SESION
  return { mensaje: MENSAJE_ERROR_ACTUALIZACION, enlace: null }
}

interface AcordeonProps {
  titulo: string
  abierto: boolean
  alAlternar: () => void
  children: ReactNode
}

function Acordeon({ titulo, abierto, alAlternar, children }: AcordeonProps) {
  return (
    <section className="editar-planta-acordeon">
      <h2 className="editar-planta-acordeon-titulo">
        <button type="button" className="editar-planta-acordeon-boton" aria-expanded={abierto} onClick={alAlternar}>
          {titulo}
          <span aria-hidden="true" className="editar-planta-acordeon-icono">
            ›
          </span>
        </button>
      </h2>
      {abierto && <div className="editar-planta-acordeon-contenido">{children}</div>}
    </section>
  )
}

export interface FormularioEditarPlantaProps {
  planta: PlantaDetalleResponse
}

export function FormularioEditarPlanta({ planta }: FormularioEditarPlantaProps) {
  const esEscritorio = useEsEscritorio()
  const navegar = useNavigate()
  const [datos, setDatos] = useState<DatosPlanta>(() => datosDesdePlanta(planta))
  const [fotografia, setFotografia] = useState<File | null>(null)
  const [fotografiaActual, setFotografiaActual] = useState(planta.fotografiaUrl)
  const [errores, setErrores] = useState<ErroresPlanta>({})
  const [aviso, setAviso] = useState<Aviso | null>(null)
  const [abiertos, setAbiertos] = useState([true, false, false])
  const [enviando, setEnviando] = useState(false)
  const rutaFicha = `/catalogo-plantas/${planta.id}`

  function actualizar<K extends keyof DatosPlanta>(campo: K, valor: DatosPlanta[K]) {
    setDatos((actuales) => ({ ...actuales, [campo]: valor }))
  }

  function alternarAcordeon(indice: number) {
    setAbiertos((actuales) => actuales.map((abierto, posicion) => (posicion === indice ? !abierto : abierto)))
  }

  function mostrarErrores(nuevos: ErroresPlanta) {
    setErrores(nuevos)
    const conError = seccionConError(nuevos)
    if (conError >= 0) setAbiertos((actuales) => actuales.map((abierto, posicion) => abierto || posicion === conError))
  }

  function cambiarFotografia(nueva: File | null) {
    setFotografia(nueva)
    if (!nueva) setFotografiaActual(null)
  }

  async function guardarFicha(cualidadTermica: CualidadTermica, fotografiaUrl: string | null) {
    try {
      await actualizarPlanta(planta.id, construirSolicitud(datos, cualidadTermica, fotografiaUrl))
      navegar(rutaFicha, { state: { mensajeExito: MENSAJE_EXITO } })
    } catch (error) {
      const delServidor = erroresDelServidor(error, MENSAJE_DUPLICADO)
      if (delServidor) mostrarErrores(delServidor)
      if (!esErrorDeEstado(error, 400)) setAviso(avisoDeError(error))
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
      await enviarConFotografia(fotografia, fotografiaActual, (url) => guardarFicha(cualidadTermica, url), setAviso)
    } finally {
      setEnviando(false)
    }
  }

  function alEnviar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    void guardar()
  }

  const campos: CamposPlantaProps = { datos, errores, alCambiar: actualizar }
  const cargadorFoto = (
    <CargadorImagen
      etiqueta="Fotografía de la planta"
      archivo={fotografia}
      urlActual={fotografiaActual}
      alCambiar={cambiarFotografia}
    />
  )

  if (esEscritorio) {
    return (
      <form className="editar-planta" onSubmit={alEnviar} noValidate>
        <div className="editar-planta-columnas">
          <div className="editar-planta-columna">
            <TarjetaFormulario titulo="Taxonomía y nombres">
              <CamposIdentificacion {...campos} />
            </TarjetaFormulario>
            <TarjetaFormulario titulo="Fotografía">{cargadorFoto}</TarjetaFormulario>
            <TarjetaFormulario titulo="Condiciones de cultivo">
              <CamposCultivo {...campos} />
            </TarjetaFormulario>
          </div>
          <div className="editar-planta-columna">
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
        <div className="editar-planta-acciones">
          <Boton variante="secundario" onClick={() => navegar(rutaFicha)}>
            Descartar Cambios
          </Boton>
          <Boton type="submit" disabled={enviando}>
            Guardar Cambios
          </Boton>
        </div>
      </form>
    )
  }

  function contenidoDelAcordeon(indice: number) {
    if (indice === 0) {
      return (
        <>
          <CamposIdentificacion {...campos} />
          {cargadorFoto}
        </>
      )
    }
    if (indice === 1) return <CamposCultivo {...campos} />
    return (
      <>
        <CamposUsos {...campos} />
        <CampoPreparacion {...campos} />
        <CampoContraindicaciones {...campos} />
      </>
    )
  }

  return (
    <form className="editar-planta" onSubmit={alEnviar} noValidate>
      {TITULOS_ACORDEONES.map((titulo, indice) => (
        <Acordeon key={titulo} titulo={titulo} abierto={abiertos[indice]} alAlternar={() => alternarAcordeon(indice)}>
          {contenidoDelAcordeon(indice)}
        </Acordeon>
      ))}
      <AvisoFormulario aviso={aviso} />
      <div className="editar-planta-barra-inferior">
        <Boton variante="secundario" onClick={() => navegar(rutaFicha)}>
          Descartar
        </Boton>
        <Boton type="submit" disabled={enviando}>
          Guardar Cambios
        </Boton>
      </div>
    </form>
  )
}
