export function urlFoto(ruta: string | null): string | null {
  return ruta ? `/api/publico/fotos/${ruta}` : null
}
