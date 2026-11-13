import { CtaComunidadInicio } from '../features/inicio/CtaComunidadInicio'
import { HeroInicio } from '../features/inicio/HeroInicio'
import { NivelesKinti } from '../features/inicio/NivelesKinti'
import { SeccionLunarInicio } from '../features/inicio/SeccionLunarInicio'
import './InicioPage.css'

export function InicioPage() {
  return (
    <div className="inicio-pagina">
      <HeroInicio />
      <NivelesKinti />
      <SeccionLunarInicio />
      <CtaComunidadInicio />
    </div>
  )
}
