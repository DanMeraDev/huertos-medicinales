import { useId, type ComponentProps } from 'react'
import '../../components/CampoTexto.css'
import './CampoAreaTexto.css'

export interface CampoAreaTextoProps extends Omit<ComponentProps<'textarea'>, 'value' | 'onChange'> {
  etiqueta: string
  valor: string
  alCambiar: (valor: string) => void
  error?: string
}

export function CampoAreaTexto({ etiqueta, valor, alCambiar, error, rows = 4, ...props }: CampoAreaTextoProps) {
  const id = useId()
  const idError = `${id}-error`

  return (
    <div className="campo">
      <label htmlFor={id} className="campo-etiqueta">
        {etiqueta}
      </label>
      <textarea
        id={id}
        className="campo-control campo-area-texto"
        rows={rows}
        value={valor}
        onChange={(evento) => alCambiar(evento.target.value)}
        aria-invalid={Boolean(error)}
        aria-describedby={error ? idError : undefined}
        {...props}
      />
      {error && (
        <p id={idError} className="campo-error">
          {error}
        </p>
      )}
    </div>
  )
}
