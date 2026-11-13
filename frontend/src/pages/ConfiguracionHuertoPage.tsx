import { ConfiguracionHuertoView } from '../features/huerto-configuracion/ConfiguracionHuertoView'
import { ConsejoKinti } from '../components/ConsejoKinti'

export function ConfiguracionHuertoPage() {
  return (
    <>
      <ConsejoKinti
        variante="dia"
        mensaje="Registra bien la altitud y el área de tu huerto: con ellas sabremos qué plantas crecen mejor allí."
      />
      <ConfiguracionHuertoView />
    </>
  )
}