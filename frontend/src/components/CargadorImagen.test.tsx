import { fireEvent, render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { useState } from 'react'
import { CargadorImagen } from './CargadorImagen'

const ETIQUETA = 'Fotografía de la planta'
const MENSAJE_FORMATO = 'Formato no admitido. Seleccione una imagen JPG, PNG o WEBP'
const MENSAJE_TAMANIO = 'La imagen supera el límite de 5 MB. Intente reducir la resolución'

function imagen(nombre = 'manzanilla.jpg', tipo = 'image/jpeg', bytes = 10) {
  return new File([new Uint8Array(bytes)], nombre, { type: tipo })
}

function CargadorControlado({ alCambiar, urlActual }: { alCambiar: (archivo: File | null) => void; urlActual?: string | null }) {
  const [archivo, setArchivo] = useState<File | null>(null)
  return (
    <CargadorImagen
      etiqueta={ETIQUETA}
      archivo={archivo}
      urlActual={urlActual}
      alCambiar={(nuevo) => {
        alCambiar(nuevo)
        setArchivo(nuevo)
      }}
    />
  )
}

function entradaArchivo() {
  return screen.getByLabelText(ETIQUETA, { selector: 'input' })
}

function zona() {
  return screen.getByRole('button', { name: ETIQUETA })
}

describe('CargadorImagen', () => {
  const usuario = userEvent.setup({ applyAccept: false })
  let crearUrl: ReturnType<typeof vi.fn<(archivo: Blob) => string>>
  let revocarUrl: ReturnType<typeof vi.fn<(url: string) => void>>

  beforeEach(() => {
    let contador = 0
    crearUrl = vi.fn(() => `blob:vista-${++contador}`)
    revocarUrl = vi.fn()
    Object.assign(URL, { createObjectURL: crearUrl, revokeObjectURL: revocarUrl })
  })

  it('acepta solo JPG, PNG y WEBP y no fuerza la cámara', () => {
    render(<CargadorImagen etiqueta={ETIQUETA} archivo={null} alCambiar={vi.fn()} />)

    expect(entradaArchivo()).toHaveAttribute('type', 'file')
    expect(entradaArchivo()).toHaveAttribute('accept', 'image/jpeg,image/png,image/webp')
    expect(entradaArchivo()).not.toHaveAttribute('capture')
  })

  it('muestra los textos de móvil y de escritorio', () => {
    render(<CargadorImagen etiqueta={ETIQUETA} archivo={null} alCambiar={vi.fn()} />)

    expect(within(zona()).getByText('Toca para tomar foto o elegir de tu galería')).toBeInTheDocument()
    expect(within(zona()).getByText('Arrastra y suelta la fotografía aquí')).toBeInTheDocument()
    expect(within(zona()).getByText('o haz clic para explorar archivos (JPG, PNG, WEBP hasta 5 MB)')).toBeInTheDocument()
  })

  it('elegir un archivo válido avisa y muestra la vista previa', async () => {
    const alCambiar = vi.fn()
    const foto = imagen()
    render(<CargadorControlado alCambiar={alCambiar} />)

    await usuario.upload(entradaArchivo(), foto)

    expect(alCambiar).toHaveBeenCalledExactlyOnceWith(foto)
    expect(crearUrl).toHaveBeenCalledWith(foto)
    expect(screen.getByRole('img', { name: ETIQUETA })).toHaveAttribute('src', 'blob:vista-1')
  })

  it('soltar un archivo válido avisa y muestra la vista previa', () => {
    const alCambiar = vi.fn()
    const foto = imagen('ruda.png', 'image/png')
    render(<CargadorControlado alCambiar={alCambiar} />)

    fireEvent.drop(zona(), { dataTransfer: { files: [foto, imagen('otra.jpg')] } })

    expect(alCambiar).toHaveBeenCalledExactlyOnceWith(foto)
    expect(screen.getByRole('img', { name: ETIQUETA })).toHaveAttribute('src', 'blob:vista-1')
  })

  it('resalta la zona mientras se arrastra un archivo encima', () => {
    render(<CargadorImagen etiqueta={ETIQUETA} archivo={null} alCambiar={vi.fn()} />)

    fireEvent.dragOver(zona())
    expect(zona()).toHaveClass('cargador-imagen-zona-activa')

    fireEvent.dragLeave(zona())
    expect(zona()).not.toHaveClass('cargador-imagen-zona-activa')
  })

  it('rechaza un formato no admitido sin avisar del cambio', async () => {
    const alCambiar = vi.fn()
    render(<CargadorImagen etiqueta={ETIQUETA} archivo={null} alCambiar={alCambiar} />)

    await usuario.upload(entradaArchivo(), imagen('notas.txt', 'text/plain'))

    expect(screen.getByRole('alert')).toHaveTextContent(MENSAJE_FORMATO)
    expect(alCambiar).not.toHaveBeenCalled()
  })

  it('rechaza un archivo soltado con formato no admitido', () => {
    const alCambiar = vi.fn()
    render(<CargadorImagen etiqueta={ETIQUETA} archivo={null} alCambiar={alCambiar} />)

    fireEvent.drop(zona(), { dataTransfer: { files: [imagen('animada.gif', 'image/gif')] } })

    expect(screen.getByRole('alert')).toHaveTextContent(MENSAJE_FORMATO)
    expect(alCambiar).not.toHaveBeenCalled()
  })

  it('rechaza una imagen de más de 5 MB sin avisar del cambio', async () => {
    const alCambiar = vi.fn()
    render(<CargadorImagen etiqueta={ETIQUETA} archivo={null} alCambiar={alCambiar} />)

    await usuario.upload(entradaArchivo(), imagen('grande.jpg', 'image/jpeg', 5 * 1024 * 1024 + 1))

    expect(screen.getByRole('alert')).toHaveTextContent(MENSAJE_TAMANIO)
    expect(alCambiar).not.toHaveBeenCalled()
  })

  it('acepta una imagen de exactamente 5 MB', async () => {
    const alCambiar = vi.fn()
    const limite = imagen('limite.webp', 'image/webp', 5 * 1024 * 1024)
    render(<CargadorImagen etiqueta={ETIQUETA} archivo={null} alCambiar={alCambiar} />)

    await usuario.upload(entradaArchivo(), limite)

    expect(alCambiar).toHaveBeenCalledExactlyOnceWith(limite)
    expect(screen.queryByRole('alert')).not.toBeInTheDocument()
  })

  it('toma la imagen pegada cuando el foco está dentro del formulario', async () => {
    const alCambiar = vi.fn()
    const captura = imagen('captura.png', 'image/png')
    render(
      <>
        <form>
          <input aria-label="Nombre común" />
          <CargadorControlado alCambiar={alCambiar} />
        </form>
        <button type="button">Fuera del formulario</button>
      </>,
    )

    await usuario.click(screen.getByLabelText('Nombre común'))
    fireEvent.paste(document.activeElement as Element, { clipboardData: { files: [captura] } })

    expect(alCambiar).toHaveBeenCalledExactlyOnceWith(captura)
    expect(screen.getByRole('img', { name: ETIQUETA })).toBeInTheDocument()
  })

  it('ignora lo pegado cuando el foco está fuera del formulario', async () => {
    const alCambiar = vi.fn()
    render(
      <>
        <form>
          <CargadorControlado alCambiar={alCambiar} />
        </form>
        <button type="button">Fuera del formulario</button>
      </>,
    )

    await usuario.click(screen.getByRole('button', { name: 'Fuera del formulario' }))
    fireEvent.paste(document.activeElement as Element, { clipboardData: { files: [imagen('captura.png', 'image/png')] } })

    expect(alCambiar).not.toHaveBeenCalled()
  })

  it('sin formulario, toma lo pegado solo con el foco dentro del cargador', async () => {
    const alCambiar = vi.fn()
    const captura = imagen('captura.png', 'image/png')
    render(
      <>
        <CargadorControlado alCambiar={alCambiar} />
        <button type="button">Otro control</button>
      </>,
    )

    await usuario.click(screen.getByRole('button', { name: 'Otro control' }))
    fireEvent.paste(document.activeElement as Element, { clipboardData: { files: [captura] } })
    expect(alCambiar).not.toHaveBeenCalled()

    zona().focus()
    fireEvent.paste(zona(), { clipboardData: { files: [captura] } })
    expect(alCambiar).toHaveBeenCalledExactlyOnceWith(captura)
  })

  it('ignora lo pegado si el portapapeles no trae una imagen', async () => {
    const alCambiar = vi.fn()
    render(
      <form>
        <input aria-label="Nombre común" />
        <CargadorImagen etiqueta={ETIQUETA} archivo={null} alCambiar={alCambiar} />
      </form>,
    )

    await usuario.click(screen.getByLabelText('Nombre común'))
    fireEvent.paste(document.activeElement as Element, { clipboardData: { files: [] } })

    expect(alCambiar).not.toHaveBeenCalled()
    expect(screen.queryByRole('alert')).not.toBeInTheDocument()
  })

  it('abre el selector con clic, Enter y Espacio', async () => {
    const abrirSelector = vi.fn()
    render(<CargadorImagen etiqueta={ETIQUETA} archivo={null} alCambiar={vi.fn()} />)
    entradaArchivo().addEventListener('click', abrirSelector)

    await usuario.tab()
    expect(zona()).toHaveFocus()
    await usuario.keyboard('{Enter}')
    expect(abrirSelector).toHaveBeenCalledTimes(1)

    await usuario.keyboard(' ')
    expect(abrirSelector).toHaveBeenCalledTimes(2)

    await usuario.click(zona())
    expect(abrirSelector).toHaveBeenCalledTimes(3)
  })

  it('Cambiar abre el selector de archivos', async () => {
    const abrirSelector = vi.fn()
    render(<CargadorImagen etiqueta={ETIQUETA} archivo={imagen()} alCambiar={vi.fn()} />)
    entradaArchivo().addEventListener('click', abrirSelector)

    await usuario.click(screen.getByRole('button', { name: 'Cambiar' }))

    expect(abrirSelector).toHaveBeenCalledOnce()
  })

  it('Quitar avisa con null', async () => {
    const alCambiar = vi.fn()
    render(<CargadorImagen etiqueta={ETIQUETA} archivo={imagen()} alCambiar={alCambiar} />)

    await usuario.click(screen.getByRole('button', { name: 'Quitar' }))

    expect(alCambiar).toHaveBeenCalledExactlyOnceWith(null)
  })

  it('Ver en grande abre el diálogo con la imagen', async () => {
    render(<CargadorImagen etiqueta={ETIQUETA} archivo={imagen()} alCambiar={vi.fn()} />)

    await usuario.click(screen.getByRole('button', { name: 'Ver en grande' }))

    const dialogo = screen.getByRole('dialog', { name: ETIQUETA })
    expect(within(dialogo).getByRole('img', { name: ETIQUETA })).toHaveAttribute('src', expect.stringMatching(/^blob:vista-/))

    await usuario.click(within(dialogo).getByRole('button', { name: 'Cancelar' }))
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
  })

  it('sin archivo nuevo muestra la fotografía guardada con sus controles', () => {
    render(<CargadorImagen etiqueta={ETIQUETA} archivo={null} urlActual="plantas/manzanilla.jpg" alCambiar={vi.fn()} />)

    expect(screen.getByRole('img', { name: ETIQUETA })).toHaveAttribute('src', '/api/publico/fotos/plantas/manzanilla.jpg')
    expect(screen.getByRole('button', { name: 'Ver en grande' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Cambiar' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Quitar' })).toBeInTheDocument()
    expect(crearUrl).not.toHaveBeenCalled()
  })

  it('revoca la URL de la vista previa al cambiar de archivo y al desmontar', async () => {
    const { unmount } = render(<CargadorControlado alCambiar={vi.fn()} />)

    await usuario.upload(entradaArchivo(), imagen('primera.jpg'))
    expect(revocarUrl).not.toHaveBeenCalled()

    await usuario.upload(entradaArchivo(), imagen('segunda.jpg'))
    expect(revocarUrl).toHaveBeenCalledExactlyOnceWith('blob:vista-1')

    unmount()
    expect(revocarUrl).toHaveBeenLastCalledWith('blob:vista-2')
  })
})
