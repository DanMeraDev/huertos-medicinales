import { useState, type FormEvent } from 'react'
import { ApiError } from '../../api/client'
import { Boton } from '../../components/Boton'
import { CampoTexto } from '../../components/CampoTexto'
import { KintiGuia } from '../../components/KintiGuia'
import { Notificacion } from '../../components/Notificacion'
import {
  consultarPerfilPorCedula,
  type PerfilPublicoMiembroResponse,
} from './miembrosPublicoApi'
import { PerfilMiembroModal } from './PerfilMiembroModal'
import './BuscadorPerfilCedula.css'

const MENSAJE_FORMATO_INVALIDO = 'La cédula debe contener exactamente 10 dígitos numéricos'
const MENSAJE_NO_ENCONTRADO =
  'No se encontró ningún voluntario registrado con la cédula ingresada. Verifica el número o anímate a registrar una nueva comunidad'

export function BuscadorPerfilCedula() {
  const [cedula, setCedula] = useState('')
  const [cargando, setCargando] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [perfil, setPerfil] = useState<PerfilPublicoMiembroResponse | null>(null)
  const [modalAbierto, setModalAbierto] = useState(false)

  async function manejarSubmit(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    const valor = cedula.trim()

    if (!/^[0-9]{10}$/.test(valor)) {
      setError(MENSAJE_FORMATO_INVALIDO)
      return
    }

    setCargando(true)
    setError(null)

    try {
      const resultado = await consultarPerfilPorCedula(valor)
      setPerfil(resultado)
      setModalAbierto(true)
    } catch (err: unknown) {
      if (err instanceof ApiError) {
        setError(
          err.detalle ??
            (err.status === 404 ? MENSAJE_NO_ENCONTRADO : err.message),
        )
      } else {
        setError('Error al consultar el perfil del voluntario')
      }
    } finally {
      setCargando(false)
    }
  }

  return (
    <section className="buscador-perfil" aria-label="Búsqueda de voluntario por cédula">
      <div className="buscador-perfil-banner">
        <KintiGuia variante="dia" tamano="sm" />
        <p className="buscador-perfil-mensaje">
          ¡Nuestros voluntarios son el alma viva de los huertos! Honramos a quienes dedican sus
          manos y saberes ancestrales al cuidado de la tierra y la salud comunitaria
        </p>
      </div>

      <form className="buscador-perfil-formulario" onSubmit={manejarSubmit} noValidate>
        <div className="buscador-perfil-campo">
          <CampoTexto
            etiqueta="Cédula de Identidad"
            valor={cedula}
            alCambiar={(nuevoValor) => {
              setCedula(nuevoValor)
              if (error) setError(null)
            }}
            inputMode="numeric"
            maxLength={10}
            placeholder="Ej. 1710034065"
            required
          />
        </div>

        <Boton
          type="submit"
          variante="primary"
          className="buscador-perfil-boton"
          disabled={cargando}
        >
          <svg
            width="18"
            height="18"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
            aria-hidden="true"
          >
            <circle cx="11" cy="11" r="8" />
            <line x1="21" y1="21" x2="16.65" y2="16.65" />
          </svg>
          <span>{cargando ? 'Buscando...' : 'Buscar Voluntario'}</span>
        </Boton>
      </form>

      {error && <Notificacion tipo="error" mensaje={error} />}

      <PerfilMiembroModal
        abierto={modalAbierto}
        perfil={perfil}
        alCerrar={() => setModalAbierto(false)}
      />
    </section>
  )
}
