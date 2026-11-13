import { useId, useState, type ComponentProps } from 'react'
import './CampoTexto.css'

export interface CampoTextoProps extends Omit<ComponentProps<'input'>, 'value' | 'onChange'> {
  etiqueta: string
  valor: string
  alCambiar: (valor: string) => void
  error?: string
  alternarVisibilidadClave?: boolean
}

export function CampoTexto({
  etiqueta,
  valor,
  alCambiar,
  error,
  alternarVisibilidadClave = false,
  type,
  ...props
}: CampoTextoProps) {
  const id = useId()
  const idError = `${id}-error`
  const [claveVisible, setClaveVisible] = useState(false)

  const soportaToggle = alternarVisibilidadClave && type === 'password'
  const tipoEfectivo = soportaToggle ? (claveVisible ? 'text' : 'password') : type

  return (
    <div className="campo">
      <label htmlFor={id} className="campo-etiqueta">
        {etiqueta}
      </label>
      <div className={soportaToggle ? 'campo-clave-envoltorio' : undefined}>
        <input
          id={id}
          type={tipoEfectivo}
          className={soportaToggle ? 'campo-control campo-control-con-ojo' : 'campo-control'}
          value={valor}
          onChange={(evento) => alCambiar(evento.target.value)}
          aria-invalid={Boolean(error)}
          aria-describedby={error ? idError : undefined}
          {...props}
        />
        {soportaToggle && (
          <button
            type="button"
            className="campo-ojo-boton"
            aria-label={claveVisible ? 'Ocultar contraseña' : 'Mostrar contraseña'}
            aria-pressed={claveVisible}
            onClick={() => setClaveVisible((prev) => !prev)}
          >
            {claveVisible ? (
              <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" strokeWidth="2" aria-hidden="true">
                <path d="M17.94 17.94A10.94 10.94 0 0 1 12 20c-7 0-11-8-11-8a21.77 21.77 0 0 1 5.06-6.94" strokeLinecap="round" strokeLinejoin="round" />
                <path d="M9.9 4.24A10.94 10.94 0 0 1 12 4c7 0 11 8 11 8a21.8 21.8 0 0 1-3.17 4.49" strokeLinecap="round" strokeLinejoin="round" />
                <path d="M14.12 14.12a3 3 0 1 1-4.24-4.24" strokeLinecap="round" strokeLinejoin="round" />
                <line x1="1" y1="1" x2="23" y2="23" strokeLinecap="round" />
              </svg>
            ) : (
              <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" strokeWidth="2" aria-hidden="true">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" strokeLinecap="round" strokeLinejoin="round" />
                <circle cx="12" cy="12" r="3" strokeLinecap="round" strokeLinejoin="round" />
              </svg>
            )}
          </button>
        )}
      </div>
      {error && (
        <p id={idError} className="campo-error">
          {error}
        </p>
      )}
    </div>
  )
}
