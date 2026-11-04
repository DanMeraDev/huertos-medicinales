import { useId, type ComponentProps } from 'react'
import './CampoTexto.css'

export interface OpcionSelector {
  valor: string
  etiqueta: string
}

export interface SelectorProps extends Omit<ComponentProps<'select'>, 'value' | 'onChange'> {
  etiqueta: string
  opciones: OpcionSelector[]
  valor: string
  alCambiar: (valor: string) => void
  error?: string
}

export function Selector({ etiqueta, opciones, valor, alCambiar, error, ...props }: SelectorProps) {
  const id = useId()
  const idError = `${id}-error`

  return (
    <div className="campo">
      <label htmlFor={id} className="campo-etiqueta">
        {etiqueta}
      </label>
      <select
        id={id}
        className="campo-control"
        value={valor}
        onChange={(evento) => alCambiar(evento.target.value)}
        aria-invalid={Boolean(error)}
        aria-describedby={error ? idError : undefined}
        {...props}
      >
        <option value="" disabled>
          Selecciona una opción
        </option>
        {opciones.map((opcion) => (
          <option key={opcion.valor} value={opcion.valor}>
            {opcion.etiqueta}
          </option>
        ))}
      </select>
      {error && (
        <p id={idError} className="campo-error">
          {error}
        </p>
      )}
    </div>
  )
}
