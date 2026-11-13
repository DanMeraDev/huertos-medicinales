import { useState, useEffect, type FormEvent } from 'react'
import { Boton } from '../../components/Boton'
import { CampoTexto } from '../../components/CampoTexto'
import { KintiGuia } from '../../components/KintiGuia'
import { Notificacion } from '../../components/Notificacion'
import { Selector, type OpcionSelector } from '../../components/Selector'
import { ApiError } from '../../api/client'
import { obtenerPlantasActivas, registrarMaterialVegetal } from './bodegaApi'
import type { MaterialVegetalResponse, PlantaResumen, TipoMaterialVegetal, UnidadMedida } from './types'
import './FormularioIngresoMaterial.css'

const OPCIONES_TIPO_MATERIAL: OpcionSelector[] = [
  { valor: 'SEMILLAS', etiqueta: 'Semillas' },
  { valor: 'ESTACAS', etiqueta: 'Estacas' },
  { valor: 'ESQUEJES', etiqueta: 'Esquejes' },
  { valor: 'PLANTULAS', etiqueta: 'Plántulas' },
]

const OPCIONES_UNIDAD_MEDIDA: OpcionSelector[] = [
  { valor: 'UNIDADES', etiqueta: 'Unidades' },
  { valor: 'GRAMOS', etiqueta: 'Gramos' },
  { valor: 'MANOJOS', etiqueta: 'Manojos' },
]

export interface FormularioIngresoMaterialProps {
  plantasDisponibles?: PlantaResumen[]
  onExito?: (material: MaterialVegetalResponse) => void
}

