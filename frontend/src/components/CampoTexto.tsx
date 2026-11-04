import { useId, type ComponentProps } from 'react'
import './CampoTexto.css'

export interface CampoTextoProps extends Omit<ComponentProps<'input'>, 'value' | 'onChange'> {
  etiqueta: string
  valor: string
  alCambiar: (valor: string) => void
  error?: string
}

export function CampoTexto({ etiqueta, valor, alCambiar, error, ...props }: CampoTextoProps) {
  const id = useId()
  const idError = `${id}-error`

  return (
    <div className="campo">
      <label htmlFor={id} className="campo-etiqueta">
        {etiqueta}
      </label>
      <input
        id={id}
        className="campo-control"
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
