import { apiPost } from '../../api/client'

export type Genero = 'FEMENINO' | 'MASCULINO' | 'OTRO'

export type Rol = 'TECNICO_SIS' | 'TECNICO_MSP' | 'RESPONSABLE_COMUNIDAD'

export interface CrearUsuarioRequest {
  nombreCompleto: string
  identificacion: string
  fechaNacimiento: string
  genero: Genero
  correo: string
  telefono: string
  password: string
  rol: Rol
}

export interface UsuarioDetalleResponse {
  id: number
  nombreCompleto: string
  identificacion: string
  fechaNacimiento: string
  genero: Genero
  correo: string
  telefono: string
  rol: Rol
  activo: boolean
  requiereCambioClave: boolean
  creadoEn: string
}

export function crearUsuario(datos: CrearUsuarioRequest) {
  return apiPost<UsuarioDetalleResponse>('/usuarios', datos)
}
