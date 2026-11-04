import './Notificacion.css'

export interface NotificacionProps {
  tipo: 'exito' | 'error'
  mensaje: string
}

export function Notificacion({ tipo, mensaje }: NotificacionProps) {
  return (
    <div role={tipo === 'error' ? 'alert' : 'status'} className={`notificacion notificacion-${tipo}`}>
      {mensaje}
    </div>
  )
}
