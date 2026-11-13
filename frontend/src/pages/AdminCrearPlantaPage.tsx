import { FormularioCrearPlanta } from '../features/catalogo-admin/FormularioCrearPlanta'
import { ConsejoKinti } from '../components/ConsejoKinti'
import './AdminCrearPlantaPage.css'

export function AdminCrearPlantaPage() {
  return (
    <div className="admin-crear-planta-pagina">
      <h1>Registrar planta</h1>
      <ConsejoKinti
        variante="sabiduria"
        mensaje="Completa la ficha con cuidado: las comunidades la consultan antes de sembrar."
      />
      <FormularioCrearPlanta />
    </div>
  )
}
