import { useState } from 'react'
import { useNavigate } from 'react-router'
import { Notificacion } from '../components/Notificacion'
import { FormularioSiembra } from '../features/huerto-siembras/FormularioSiembra'
import type { SiembraDetalle } from '../features/huerto-siembras/siembrasApi'

export function RegistroSiembraPage() {
  const navigate = useNavigate()
  const [exito, setExito] = useState<SiembraDetalle | null>(null)

  function manejarExito(siembra: SiembraDetalle) {
    setExito(siembra)
    setTimeout(() => {
      navigate('/huerto/subhuertos')
    }, 2000)
  }

  return (
    <div>
      {exito && (
        <Notificacion
          tipo="exito"
          mensaje={`Siembra registrada exitosamente: ${siembraResumen(exito)}`}
        />
      )}
      <FormularioSiembra alExito={manejarExito} />
    </div>
  )
}

function siembraResumen(siembra: SiembraDetalle): string {
  const alerta = siembra.tuvoAlertaAltitud ? ' (con alerta de altitud registrada)' : ''
  return `${siembra.cantidadSembrada} unidades/gramos de ${siembra.nombreComunPlanta} en ${siembra.nombreSubhuerto}${alerta}`
}

