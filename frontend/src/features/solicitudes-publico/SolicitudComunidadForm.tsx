import { useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router'
import { ApiError } from '../../api/client'
import { Boton } from '../../components/Boton'
import { CampoTexto } from '../../components/CampoTexto'
import { KintiGuia } from '../../components/KintiGuia'
import { Notificacion } from '../../components/Notificacion'
import { Selector } from '../../components/Selector'
import { esCedulaValida, esMayorDeEdad } from '../../components/validaciones'
import { ModalConfirmacionSolicitud } from './ModalConfirmacionSolicitud'
import { ModalConsentimientoLopdp } from './ModalConsentimientoLopdp'
import {
  MAXIMO_SUBTIPOS,
  OPCIONES_DISENO,
  OPCIONES_GENERO,
  OPCIONES_SUBTIPO,
  OPCIONES_TIPO_ORGANIZACION,
} from './opcionesSolicitud'
import {
  enviarSolicitudComunidad,
  type CrearSolicitudComunidadRequest,
  type DisenoPrincipal,
  type Genero,
  type SolicitudComunidadResponse,
  type TipoOrganizacion,
} from './solicitudesPublicoApi'
import './SolicitudComunidadForm.css'

type Campos = Record<
  | 'nombreComunidad'
  | 'tipoOrganizacion'
  | 'provincia'
  | 'parroquia'
  | 'direccion'
  | 'latitud'
  | 'longitud'
  | 'altitudMsnm'
  | 'disenoPrincipal'
  | 'cedulaSolicitante'
  | 'nombreSolicitante'
  | 'fechaNacimiento'
  | 'generoSolicitante'
  | 'correoSolicitante'
  | 'celularSolicitante',
  string
>

type Errores = Partial<Record<keyof Campos | 'subtiposSolicitados', string>>

const CAMPOS_VACIOS: Campos = {
  nombreComunidad: '',
  tipoOrganizacion: '',
  provincia: '',
  parroquia: '',
  direccion: '',
  latitud: '',
  longitud: '',
  altitudMsnm: '2500',
  disenoPrincipal: '',
  cedulaSolicitante: '',
  nombreSolicitante: '',
  fechaNacimiento: '',
  generoSolicitante: '',
  correoSolicitante: '',
  celularSolicitante: '',
}

const SOLO_LETRAS = /^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$/
const SOLO_LETRAS_MENSAJE = 'Solo se admiten letras y espacios'

function validarFormato(campos: Campos): Errores {
  const errores: Errores = {}
  if (campos.nombreComunidad && !SOLO_LETRAS.test(campos.nombreComunidad)) {
    errores.nombreComunidad = SOLO_LETRAS_MENSAJE
  }
  if (campos.nombreSolicitante && !SOLO_LETRAS.test(campos.nombreSolicitante)) {
    errores.nombreSolicitante = SOLO_LETRAS_MENSAJE
  }
  if (campos.cedulaSolicitante && !esCedulaValida(campos.cedulaSolicitante)) {
    errores.cedulaSolicitante = 'La cédula no es válida'
  }
  if (campos.fechaNacimiento && !esMayorDeEdad(campos.fechaNacimiento)) {
    errores.fechaNacimiento = 'Debes ser mayor de 18 años para postular una comunidad'
  }
  if (campos.celularSolicitante && !/^09[0-9]{8}$/.test(campos.celularSolicitante)) {
    errores.celularSolicitante = 'El celular debe tener 10 dígitos y empezar con 09'
  }
  return errores
}

function validarObligatorios(campos: Campos, subtipos: string[]): Errores {
  const errores: Errores = {}
  for (const campo of Object.keys(campos) as (keyof Campos)[]) {
    if (!campos[campo].trim()) {
      errores[campo] = 'Este campo es obligatorio'
    }
  }
  if (subtipos.length === 0) {
    errores.subtiposSolicitados = 'Elige al menos un subtipo de cultivo'
  }
  return errores
}

function armarSolicitud(campos: Campos, subtipos: string[]): CrearSolicitudComunidadRequest {
  return {
    ...campos,
    tipoOrganizacion: campos.tipoOrganizacion as TipoOrganizacion,
    disenoPrincipal: campos.disenoPrincipal as DisenoPrincipal,
    generoSolicitante: campos.generoSolicitante as Genero,
    latitud: Number(campos.latitud),
    longitud: Number(campos.longitud),
    altitudMsnm: Number(campos.altitudMsnm),
    subtiposSolicitados: subtipos.join(','),
    consentimientoDatos: true,
  }
}

export function SolicitudComunidadForm() {
  const navegar = useNavigate()
  const [campos, setCampos] = useState<Campos>(CAMPOS_VACIOS)
  const [subtipos, setSubtipos] = useState<string[]>([])
  const [faltantes, setFaltantes] = useState<Errores>({})
  const [pidiendoConsentimiento, setPidiendoConsentimiento] = useState(false)
  const [enviando, setEnviando] = useState(false)
  const [errorServidor, setErrorServidor] = useState<ApiError | null>(null)
  const [registrada, setRegistrada] = useState<SolicitudComunidadResponse | null>(null)
  const erroresFormato = validarFormato(campos)

  function errorDe(campo: keyof Errores) {
    return erroresFormato[campo] ?? faltantes[campo] ?? errorServidor?.errores?.[campo]
  }

  function cambiar(campo: keyof Campos) {
    return (valor: string) => {
      setCampos((anteriores) => ({ ...anteriores, [campo]: valor }))
      setFaltantes((anteriores) => ({ ...anteriores, [campo]: undefined }))
    }
  }

  function alternarSubtipo(subtipo: string) {
    setSubtipos((anteriores) =>
      anteriores.includes(subtipo) ? anteriores.filter((elegido) => elegido !== subtipo) : [...anteriores, subtipo],
    )
    setFaltantes((anteriores) => ({ ...anteriores, subtiposSolicitados: undefined }))
  }

  function usarMiUbicacion() {
    navigator.geolocation.getCurrentPosition(({ coords }) => {
      setCampos((anteriores) => ({
        ...anteriores,
        latitud: coords.latitude.toFixed(7),
        longitud: coords.longitude.toFixed(7),
      }))
    })
  }

  function revisar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    const obligatorios = validarObligatorios(campos, subtipos)
    setFaltantes(obligatorios)
    if (Object.keys(obligatorios).length === 0 && Object.keys(erroresFormato).length === 0) {
      setPidiendoConsentimiento(true)
    }
  }

  async function enviar() {
    setPidiendoConsentimiento(false)
    setEnviando(true)
    setErrorServidor(null)
    try {
      setRegistrada(await enviarSolicitudComunidad(armarSolicitud(campos, subtipos)))
    } catch (falla) {
      if (!(falla instanceof ApiError)) {
        throw falla
      }
      setErrorServidor(falla)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <form className="solicitud" onSubmit={revisar} noValidate>
      <header className="solicitud-cabecera">
        <KintiGuia variante="dia" className="solicitud-kinti" />
        <p className="solicitud-frase">
          ¡Sembrar en comunidad sana el cuerpo y une los corazones! Da el primer paso para cultivar la salud de tu
          barrio completando esta solicitud
        </p>
      </header>
      {errorServidor && <Notificacion tipo="error" mensaje={errorServidor.message} />}
      <fieldset className="solicitud-tarjeta" disabled={enviando}>
        <legend className="solicitud-titulo">Datos de la comunidad y huerto</legend>
        <div className="solicitud-ancho">
          <CampoTexto etiqueta="Nombre de la comunidad" maxLength={150} required valor={campos.nombreComunidad}
            alCambiar={cambiar('nombreComunidad')} error={errorDe('nombreComunidad')} />
        </div>
        <Selector etiqueta="Tipo de organización" opciones={OPCIONES_TIPO_ORGANIZACION} required
          valor={campos.tipoOrganizacion} alCambiar={cambiar('tipoOrganizacion')} error={errorDe('tipoOrganizacion')} />
        <CampoTexto etiqueta="Provincia" maxLength={100} required valor={campos.provincia}
          alCambiar={cambiar('provincia')} error={errorDe('provincia')} />
        <CampoTexto etiqueta="Parroquia" maxLength={100} required valor={campos.parroquia}
          alCambiar={cambiar('parroquia')} error={errorDe('parroquia')} />
        <div className="solicitud-ancho">
          <CampoTexto etiqueta="Dirección exacta" maxLength={255} required valor={campos.direccion}
            alCambiar={cambiar('direccion')} error={errorDe('direccion')} />
        </div>
        <CampoTexto etiqueta="Latitud" type="number" step="any" required valor={campos.latitud}
          alCambiar={cambiar('latitud')} error={errorDe('latitud')} />
        <CampoTexto etiqueta="Longitud" type="number" step="any" required valor={campos.longitud}
          alCambiar={cambiar('longitud')} error={errorDe('longitud')} />
        <div className="solicitud-ancho">
          <Boton variante="secundario" onClick={usarMiUbicacion}>
            Usar mi ubicación actual
          </Boton>
        </div>
        <CampoTexto etiqueta="Altitud estimada (msnm)" type="number" min={0} max={5000} required
          valor={campos.altitudMsnm} alCambiar={cambiar('altitudMsnm')} error={errorDe('altitudMsnm')} />
        <Selector etiqueta="Diseño principal del huerto" opciones={OPCIONES_DISENO} required
          valor={campos.disenoPrincipal} alCambiar={cambiar('disenoPrincipal')} error={errorDe('disenoPrincipal')} />
        <fieldset className="solicitud-ancho solicitud-subtipos">
          <legend className="campo-etiqueta">Subtipos de cultivo (hasta {MAXIMO_SUBTIPOS})</legend>
          {OPCIONES_SUBTIPO.map((opcion) => (
            <label key={opcion.valor} className="solicitud-subtipo">
              <input
                type="checkbox"
                checked={subtipos.includes(opcion.valor)}
                disabled={!subtipos.includes(opcion.valor) && subtipos.length >= MAXIMO_SUBTIPOS}
                onChange={() => alternarSubtipo(opcion.valor)}
              />
              {opcion.etiqueta}
            </label>
          ))}
          {errorDe('subtiposSolicitados') && <p className="campo-error">{errorDe('subtiposSolicitados')}</p>}
        </fieldset>
      </fieldset>
      <fieldset className="solicitud-tarjeta" disabled={enviando}>
        <legend className="solicitud-titulo">Datos del solicitante</legend>
        <CampoTexto etiqueta="Cédula de identidad" inputMode="numeric" maxLength={10} required
          valor={campos.cedulaSolicitante} alCambiar={cambiar('cedulaSolicitante')}
          error={errorDe('cedulaSolicitante')} />
        <CampoTexto etiqueta="Nombre y apellido" maxLength={120} required valor={campos.nombreSolicitante}
          alCambiar={cambiar('nombreSolicitante')} error={errorDe('nombreSolicitante')} />
        <CampoTexto etiqueta="Fecha de nacimiento" type="date" required valor={campos.fechaNacimiento}
          alCambiar={cambiar('fechaNacimiento')} error={errorDe('fechaNacimiento')} />
        <Selector etiqueta="Género" opciones={OPCIONES_GENERO} required valor={campos.generoSolicitante}
          alCambiar={cambiar('generoSolicitante')} error={errorDe('generoSolicitante')} />
        <CampoTexto etiqueta="Correo electrónico" type="email" maxLength={180} required
          valor={campos.correoSolicitante} alCambiar={cambiar('correoSolicitante')}
          error={errorDe('correoSolicitante')} />
        <CampoTexto etiqueta="Teléfono celular" type="tel" maxLength={10} required
          valor={campos.celularSolicitante} alCambiar={cambiar('celularSolicitante')}
          error={errorDe('celularSolicitante')} />
      </fieldset>
      <div className="solicitud-acciones">
        <Boton type="submit" disabled={enviando}>
          {enviando ? 'Enviando…' : 'Enviar solicitud de huerto'}
        </Boton>
      </div>
      <ModalConsentimientoLopdp
        abierto={pidiendoConsentimiento}
        alCancelar={() => setPidiendoConsentimiento(false)}
        alAceptar={enviar}
      />
      <ModalConfirmacionSolicitud solicitud={registrada} alCerrar={() => navegar('/')} />
    </form>
  )
}
