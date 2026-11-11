import { apiPost } from '../../api/client'

export type Rol = 'TECNICO_SIS' | 'TECNICO_MSP' | 'RESPONSABLE_COMUNIDAD'

export interface LoginRequest {
  correo: string
  password: string
}

export interface LoginResponse {
  token: string
  tipoToken: string
  id: number
  correo: string
  nombreCompleto: string
  rol: Rol
  requiereCambioClave: boolean
}

export const iniciarSesion = (credenciales: LoginRequest) =>
  apiPost<LoginResponse>('/publico/auth/login', credenciales)
