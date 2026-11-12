import { http, HttpResponse } from 'msw'
import type { PaginaResponse } from '../../api/client'
import type { CategoriaUso, CualidadTermica, PlantaDetalle, PlantaResumen } from './types'

function planta(
  id: number,
  nombreComun: string,
  nombreKichwa: string | null,
  nombreCientifico: string,
  cualidadTermica: CualidadTermica,
  categorias: CategoriaUso[],
  archivo: string,
): PlantaResumen {
  return {
    id,
    nombreComun,
    nombreKichwa,
    nombreCientifico,
    cualidadTermica,
    categorias,
    fotografiaUrl: `plantas/${archivo}.jpg`,
  }
}

export const plantasMock: PlantaResumen[] = [
  planta(14, 'Achicoria', null, 'Cichorium intybus', 'CALIENTE', ['DEPURATIVA'], 'achicoria'),
  planta(12, 'Albahaca', 'Chakra kiwa', 'Ocimum basilicum', 'CALIENTE', ['ANTIESPASMODICA'], 'albahaca'),
  planta(11, 'Cedrón', null, 'Aloysia citrodora', 'CALIENTE', ['ANTIESPASMODICA', 'SEDANTE'], 'cedron'),
  planta(10, 'Chilca', 'Chillka', 'Baccharis latifolia', 'CALIENTE', ['ANTIINFLAMATORIA', 'ENERGETICA'], 'chilca'),
  planta(17, 'Cola de caballo', 'Caballo chupa', 'Equisetum giganteum', 'FRIA', ['DIURETICA'], 'cola-de-caballo'),
  planta(15, 'Diente de león', 'Tani', 'Taraxacum officinale', 'FRIA', ['DIURETICA', 'DEPURATIVA'], 'diente-de-leon'),
  planta(16, 'Llantén', null, 'Plantago major', 'FRIA', ['ASTRINGENTE', 'DIURETICA', 'CICATRIZANTE'], 'llanten'),
  planta(1, 'Manzanilla', null, 'Matricaria chamomilla', 'CALIENTE', ['ANTIESPASMODICA', 'SEDANTE', 'ANTIINFLAMATORIA'], 'manzanilla'),
  planta(7, 'Marco', null, 'Ambrosia arborescens', 'CALIENTE', ['ENERGETICA', 'ANTIINFLAMATORIA'], 'marco'),
  planta(6, 'Matico', null, 'Aristeguietia glutinosa', 'CALIENTE', ['CICATRIZANTE', 'ANTIINFLAMATORIA'], 'matico'),
  planta(2, 'Menta', null, 'Mentha x piperita', 'CALIENTE', ['ANTIESPASMODICA', 'DESCONGESTIONANTE'], 'menta'),
  planta(4, 'Orégano', null, 'Origanum vulgare', 'CALIENTE', ['ANTIOXIDANTE', 'ANTIESPASMODICA'], 'oregano'),
  planta(8, 'Ortiga', 'Tsini', 'Urtica dioica', 'CALIENTE', ['ANTIINFLAMATORIA', 'DIURETICA', 'ENERGETICA'], 'ortiga'),
  planta(9, 'Paico', 'Paiku', 'Dysphania ambrosioides', 'CALIENTE', ['ANTIPARASITARIA'], 'paico'),
  planta(5, 'Romero', null, 'Rosmarinus officinalis', 'CALIENTE', ['ASTRINGENTE', 'ESTIMULANTE', 'ANTIOXIDANTE'], 'romero'),
  planta(13, 'Ruda', null, 'Ruta graveolens', 'CALIENTE', ['ENERGETICA', 'ANTIESPASMODICA'], 'ruda'),
  planta(19, 'Sábila', null, 'Aloe vera', 'FRIA', ['CICATRIZANTE', 'ANTIINFLAMATORIA'], 'sabila'),
  planta(3, 'Toronjil', null, 'Melissa officinalis', 'CALIENTE', ['SEDANTE', 'ANTIESPASMODICA'], 'toronjil'),
  planta(18, 'Valeriana', null, 'Valeriana officinalis', 'FRIA', ['SEDANTE', 'ANTIESPASMODICA'], 'valeriana'),
  planta(20, 'Violeta', null, 'Viola odorata', 'FRIA', ['EXPECTORANTE'], 'violeta'),
]

