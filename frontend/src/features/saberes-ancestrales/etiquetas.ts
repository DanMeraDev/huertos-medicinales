import type { CategoriaSaber, FaseLunar } from './saberesAncestralesApi'

export const ETIQUETAS_CATEGORIA: Record<CategoriaSaber, string> = {
  PREPARACION_TIERRA: 'Preparación de la tierra',
  ABONO_NATURAL: 'Abono natural',
  RITUAL_SIEMBRA: 'Ritual de siembra',
  SIEMBRA_SENALETICA: 'Siembra y señalética',
  CUIDADO_RIEGO: 'Cuidado y riego',
  RITUAL_COSECHA: 'Ritual de cosecha',
  SECADO_EMPAQUETADO: 'Secado y empaquetado',
}

export const ETIQUETAS_FASE_LUNAR: Record<FaseLunar, string> = {
  YANA_KILLA_OSCURA: '🌑 Yana Killa · Luna oscura',
  WINAK_KILLA_CRECIENTE: '🌓 Wiñak Killa · Luna creciente',
  PURA_KILLA_LLENA: '🌕 Pura Killa · Luna llena',
  WANUK_KILLA_MENGUANTE: '🌘 Wañuk Killa · Luna menguante',
}
