import { useSyncExternalStore } from 'react'

const CONSULTA_ESCRITORIO = '(min-width: 1024px)'

function suscribir(avisar: () => void) {
  const consulta = window.matchMedia(CONSULTA_ESCRITORIO)
  consulta.addEventListener('change', avisar)
  return () => consulta.removeEventListener('change', avisar)
}

function esEscritorioAhora() {
  return window.matchMedia(CONSULTA_ESCRITORIO).matches
}

export function useEsEscritorio() {
  return useSyncExternalStore(suscribir, esEscritorioAhora)
}