export const colaDeCaballoDetalle: PlantaDetalle = {
  id: 17,
  nombreComun: 'Cola de caballo',
  nombreKichwa: 'Caballo chupa',
  nombreCientifico: 'Equisetum giganteum',
  familiaBotanica: 'Equisetaceae',
  cualidadTermica: 'FRIA',
  cicloCultivo: 'LARGO',
  origen: 'NATIVA',
  altitudMinMsnm: 0,
  altitudMaxMsnm: 3500,
  regionesDistribucion: 'Sierra, Amazonía',
  usosMedicinales:
    'Para problemas de riñón, infección de vías urinarias, reumatismo y dolores del cuerpo (Anexo 2.1). Diurética (Anexo 5).',
  preparacionDosificacion:
    'Hervir el tallo de una planta madura por 15 minutos en 1 litro de agua.\nTomar 1 vaso al día durante 4 días (Anexo 2.1).',
  contraindicaciones:
    'No usar por más de 4 semanas seguidas. No usar en embarazo, lactancia, ni en personas con enfermedad del corazón o del riñón.',
  categorias: ['DIURETICA'],
  fotografiaUrl: 'plantas/cola-de-caballo.jpg',
}

export const achicoriaSinDatosDetalle: PlantaDetalle = {
  id: 14,
  nombreComun: 'Achicoria',
  nombreKichwa: null,
  nombreCientifico: 'Cichorium intybus',
  familiaBotanica: 'Asteraceae',
  cualidadTermica: 'CALIENTE',
  cicloCultivo: null,
  origen: null,
  altitudMinMsnm: null,
  altitudMaxMsnm: null,
  regionesDistribucion: null,
  usosMedicinales: 'Ayuda a expulsar cálculos de la vesícula (Anexo 2.1). Depurativa.',
  preparacionDosificacion: null,
  contraindicaciones: null,
  categorias: ['DEPURATIVA'],
  fotografiaUrl: null,
}

const detallesMock = [colaDeCaballoDetalle, achicoriaSinDatosDetalle]

export function paginaDePlantas(plantas: PlantaResumen[], pagina: number, tamanio: number): PaginaResponse<PlantaResumen> {
  return {
    contenido: plantas.slice(pagina * tamanio, (pagina + 1) * tamanio),
    pagina,
    tamanio,
    totalElementos: plantas.length,
    totalPaginas: Math.ceil(plantas.length / tamanio),
  }
}

function normalizar(texto: string) {
  return texto.normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase()
}

function coincide(planta: PlantaResumen, criterio: string) {
  return [planta.nombreComun, planta.nombreCientifico, planta.nombreKichwa].some(
    (nombre) => nombre !== null && normalizar(nombre).includes(criterio),
  )
}

export const plantasHandlers = [
  http.get('*/api/publico/plantas/buscar', ({ request }) => {
    const criterio = normalizar(new URL(request.url).searchParams.get('criterio') ?? '')
    return HttpResponse.json(plantasMock.filter((planta) => coincide(planta, criterio)))
  }),
  http.get('*/api/publico/plantas/:id', ({ params }) => {
    const detalle = detallesMock.find((planta) => String(planta.id) === params.id)
    if (!detalle) {
      return HttpResponse.json({ detail: 'La ficha solicitada no está disponible' }, { status: 404 })
    }
    return HttpResponse.json(detalle)
  }),
  http.get('*/api/publico/plantas', ({ request }) => {
    const parametros = new URL(request.url).searchParams
    const pagina = Number(parametros.get('page') ?? 0)
    const tamanio = Number(parametros.get('size') ?? 12)
    return HttpResponse.json(paginaDePlantas(plantasMock, pagina, tamanio))
  }),
]