export function FormularioIngresoMaterial({
  plantasDisponibles,
  onExito,
}: FormularioIngresoMaterialProps) {
  const [plantasCargadas, setPlantasCargadas] = useState<PlantaResumen[]>([])
  const [plantaId, setPlantaId] = useState('')
  const [tipoMaterial, setTipoMaterial] = useState<TipoMaterialVegetal | ''>('')
  const [cantidad, setCantidad] = useState('')
  const [unidadMedida, setUnidadMedida] = useState<UnidadMedida | ''>('')
  const [loteOrigen, setLoteOrigen] = useState('')
  const [fechaIngreso, setFechaIngreso] = useState(() => new Date().toISOString().split('T')[0])
  const [cargando, setCargando] = useState(false)
  const [errores, setErrores] = useState<Record<string, string>>({})
  const [notificacion, setNotificacion] = useState<{ tipo: 'exito' | 'error'; mensaje: string } | null>(null)

  useEffect(() => {
    if (plantasDisponibles !== undefined) {
      return
    }
    let montado = true
    obtenerPlantasActivas()
      .then((resultado) => {
        if (montado && resultado) {
          setPlantasCargadas(resultado)
        }
      })
      .catch((error: unknown) => {
        if (montado) {
          const mensaje =
            error instanceof ApiError
              ? (error.detalle ?? error.message)
              : 'No se pudo cargar el catálogo de plantas'
          setNotificacion({ tipo: 'error', mensaje })
        }
      })
    return () => {
      montado = false
    }
  }, [plantasDisponibles])

  const plantas = plantasDisponibles ?? plantasCargadas

  const opcionesPlantas: OpcionSelector[] = plantas.map((planta) => ({
    valor: String(planta.id),
    etiqueta: planta.nombreComun,
  }))

  const manejarEnvio = async (evento: FormEvent<HTMLFormElement>) => {
    evento.preventDefault()
    setNotificacion(null)
    const nuevosErrores: Record<string, string> = {}

    if (!plantaId) {
      nuevosErrores.plantaId = 'Debe seleccionar una planta del catálogo'
    }

    if (!tipoMaterial) {
      nuevosErrores.tipoMaterial = 'Debe seleccionar un tipo de material'
    }

    const cantidadNumerica = parseFloat(cantidad)
    if (!cantidad || isNaN(cantidadNumerica) || cantidadNumerica <= 0) {
      nuevosErrores.cantidad = 'La cantidad debe ser mayor a cero'
      setNotificacion({
        tipo: 'error',
        mensaje: 'La cantidad debe ser mayor a cero',
      })
    }

    if (!unidadMedida) {
      nuevosErrores.unidadMedida = 'Debe seleccionar una unidad de medida'
    }

    if (!fechaIngreso) {
      nuevosErrores.fechaIngreso = 'La fecha de ingreso es obligatoria'
    }

    if (Object.keys(nuevosErrores).length > 0) {
      setErrores(nuevosErrores)
      return
    }

    setErrores({})
    setCargando(true)

    try {
      const respuesta = await registrarMaterialVegetal({
        plantaId: Number(plantaId),
        tipoMaterial: tipoMaterial as TipoMaterialVegetal,
        cantidad: cantidadNumerica,
        unidadMedida: unidadMedida as UnidadMedida,
        loteOrigen: loteOrigen.trim() || undefined,
        fechaIngreso,
      })

      setNotificacion({
        tipo: 'exito',
        mensaje: 'Ingreso de material vegetal registrado con éxito',
      })

      if (onExito) {
        onExito(respuesta)
      }
    } catch (error) {
      if (error instanceof ApiError) {
        if (error.errores) {
          setErrores(error.errores)
        }
        const mensajeError =
          error.status === 404
            ? error.detalle || 'La planta seleccionada no existe en el catálogo'
            : error.detalle || error.message || 'Error al registrar el ingreso'
        setNotificacion({
          tipo: 'error',
          mensaje: mensajeError,
        })
      } else {
        setNotificacion({
          tipo: 'error',
          mensaje: 'Error de conexión con el servidor',
        })
      }
    } finally {
      setCargando(false)
    }
  }

  return (
    <div className="formulario-ingreso-contenedor">
      <div className="formulario-ingreso-kinti-banner">
        <KintiGuia variante="alerta" tamano="md" className="formulario-ingreso-kinti" />
        <p className="formulario-ingreso-kinti-mensaje">
          ¡Qué alegría que haya llegado más material para el cultivo! Gracias por cuidar de la comunidad.
        </p>
      </div>

      <form className="formulario-ingreso" onSubmit={manejarEnvio} noValidate>
        <h2 className="formulario-ingreso-titulo">Ingreso de Material Vegetal</h2>
        <p className="formulario-ingreso-descripcion">
          Registra semillas, estacas, esquejes o plántulas para el inventario de la bodega comunitaria.
        </p>

        {notificacion && (
          <Notificacion tipo={notificacion.tipo} mensaje={notificacion.mensaje} />
        )}

        <div className="formulario-ingreso-campos">
          <Selector
            etiqueta="Planta del Catálogo"
            opciones={opcionesPlantas}
            valor={plantaId}
            alCambiar={(nuevoValor) => {
              setPlantaId(nuevoValor)
              setErrores((prev) => ({ ...prev, plantaId: '' }))
            }}
            error={errores.plantaId}
            required
          />

          <Selector
            etiqueta="Tipo de Material"
            opciones={OPCIONES_TIPO_MATERIAL}
            valor={tipoMaterial}
            alCambiar={(nuevoValor) => {
              setTipoMaterial(nuevoValor as TipoMaterialVegetal)
              setErrores((prev) => ({ ...prev, tipoMaterial: '' }))
            }}
            error={errores.tipoMaterial}
            required
          />

          <CampoTexto
            etiqueta="Cantidad Ingresada"
            type="number"
            step="0.01"
            min="0.01"
            valor={cantidad}
            alCambiar={(nuevoValor) => {
              setCantidad(nuevoValor)
              setErrores((prev) => ({ ...prev, cantidad: '' }))
            }}
            error={errores.cantidad}
            required
          />

          <Selector
            etiqueta="Unidad de Medida"
            opciones={OPCIONES_UNIDAD_MEDIDA}
            valor={unidadMedida}
            alCambiar={(nuevoValor) => {
              setUnidadMedida(nuevoValor as UnidadMedida)
              setErrores((prev) => ({ ...prev, unidadMedida: '' }))
            }}
            error={errores.unidadMedida}
            required
          />

          <CampoTexto
            etiqueta="Lote u Origen"
            placeholder="Ej. Donación vivero local"
            valor={loteOrigen}
            alCambiar={(nuevoValor) => setLoteOrigen(nuevoValor)}
          />

          <CampoTexto
            etiqueta="Fecha de Ingreso"
            type="date"
            valor={fechaIngreso}
            alCambiar={(nuevoValor) => {
              setFechaIngreso(nuevoValor)
              setErrores((prev) => ({ ...prev, fechaIngreso: '' }))
            }}
            error={errores.fechaIngreso}
            required
          />
        </div>

        <div className="formulario-ingreso-acciones">
          <Boton type="submit" disabled={cargando}>
            {cargando ? 'Registrando...' : 'Registrar Ingreso'}
          </Boton>
        </div>
      </form>
    </div>
  )
}
