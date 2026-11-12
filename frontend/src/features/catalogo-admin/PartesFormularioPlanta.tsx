import type { ReactNode } from 'react'
import { Link } from 'react-router'
import { Notificacion } from '../../components/Notificacion'
import { Tarjeta } from '../../components/Tarjeta'
import type { Aviso } from './envioPlanta'
import './FormularioCrearPlanta.css'

export function AvisoFormulario({ aviso }: { aviso: Aviso | null }) {
  if (!aviso) return null
  return (
    <div className="crear-planta-aviso">
      <Notificacion tipo="error" mensaje={aviso.mensaje} />
      {aviso.enlace && <Link to={aviso.enlace.ruta}>{aviso.enlace.texto}</Link>}
    </div>
  )
}

export function TarjetaFormulario({ titulo, children }: { titulo: string; children: ReactNode }) {
  return (
    <Tarjeta className="crear-planta-tarjeta">
      <h2 className="crear-planta-tarjeta-titulo">{titulo}</h2>
      {children}
    </Tarjeta>
  )
}
