import { useEffect, useState, type FormEvent } from 'react'
import { Boton } from '../../components/Boton'
import { CampoTexto } from '../../components/CampoTexto'
import { Insignia } from '../../components/Insignia'
import { KintiGuia } from '../../components/KintiGuia'
import { Notificacion } from '../../components/Notificacion'
import {
  actualizarMiComunidad,
  obtenerMiComunidad,
  type ComunidadDetalleResponse,
} from './comunidadesApi'
import './MiComunidadForm.css'

const PATRON_NOMBRE = /^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$/
const PATRON_TELEFONO = /^09[0-9]{8}$/
const PATRON_CORREO = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

export function MiComunidadForm() {
  const [comunidad, setComunidad] = useState<ComunidadDetalleResponse | null>(null)
  const [cargando, setCargando] = useState(true)
  const [guardando, setGuardando] = useState(false)

  const [nombre, setNombre] = useState('')
  const [telefono, setTelefono] = useState('')
  const [correo, setCorreo] = useState('')

  const [errores, setErrores] = useState<{
    nombre?: string
    telefono?: string
    correo?: string
  }>({})

  const [notificacion, setNotificacion] = useState<{
    tipo: 'exito' | 'error'
    mensaje: string
  } | null>(null)

  useEffect(() => {
    let montado = true
    obtenerMiComunidad()
      .then((datos) => {
        if (montado) {
          setComunidad(datos)
          setNombre(datos.nombre)
          setTelefono(datos.telefonoContacto)
          setCorreo(datos.correoContacto)
          setCargando(false)
        }
      })
      .catch((err: Error) => {
        if (montado) {
          setNotificacion({
            tipo: 'error',
            mensaje: err.message || 'Error al cargar los datos de la comunidad',
          })
          setCargando(false)
        }
      })
    return () => {
      montado = false
    }
  }, [])

  function validar(): boolean {
    const nuevosErrores: { nombre?: string; telefono?: string; correo?: string } = {}

    if (!nombre.trim()) {
      nuevosErrores.nombre = 'El nombre es obligatorio'
    } else if (!PATRON_NOMBRE.test(nombre)) {
      nuevosErrores.nombre = 'El nombre solo puede contener letras y espacios'
    } else if (nombre.length > 150) {
      nuevosErrores.nombre = 'El nombre no puede exceder 150 caracteres'
    }

    if (!telefono.trim()) {
      nuevosErrores.telefono = 'El teléfono es obligatorio'
    } else if (!PATRON_TELEFONO.test(telefono)) {
      nuevosErrores.telefono = 'El teléfono debe tener 10 dígitos y empezar con 09'
    }

    if (!correo.trim()) {
      nuevosErrores.correo = 'El correo es obligatorio'
    } else if (!PATRON_CORREO.test(correo)) {
      nuevosErrores.correo = 'Formato de correo inválido'
    } else if (correo.length > 180) {
      nuevosErrores.correo = 'El correo no puede exceder 180 caracteres'
    }

    setErrores(nuevosErrores)
    return Object.keys(nuevosErrores).length === 0
  }

  async function manejarEnvio(evento: FormEvent) {
    evento.preventDefault()
    setNotificacion(null)

    if (!validar()) {
      return
    }

    setGuardando(true)
    try {
      const actualizada = await actualizarMiComunidad({
        nombre: nombre.trim(),
        telefonoContacto: telefono.trim(),
        correoContacto: correo.trim(),
      })
      setComunidad(actualizada)
      setNombre(actualizada.nombre)
      setTelefono(actualizada.telefonoContacto)
      setCorreo(actualizada.correoContacto)
      setErrores({})
      setNotificacion({
        tipo: 'exito',
        mensaje: 'Información de la comunidad actualizada correctamente',
      })
    } catch (err: unknown) {
      const mensaje = err instanceof Error ? err.message : 'Error al actualizar la comunidad'
      setNotificacion({
        tipo: 'error',
        mensaje,
      })
    } finally {
      setGuardando(false)
    }
  }

  if (cargando) {
    return <p className="mi-comunidad-cargando">Cargando información de tu comunidad...</p>
  }

  return (
    <div className="mi-comunidad">
      <header className="mi-comunidad-banner">
        <KintiGuia variante="dia" tamano="sm" />
        <p className="mi-comunidad-banner-texto">
          ¡Cuidar nuestra comunidad es sembrar futuro! Mantén actualizados los canales de contacto para
          que nuevos voluntarios y sabios puedan sumarse a tus mingas y talleres.
        </p>
      </header>

      {notificacion && (
        <Notificacion tipo={notificacion.tipo} mensaje={notificacion.mensaje} />
      )}

      {comunidad && (
        <section className="mi-comunidad-tarjeta" aria-label="Información territorial de la comunidad">
          <h2 className="mi-comunidad-subtitulo">Ficha Territorial</h2>
          <div className="mi-comunidad-info-grid">
            <div className="mi-comunidad-dato">
              <span className="mi-comunidad-dato-etiqueta">Comunidad</span>
              <span className="mi-comunidad-dato-valor">{comunidad.nombre}</span>
            </div>
            <div className="mi-comunidad-dato">
              <span className="mi-comunidad-dato-etiqueta">Responsable Oficial</span>
              <span className="mi-comunidad-dato-valor">{comunidad.nombreResponsable}</span>
            </div>
            <div className="mi-comunidad-dato">
              <span className="mi-comunidad-dato-etiqueta">Provincia</span>
              <span className="mi-comunidad-dato-valor">{comunidad.provincia}</span>
            </div>
            <div className="mi-comunidad-dato">
              <span className="mi-comunidad-dato-etiqueta">Parroquia</span>
              <span className="mi-comunidad-dato-valor">{comunidad.parroquia}</span>
            </div>
            <div className="mi-comunidad-dato">
              <span className="mi-comunidad-dato-etiqueta">Dirección</span>
              <span className="mi-comunidad-dato-valor">{comunidad.direccion}</span>
            </div>
            <div className="mi-comunidad-dato">
              <span className="mi-comunidad-dato-etiqueta">Tipo de Organización</span>
              <span className="mi-comunidad-dato-valor">{comunidad.tipoOrganizacion}</span>
            </div>
            <div className="mi-comunidad-dato">
              <span className="mi-comunidad-dato-etiqueta">Puntos Comunitarios</span>
              <span className="mi-comunidad-dato-valor">
                <Insignia texto={`${comunidad.puntosComunidad} pts`} tono="exito" />
              </span>
            </div>
          </div>
        </section>
      )}

      <form className="mi-comunidad-tarjeta" onSubmit={manejarEnvio} noValidate>
        <h2 className="mi-comunidad-subtitulo">Canales Oficiales de Contacto</h2>

        <CampoTexto
          etiqueta="Nombre de la comunidad"
          valor={nombre}
          alCambiar={setNombre}
          error={errores.nombre}
          disabled={guardando}
          maxLength={150}
          required
        />

        <CampoTexto
          etiqueta="Teléfono de contacto"
          valor={telefono}
          alCambiar={setTelefono}
          error={errores.telefono}
          disabled={guardando}
          placeholder="0991234567"
          maxLength={10}
          required
        />

        <CampoTexto
          etiqueta="Correo de contacto"
          valor={correo}
          alCambiar={setCorreo}
          error={errores.correo}
          disabled={guardando}
          type="email"
          maxLength={180}
          required
        />

        <div className="mi-comunidad-acciones">
          <Boton
            type="submit"
            variante="primario"
            disabled={guardando}
            className="mi-comunidad-boton"
          >
            {guardando ? 'Guardando...' : 'Guardar Cambios'}
          </Boton>
        </div>
      </form>
    </div>
  )
}
