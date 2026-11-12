import {
  useCallback,
  useEffect,
  useEffectEvent,
  useId,
  useRef,
  useState,
  type ChangeEvent,
  type DragEvent,
  type KeyboardEvent,
} from 'react'
import { urlFoto } from '../api/fotos'
import { Boton } from './Boton'
import { Modal } from './Modal'
import { Notificacion } from './Notificacion'
import './CargadorImagen.css'

export const MENSAJE_FORMATO_NO_ADMITIDO = 'Formato no admitido. Seleccione una imagen JPG, PNG o WEBP'
export const MENSAJE_IMAGEN_DEMASIADO_GRANDE = 'La imagen supera el límite de 5 MB. Intente reducir la resolución'

const TIPOS_ADMITIDOS = ['image/jpeg', 'image/png', 'image/webp']
const TAMANIO_MAXIMO = 5 * 1024 * 1024

export interface CargadorImagenProps {
  etiqueta: string
  archivo: File | null
  alCambiar: (archivo: File | null) => void
  urlActual?: string | null
}

function validarImagen(archivo: File) {
  if (!TIPOS_ADMITIDOS.includes(archivo.type)) return MENSAJE_FORMATO_NO_ADMITIDO
  if (archivo.size > TAMANIO_MAXIMO) return MENSAJE_IMAGEN_DEMASIADO_GRANDE
  return null
}

function useVistaPrevia(archivo: File | null) {
  return useCallback(
    (imagen: HTMLImageElement | null) => {
      if (!imagen || !archivo) return
      const url = URL.createObjectURL(archivo)
      imagen.src = url
      return () => URL.revokeObjectURL(url)
    },
    [archivo],
  )
}

export function CargadorImagen({ etiqueta, archivo, alCambiar, urlActual = null }: CargadorImagenProps) {
  const idEtiqueta = useId()
  const contenedor = useRef<HTMLDivElement>(null)
  const entrada = useRef<HTMLInputElement>(null)
  const [error, setError] = useState<string | null>(null)
  const [arrastrando, setArrastrando] = useState(false)
  const [ampliada, setAmpliada] = useState(false)
  const refVista = useVistaPrevia(archivo)
  const urlGuardada = urlFoto(urlActual)
  const propsImagen = archivo ? { ref: refVista } : { src: urlGuardada ?? undefined }

  function seleccionar(nuevo: File) {
    const mensaje = validarImagen(nuevo)
    setError(mensaje)
    if (!mensaje) alCambiar(nuevo)
  }

  const alPegar = useEffectEvent((evento: ClipboardEvent) => {
    const ambito = contenedor.current?.closest('form') ?? contenedor.current
    const imagen = Array.from(evento.clipboardData?.files ?? []).find((pegado) => pegado.type.startsWith('image/'))
    if (!imagen || !ambito?.contains(document.activeElement)) return
    evento.preventDefault()
    seleccionar(imagen)
  })

  useEffect(() => {
    const escuchar = (evento: ClipboardEvent) => alPegar(evento)
    document.addEventListener('paste', escuchar)
    return () => document.removeEventListener('paste', escuchar)
  }, [])

  function abrirSelector() {
    entrada.current?.click()
  }

  function alElegir(evento: ChangeEvent<HTMLInputElement>) {
    const elegido = evento.target.files?.[0]
    evento.target.value = ''
    if (elegido) seleccionar(elegido)
  }

  function alTeclear(evento: KeyboardEvent<HTMLDivElement>) {
    if (evento.key !== 'Enter' && evento.key !== ' ') return
    evento.preventDefault()
    abrirSelector()
  }

  function alArrastrarEncima(evento: DragEvent<HTMLDivElement>) {
    evento.preventDefault()
    setArrastrando(true)
  }

  function alSoltar(evento: DragEvent<HTMLDivElement>) {
    evento.preventDefault()
    setArrastrando(false)
    const primero = evento.dataTransfer.files[0]
    if (primero) seleccionar(primero)
  }

  function quitar() {
    setError(null)
    alCambiar(null)
  }

  return (
    <div ref={contenedor} className="cargador-imagen">
      <span id={idEtiqueta} className="cargador-imagen-etiqueta">
        {etiqueta}
      </span>
      <input
        ref={entrada}
        type="file"
        accept={TIPOS_ADMITIDOS.join(',')}
        hidden
        aria-labelledby={idEtiqueta}
        onChange={alElegir}
      />
      {archivo || urlGuardada ? (
        <div className="cargador-imagen-vista">
          <img className="cargador-imagen-foto" alt={etiqueta} {...propsImagen} />
          <div className="cargador-imagen-controles">
            <Boton variante="secundario" onClick={() => setAmpliada(true)}>
              Ver en grande
            </Boton>
            <Boton variante="secundario" onClick={abrirSelector}>
              Cambiar
            </Boton>
            <Boton variante="peligro" onClick={quitar}>
              Quitar
            </Boton>
          </div>
          <Modal abierto={ampliada} titulo={etiqueta} alCancelar={() => setAmpliada(false)}>
            <img className="cargador-imagen-ampliada" alt={etiqueta} {...propsImagen} />
          </Modal>
        </div>
      ) : (
        <div
          role="button"
          tabIndex={0}
          aria-labelledby={idEtiqueta}
          className={arrastrando ? 'cargador-imagen-zona cargador-imagen-zona-activa' : 'cargador-imagen-zona'}
          onClick={abrirSelector}
          onKeyDown={alTeclear}
          onDragOver={alArrastrarEncima}
          onDragLeave={() => setArrastrando(false)}
          onDrop={alSoltar}
        >
          <svg className="cargador-imagen-icono" viewBox="0 0 24 24" aria-hidden="true">
            <path
              d="M7 18a5 5 0 0 1-.9-9.92A6 6 0 0 1 17.7 7.1 4.5 4.5 0 0 1 17 18M12 12v8m0-8-3 3m3-3 3 3"
              fill="none"
              stroke="currentColor"
              strokeWidth="1.8"
              strokeLinecap="round"
              strokeLinejoin="round"
            />
          </svg>
          <span className="cargador-imagen-texto-movil">Toca para tomar foto o elegir de tu galería</span>
          <span className="cargador-imagen-texto-escritorio">
            <strong>Arrastra y suelta la fotografía aquí</strong>
            <span className="cargador-imagen-subtitulo">
              o haz clic para explorar archivos (JPG, PNG, WEBP hasta 5 MB)
            </span>
          </span>
        </div>
      )}
      {error && <Notificacion tipo="error" mensaje={error} />}
    </div>
  )
}
