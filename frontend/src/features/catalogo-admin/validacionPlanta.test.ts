import { plantaEditableMock } from './catalogoAdminMockHandlers'
import { construirSolicitud, datosDesdePlanta } from './validacionPlanta'

describe('datosDesdePlanta', () => {
  it('convierte la ficha técnica en los datos del formulario', () => {
    expect(datosDesdePlanta(plantaEditableMock)).toEqual({
      nombreComun: 'Hierba editable',
      nombreKichwa: 'Yura kiwa',
      nombreCientifico: 'Herba editabilis',
      familiaBotanica: 'Ficticiaceae',
      cualidadTermica: 'FRIA',
      cicloCultivo: 'CORTO',
      origen: 'NATIVA',
      altitudMinMsnm: '2000',
      altitudMaxMsnm: '3200',
      regionesDistribucion: ['Costa', 'Sierra'],
      usosMedicinales: 'Alivia el dolor de estómago',
      preparacionDosificacion: 'Infusión de 5 g en una taza',
      contraindicaciones: 'No usar en embarazo',
      categorias: ['DIURETICA', 'SEDANTE'],
    })
  })

  it('convierte "Sierra, Costa" en los chips Costa y Sierra', () => {
    const datos = datosDesdePlanta({ ...plantaEditableMock, regionesDistribucion: 'Sierra, Costa' })

    expect(datos.regionesDistribucion).toEqual(['Costa', 'Sierra'])
  })

  it('compara cada región recortada y descarta las que no son opciones', () => {
    const datos = datosDesdePlanta({ ...plantaEditableMock, regionesDistribucion: ' Amazonía ,Galápagos, Andes ' })

    expect(datos.regionesDistribucion).toEqual(['Amazonía', 'Galápagos'])
  })

  it('pasa los valores nulos a texto vacío o a la opción vacía', () => {
    const datos = datosDesdePlanta({
      ...plantaEditableMock,
      nombreKichwa: null,
      cicloCultivo: null,
      origen: null,
      altitudMinMsnm: null,
      altitudMaxMsnm: null,
      regionesDistribucion: null,
      preparacionDosificacion: null,
      contraindicaciones: null,
      fotografiaUrl: null,
    })

    expect(datos).toMatchObject({
      nombreKichwa: '',
      cicloCultivo: '',
      origen: '',
      altitudMinMsnm: '',
      altitudMaxMsnm: '',
      regionesDistribucion: [],
      preparacionDosificacion: '',
      contraindicaciones: '',
    })
  })

  it('conserva la altitud cero', () => {
    expect(datosDesdePlanta({ ...plantaEditableMock, altitudMinMsnm: 0 }).altitudMinMsnm).toBe('0')
  })

  it('al volver a construir la solicitud se obtiene la misma planta', () => {
    const solicitud = construirSolicitud(datosDesdePlanta(plantaEditableMock), 'FRIA', plantaEditableMock.fotografiaUrl)

    expect({ id: plantaEditableMock.id, ...solicitud }).toEqual({
      ...plantaEditableMock,
      regionesDistribucion: 'Costa, Sierra',
      categorias: ['DIURETICA', 'SEDANTE'],
    })
  })
})
