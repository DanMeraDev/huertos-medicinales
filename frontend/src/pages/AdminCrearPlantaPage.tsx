import { FormularioCrearPlanta } from '../features/catalogo-admin/FormularioCrearPlanta'
import './AdminCrearPlantaPage.css'

export function AdminCrearPlantaPage() {
  return (
    <div className="admin-crear-planta-pagina">
      <h1>Registrar planta</h1>
      <FormularioCrearPlanta />
    </div>
  )
}
