export interface SolicitudResumenResponse {
  id: number
  solicitante: string
  fecha: string
  provincia: string
}

export interface SolicitudDetalleCompletoResponse {
  id: number
  nombreComunidad: string
  tipoOrganizacion: string
  provincia: string
  parroquia: string
  direccion: string
  latitud: number
  longitud: number
  altitudMsnm: number
  disenoPrincipal: string
  subtiposSolicitados: string
  cedulaSolicitante: string
  nombreSolicitante: string
  fechaNacimientoSolicitante: string
  generoSolicitante: string
  correoSolicitante: string
  celularSolicitante: string
  estado: string
  creadoEn: string
}
